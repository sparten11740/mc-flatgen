package dev.flatgen.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Writing Flatgen's files: whole, over a temporary file left by a crash, with a new file's usual permissions. */
class ConfigFilesTest {
    @TempDir
    Path folder;

    @Test
    void writesOverAFileAndLeavesNoTemporaryFileEvenOneACrashLeft() throws IOException {
        Path file = folder.resolve("library.json");
        Files.writeString(file, "old");
        Files.writeString(folder.resolve("library.json.tmp"), "half");
        assertTrue(ConfigFiles.write(file, "new"));
        assertEquals("new", Files.readString(file));
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of(file), files.toList());
        }
    }

    @Test
    void readsTheDefaultsOfAMissingFileWritingItOnlyWhenAsked() {
        Path written = folder.resolve("flatgen.json");
        Path left = folder.resolve("library.json");
        assertEquals("defaults", ConfigFiles.read(written, text -> text, "defaults", text -> text));
        assertEquals("defaults", ConfigFiles.readIfThere(left, text -> text, "defaults"));
        assertTrue(Files.exists(written));
        assertTrue(Files.notExists(left));
    }

    @Test
    void givesTheFileThePermissionsAnyNewFileGets() throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        Path plain = Files.writeString(folder.resolve("plain.json"), "");
        Path written = folder.resolve("written.json");
        ConfigFiles.write(written, "");
        assertEquals(Files.getPosixFilePermissions(plain), Files.getPosixFilePermissions(written));
    }
}
