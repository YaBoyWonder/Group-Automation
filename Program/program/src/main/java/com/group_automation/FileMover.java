package com.group_automation;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class FileMover {

    /**
     * Monitors the Downloads folder for new files and moves them to the appropriate folder
     */
    public static void startMonitoring() {
        try {
            // Path to the Downloads folder
            Path downloadDir = Paths.get(System.getProperty("user.home"), "Downloads");

            WatchService watchService = FileSystems.getDefault().newWatchService();
            downloadDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Monitoring folder: " + downloadDir);

            while (true) {
                WatchKey key = watchService.take(); 

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = downloadDir.resolve((Path) event.context());
                        System.out.println("Downloaded file detected: " + filePath);

                        if (waitForFile(filePath)) {
                            moveFile(filePath);
                        } else {
                            System.out.println("Failed to move file: " + filePath);
                        }
                    }
                }
                key.reset(); 
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Waits for the file to be completely downloaded
     * @param filePath
     * @return true if the file is ready to be moved, false otherwise
     */
    private static boolean waitForFile(Path filePath) {
        int retryCount = 10;
        while (retryCount > 0) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                if (attributes.size() > 0) {
                    return true;
                }
            } catch (IOException e) {
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

    /**
     * Moves the file to the appropriate folder based on its extension
     * @param filePath
     */
    private static void moveFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = "";

        //Obtaining the file extension 
        int i = fileName.lastIndexOf('.');
        if (i > 0) 
            extension = fileName.substring(i + 1).toLowerCase();
        

        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");
        Path targetDir = null;
        //Folder paths for extension-type
        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
            targetDir = desktopPath.resolve("Images");
        } else if (extension.equals("pdf")) {
            targetDir = desktopPath.resolve("PDF");
        }

        //Moving the files to the target directory
        if (targetDir != null) {
            try {
                Files.createDirectories(targetDir); // Ensure the target directory exists
                Path targetPath = targetDir.resolve(filePath.getFileName());
                Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved file to: " + targetPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { System.out.println("Unsupported file type has been downloaded: " + fileName);  }
    }
}
