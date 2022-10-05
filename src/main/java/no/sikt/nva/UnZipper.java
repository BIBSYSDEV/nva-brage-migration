package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;

@JacocoGenerated
public final class UnZipper {

    public static final String UNZIPPING_WENT_WRONG_WITH_EXCEPTION = "Unzipping went wrong with exception :";

    private UnZipper() {
    }

    public static List<File> extractResourceDirectories(String pathToZip, String destinationDirectory) {
        var fileToUnzip = IoUtils.inputStreamFromResources(pathToZip);
        var destinationFile = new File(destinationDirectory);
        var unzippedFile = unzip(fileToUnzip, destinationFile);
        return Arrays.stream(Objects.requireNonNull(unzippedFile.listFiles())).collect(Collectors.toList());
    }

    private static File unzip(InputStream fileToUnzip, File destinationDirectory) {
        try (ZipInputStream inputStream = createInputStream(fileToUnzip)) {
            ZipEntry entry = inputStream.getNextEntry();
            writeToFile(inputStream, entry, destinationDirectory);
            inputStream.closeEntry();
            return new File(destinationDirectory.getName());
        } catch (IOException e) {
            throw new RuntimeException(UNZIPPING_WENT_WRONG_WITH_EXCEPTION, e);
        }
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private static void writeToFile(ZipInputStream inputStream, ZipEntry entry, File destinationDirectory)
        throws IOException {

        while (entry != null) {
            File newFile = newFile(destinationDirectory, entry);

            if (entry.isDirectory()) {
                handleUnknownFileType(newFile);
            } else {
                createFile(inputStream, newFile);
            }
            entry = inputStream.getNextEntry();
        }
    }

    @SuppressWarnings("PMD.AssignmentInOperand")
    private static void createFile(ZipInputStream inputStream, File newFile) throws IOException {
        byte[] buffer = new byte[1024];
        File parent = newFile.getParentFile();
        handleUnknownFileType(parent);
        var outputStream = Files.newOutputStream(Path.of(String.valueOf(newFile)));
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
    }

    private static void handleUnknownFileType(File newFile) throws IOException {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
        }
    }

    private static ZipInputStream createInputStream(InputStream fileToUnzip) throws IOException {
        return new ZipInputStream(fileToUnzip);
    }

    private static File newFile(File destinationDirectory, ZipEntry entry) throws IOException {
        File destinationFile = new File(destinationDirectory, entry.getName());
        String destinationDirectoryPath = destinationDirectory.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirectoryPath + File.separator)) {
            throw new IOException("Entry is outside of target directory: " + entry.getName());
        }

        return destinationFile;
    }
}

