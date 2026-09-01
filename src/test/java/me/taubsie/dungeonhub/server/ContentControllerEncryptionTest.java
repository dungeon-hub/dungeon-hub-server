package me.taubsie.dungeonhub.server;

import me.taubsie.dungeonhub.server.config.ConfigService;
import me.taubsie.dungeonhub.server.controller.ContentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.AEADBadTagException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ContentControllerEncryptionTest {
    @TempDir
    Path tempDirectory;

    private ContentController contentController;
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigService(tempDirectory.toString());
        contentController = new ContentController(configService, true, "test-secret-key-12345");
    }

    @Test
    void encryptThenDecrypt_returnsOriginalData() throws Exception {
        byte[] original = "Hello, this is test file content!".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = contentController.encrypt(original);
        byte[] decrypted = contentController.decrypt(encrypted);

        assertArrayEquals(original, decrypted);
    }

    @Test
    void encrypt_producesDifferentCiphertextEachTime() throws Exception {
        byte[] original = "same input".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted1 = contentController.encrypt(original);
        byte[] encrypted2 = contentController.encrypt(original);

        // random IV per call means ciphertext should differ even for identical input
        assertFalse(Arrays.equals(encrypted1, encrypted2));

        // but both should still decrypt correctly
        assertArrayEquals(original, contentController.decrypt(encrypted1));
        assertArrayEquals(original, contentController.decrypt(encrypted2));
    }

    @Test
    void encryptThenDecrypt_worksWithEmptyByteArray() throws Exception {
        byte[] original = new byte[0];

        byte[] encrypted = contentController.encrypt(original);
        byte[] decrypted = contentController.decrypt(encrypted);

        assertArrayEquals(original, decrypted);
    }

    @Test
    void encryptThenDecrypt_worksWithLargeBinaryData() throws Exception {
        byte[] original = new byte[1024 * 1024]; // 1MB, e.g. simulating a real file
        new Random(42).nextBytes(original);

        byte[] encrypted = contentController.encrypt(original);
        byte[] decrypted = contentController.decrypt(encrypted);

        assertArrayEquals(original, decrypted);
    }

    @Test
    void decrypt_failsWhenCiphertextIsTampered() throws Exception {
        byte[] original = "sensitive data".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = contentController.encrypt(original);

        // flip a byte after the IV (bytes 0-11) to corrupt the ciphertext/tag
        encrypted[encrypted.length - 1] ^= 0xFF;

        // GCM's auth tag should cause this to fail rather than silently return garbage
        assertThrows(AEADBadTagException.class, () -> contentController.decrypt(encrypted));
    }

    @Test
    void decrypt_failsWithWrongKey() throws Exception {
        byte[] original = "confidential".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = contentController.encrypt(original);

        ContentController wrongKeyService = new ContentController(configService, true, "a-completely-different-key");

        assertThrows(AEADBadTagException.class, () -> wrongKeyService.decrypt(encrypted));
    }

    @Test
    void sameKeyString_producesConsistentDecryption() throws Exception {
        // two service instances built from the same key string should be interoperable
        ContentController serviceA = new ContentController(configService, true, "shared-key");
        ContentController serviceB = new ContentController(configService, true, "shared-key");

        byte[] original = "cross-instance test".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = serviceA.encrypt(original);
        byte[] decrypted = serviceB.decrypt(encrypted);

        assertArrayEquals(original, decrypted);
    }
}