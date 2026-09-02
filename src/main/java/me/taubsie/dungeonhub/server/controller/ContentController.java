package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.Hidden;
import me.taubsie.dungeonhub.server.config.ConfigService;
import me.taubsie.dungeonhub.server.exception.ProgramStartException;
import net.dungeonhub.service.MoshiService;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.util.InMemoryResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/cdn")
public class ContentController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);
    private static final String ENCRYPTED_FILE_EXTENSION = ".enc";
    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final ConfigService configService;
    @Nullable
    private final SecretKey secretKey;

    @Autowired
    public ContentController(
            ConfigService configService,
            @Value(value = "${dungeon-hub.encryption.enabled:false}") boolean encryptionEnabled,
            @Value(value = "${dungeon-hub.encryption.key:}") String encryptionKey
    ) {
        this.configService = configService;
        if(encryptionEnabled) {
            if(encryptionKey == null || encryptionKey.isBlank()) {
                throw new ProgramStartException("The encryption key is empty, even tho you enabled encryption! Please set a key.");
            }

            this.secretKey = loadKey(encryptionKey);
        } else {
            this.secretKey = null;
        }
    }

    private SecretKey loadKey(String encryptionKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES"); // 32 bytes -> AES-256
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            // SHA-256 is guaranteed on every JVM, so this only fires on a broken/exotic runtime.
            // Throwing here fails the app context startup instead of failing per-request later.
            throw new ProgramStartException("Failed to initialize encryption key", noSuchAlgorithmException);
        }
    }

    public byte[] encrypt(byte[] data) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] encrypted = cipher.doFinal(data);

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
        buffer.put(iv);
        buffer.put(encrypted);
        return buffer.array();
    }

    public byte[] decrypt(byte[] encryptedData) throws GeneralSecurityException, BufferUnderflowException {
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[IV_LENGTH_BYTES];
        buffer.get(iv);
        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        return cipher.doFinal(cipherBytes);
    }

    private String getContentFolder() {
        return configService.getDungeonHubDirectory() + File.separator + "cdn";
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String fileExtension;
        try {
            fileExtension = getMimeType(new ByteArrayInputStream(image.getContentAsByteArray())).getExtension();
        }
        catch (MimeTypeException mimeTypeException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileExtension == null || fileExtension.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        if (fileExtension.equalsIgnoreCase(".qt")) {
            fileExtension = ".mp4";
        }

        if (name.isPresent() && name.get().endsWith(".html")) {
            fileExtension = ".html";
            name = Optional.of(name.get().substring(0, name.get().length() - 5));
        }

        UUID uuid = UUID.randomUUID();
        String originalFileName = name
                .orElseGet(() -> String.valueOf(uuid))
                .replace("{uuid}", String.valueOf(uuid))
                + fileExtension;

        String fileName = originalFileName;

        Path folder = Paths.get(getContentFolder());
        Files.createDirectories(folder);

        boolean encrypt = secretKey != null;
        byte[] data = image.getContentAsByteArray();

        if(encrypt) {
            try {
                data = encrypt(data);
                fileName += ENCRYPTED_FILE_EXTENSION;
            }
            catch (Exception exception) {
                logger.error("Error while trying to encrypt file {}. Falling back to non-encrypted storage.", originalFileName, exception);
            }
        }

        Files.write(folder.resolve(fileName), data);
        if (authentication != null) {
            setUploader(folder.resolve(fileName), authentication.getName());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(originalFileName);
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
                                MoshiService.INSTANCE.getMoshi()
                                        .adapter(List.class)
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
        String originalFileName = file;

        try {
            if(secretKey != null && Files.exists(folder.resolve(file + ENCRYPTED_FILE_EXTENSION))) {
                file += ENCRYPTED_FILE_EXTENSION;
            }

            Path content = folder.resolve(file);

            ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(originalFileName).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);
            try {
                headers.setLastModified(Files.getLastModifiedTime(content).toInstant());
            }
            catch (NullPointerException ignored) {
                //ignored since that just means this isn't set
            }

            getUploader(content).ifPresent(s -> headers.set("X-Content-Owner", s));

            Resource responseResource = new FileSystemResource(content);

            if(secretKey != null && file.endsWith(ENCRYPTED_FILE_EXTENSION)) {
                try {
                    responseResource = new ByteArrayResource(decrypt(responseResource.getContentAsByteArray()));
                }
                catch (GeneralSecurityException generalSecurityException) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            }

            Tika tika = new Tika();
            String mimeType = tika.detect(responseResource.getContentAsByteArray());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .contentLength(responseResource.contentLength())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(responseResource);
        }
        catch (NoSuchFileException noSuchFileException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (BufferUnderflowException bufferUnderflowException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
            logger.error("Your file system doesn't support user-defined attributes. Please enable them for the full functionality of the cdn.");
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