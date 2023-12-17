package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.Hidden;
import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.server.security.user.UserEntity;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.util.InMemoryResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Hidden
@RestController
@EnableMethodSecurity
@RequestMapping("/cdn")
public class ContentController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    private static String getContentFolder() {
        return DungeonHubService.getInstance()
                .getMainFolder() + File.separator + "cdn";
    }

    public MimeType getMimeType(InputStream inputStream) throws IOException, MimeTypeException {
        TikaConfig config = TikaConfig.getDefaultConfig();

        Metadata metadata = new Metadata();
        org.apache.tika.mime.MediaType mediaType = config.getMimeRepository().detect(inputStream, metadata);

        return config.getMimeRepository().forName(mediaType.toString());
    }

    @PreAuthorize("hasAuthority('CDN') || hasAnyRole('bot', 'admin')")
    @PostMapping(value = {"", "/", "{name}"})
    public ResponseEntity<String> addFile(@RequestBody Resource image, @PathVariable(required = false) Optional<String> name, Authentication authentication) throws IOException {
        if (image == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        String username = null;

        if (authentication != null && authentication.getDetails() != null && authentication.getDetails() instanceof UserEntity userEntity) {
            username = userEntity.getUsername();
        }

        String fileExtension;
        try {
            fileExtension = getMimeType(new ByteArrayInputStream(image.getContentAsByteArray())).getExtension();
        }
        catch (MimeTypeException mimeTypeException) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileExtension == null || fileExtension.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        if (fileExtension.equalsIgnoreCase(".qt")) {
            fileExtension = ".mp4";
        }

        UUID uuid = UUID.randomUUID();
        String fileName = name
                .orElseGet(() -> String.valueOf(uuid))
                .replace("{uuid}", String.valueOf(uuid))
                + fileExtension;

        Path folder = Paths.get(getContentFolder());
        Files.createDirectories(folder);
        Files.write(folder.resolve(fileName), image.getContentAsByteArray());
        if (username != null) {
            setUploader(folder.resolve(fileName), username);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileName);
    }

    @GetMapping({"/static", "/static/", "/static/{file}"})
    public ResponseEntity<Resource> getStaticFile(@PathVariable(required = false) Optional<String> file) throws IOException {
        try {
            if (file.isEmpty()) {
                List<String> allFiles = Arrays.stream(new PathMatchingResourcePatternResolver()
                                .getResources("classpath:cdn-static/*"))
                        .map(Resource::getFilename)
                        .toList();

                return ResponseEntity
                        .status(HttpStatus.FOUND)
                        .body(new InMemoryResource(
                                DungeonHubService.getInstance()
                                        .getGson()
                                        .toJson(allFiles)
                        ));
            }

            Resource contentResource = new ClassPathResource("cdn-static/" + file.get());

            if (!contentResource.exists()) {
                throw new NoSuchFileException(file.get());
            }

            Tika tika = new Tika();
            String mimeType = tika.detect(contentResource.getInputStream());

            //this is needed because otherwise some browsers download the video instead of simply displaying it
            if (mimeType.equalsIgnoreCase("video/quicktime")) {
                mimeType = "video/mp4";
            }

            ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(file.get()).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);
            headers.setLastModified(contentResource.lastModified());
            headers.set("X-Content-Owner", "admin");

            ByteArrayResource image = new ByteArrayResource(contentResource.getContentAsByteArray());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .contentLength(image.contentLength())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(image);
        }
        catch (NoSuchFileException | FileNotFoundException noSuchFileException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{file}")
    public ResponseEntity<Resource> getFile(@PathVariable String file) throws IOException {
        Path folder = Paths.get(getContentFolder());

        try {
            Path content = folder.resolve(file);

            Tika tika = new Tika();
            String mimeType = tika.detect(content);

            ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(file).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);
            try {
                headers.setLastModified(Files.getLastModifiedTime(content).toInstant());
            }
            catch (NullPointerException ignored) {
                //ignored since that just means this isn't set
            }

            getUploader(content).ifPresent(s -> headers.set("X-Content-Owner", s));

            ByteArrayResource image = new ByteArrayResource(Files.readAllBytes(content));

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .contentLength(image.contentLength())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(image);
        }
        catch (NoSuchFileException noSuchFileException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public Optional<String> getUploader(@NotNull Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                return Optional.empty();
            }

            UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);

            ByteBuffer buf = ByteBuffer.allocate(view.size("cdn-uploaded"));
            view.read("cdn-uploaded", buf);
            buf.flip();
            return Optional.of(Charset.defaultCharset().decode(buf).toString());
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            logger.warn("Your file system doesn't support user-defined attributes. Please enable them for the full functionality of the cdn.");
            return Optional.empty();
        }
        catch (IOException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    public void setUploader(@NotNull Path path, @NotNull String uploader) {
        try {
            Files.setAttribute(path, "user:cdn-uploaded", Charset.defaultCharset().encode(uploader));
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            logger.warn("Your file system doesn't support user-defined attributes. Please enable them for the full functionality of the cdn.");
        }
        catch (IOException ioException) {
            //ignore since this is just an optional feature
        }
    }
}