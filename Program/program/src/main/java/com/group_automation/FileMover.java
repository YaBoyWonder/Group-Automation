package com.group_automation;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileMover {

    public static void startMonitoring(String pngFolder, String pdfFolder) {
        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            downloadsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();
                        Path filePath = downloadsDir.resolve(fileName);
                        String extension = getFileExtension(fileName.toString());

                        try {
                            if (extension.equalsIgnoreCase("png")) {
                                moveFile(filePath, pngFolder);
                            } else if (extension.equalsIgnoreCase("pdf")) {
                                moveFile(filePath, pdfFolder);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void moveFile(Path filePath, String targetDir) throws IOException {
        Path targetPath = Paths.get(targetDir, filePath.getFileName().toString());
        Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        }
        return "";
    }
}
