package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class UnZipper {

    public static final String UNZIPPING_WENT_WRONG_WITH_EXCEPTION = "Unzipping went wrong with exception :";

    public UnZipper() {
    }

    public void unzip(InputStream fileToUnzip, File destinationDirectory) {
        byte[] buffer = new byte[1024];
        try (ZipInputStream inputStream = createInputStream(fileToUnzip)) {
            ZipEntry entry = inputStream.getNextEntry();
            writeToFile(buffer, inputStream, entry, destinationDirectory);
            inputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(UNZIPPING_WENT_WRONG_WITH_EXCEPTION + e);
        }
    }

    @SuppressWarnings("PMD")
    private void writeToFile(byte[] buffer, ZipInputStream inputStream, ZipEntry entry, File destinationDirectory)
        throws IOException {

        while (entry != null) {
            File newFile = newFile(destinationDirectory, entry);

            if (entry.isDirectory()) {
                handleUnknownFileType(newFile);
            } else {
                createFile(buffer, inputStream, newFile);
            }
            entry = inputStream.getNextEntry();
        }
    }

    @SuppressWarnings("PMD")
    private void createFile(byte[] buffer, ZipInputStream inputStream, File newFile) throws IOException {
        File parent = newFile.getParentFile();
        handleUnknownFileType(parent);
        var outputStream = Files.newOutputStream(Path.of(String.valueOf(newFile)));
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
    }

    private void handleUnknownFileType(File newFile) throws IOException {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
        }
    }

    private ZipInputStream createInputStream(InputStream fileToUnzip) throws IOException {
        return new ZipInputStream(fileToUnzip);
    }

    private File newFile(File destinationDirectory, ZipEntry entry) throws IOException {
        File destinationFile = new File(destinationDirectory, entry.getName());
        String destinationDirectoryPath = destinationDirectory.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirectoryPath + File.separator)) {
            throw new IOException("Entry is outside of target directory: " + entry.getName());
        }

        return destinationFile;
    }
}

