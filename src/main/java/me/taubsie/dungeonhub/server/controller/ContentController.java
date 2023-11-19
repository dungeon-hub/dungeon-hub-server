package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.Hidden;
import me.taubsie.dungeonhub.common.DungeonHubService;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Hidden
@RestController
@EnableMethodSecurity
@RequestMapping("/cdn")
public class ContentController {
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
    @PostMapping(value = {"", "{name}"})
    public ResponseEntity<String> addFile(@RequestBody Resource image, @PathVariable(required = false) Optional<String> name) throws IOException {
        if (image == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        String fileExtension;
        try {
            fileExtension = getMimeType(new ByteArrayInputStream(image.getContentAsByteArray())).getExtension();
        }
        catch (MimeTypeException mimeTypeException) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(fileExtension == null || fileExtension.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        if(fileExtension.equalsIgnoreCase(".qt")) {
            fileExtension = ".mp4";
        }

        UUID uuid = UUID.randomUUID();
        String fileName = name
                .orElseGet(() -> String.valueOf(uuid))
                .replace("{uuid}", String.valueOf(uuid))
                + fileExtension;

        Path folder = Paths.get(getContentFolder());
        Files.write(folder.resolve(fileName), image.getContentAsByteArray());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileName);
    }

    @GetMapping("/{file}")
    public ResponseEntity<Resource> getFile(@PathVariable String file, Authentication authentication) throws IOException {
        if(authentication == null || !authentication.isAuthenticated()) {
            //TODO if file is only for admins
        }

        Path folder = Paths.get(getContentFolder());

        try {
            Path content = folder.resolve(file);

            Tika tika = new Tika();
            String mimeType = tika.detect(content);

            ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                    .filename(file).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);

            ByteArrayResource image = new ByteArrayResource(Files.readAllBytes(content));

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .contentLength(image.contentLength())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(image);
        } catch (NoSuchFileException noSuchFileException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}