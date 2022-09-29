package no.sikt.nva;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZipper {

    private final String fileToUnzip;
    private final File destinationDirectory;

    public UnZipper(String fileToUnzip, File destinationDirectory) {
        this.fileToUnzip = fileToUnzip;
        this.destinationDirectory = destinationDirectory;
    }

    public void unzip() throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream inputStream = createInputStream();
        ZipEntry entry = inputStream.getNextEntry();
        writeToFile(buffer, inputStream, entry);
        inputStream.closeEntry();
        inputStream.close();
    }

    private void writeToFile(byte[] buffer, ZipInputStream inputStream, ZipEntry entry) throws IOException {
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

    private void createFile(byte[] buffer, ZipInputStream inputStream, File newFile) throws IOException {
        File parent = newFile.getParentFile();
        handleUnknownFileType(parent);
        FileOutputStream outputStream = new FileOutputStream(newFile);
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

    private ZipInputStream createInputStream() throws FileNotFoundException {
        return new ZipInputStream(new FileInputStream(fileToUnzip));
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
