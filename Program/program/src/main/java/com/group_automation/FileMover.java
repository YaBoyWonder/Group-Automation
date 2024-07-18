package com.group_automation;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class FileMover {

    public static void startMonitoring() {
        try {
            // Path to the Downloads folder
            Path downloadDir = Paths.get(System.getProperty("user.home"), "Downloads");

            // Create a WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();
            downloadDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Monitoring folder: " + downloadDir);

            while (true) {
                WatchKey key = watchService.take(); // Wait for a key to be available

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Check if a new file is created
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = downloadDir.resolve((Path) event.context());
                        System.out.println("New file detected: " + filePath);

                        // Wait until the file is not being used
                        if (waitForFile(filePath)) {
                            // Move the file based on its extension
                            moveFile(filePath);
                        } else {
                            System.out.println("Failed to move file: " + filePath);
                        }
                    }
                }
                key.reset(); // Reset the key and resume watching
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean waitForFile(Path filePath) {
        int retryCount = 10;
        while (retryCount > 0) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                if (attributes.size() > 0) {
                    return true;
                }
            } catch (IOException e) {
                // File is still being written or used by another process
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            retryCount--;
        }
        return false;
    }

    private static void moveFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = "";

        // Get the file extension
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }

        // Define target directories on the Desktop
        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");
        Path targetDir = null;
        if (extension.equals("png")) {
            targetDir = desktopPath.resolve("PNG");
        } else if (extension.equals("pdf")) {
            targetDir = desktopPath.resolve("PDF");
        }

        // Move the file to the target directory
        if (targetDir != null) {
            try {
                Files.createDirectories(targetDir); // Ensure the target directory exists
                Path targetPath = targetDir.resolve(filePath.getFileName());
                Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved file to: " + targetPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Unsupported file type: " + fileName);
        }
    }
}
