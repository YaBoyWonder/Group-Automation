package com.group_automation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static String pngFolder = "default";
    private static String pdfFolder = "default";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("File Mover");
            frame.setSize(400, 200);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent default close operation

            // Tray Icon
            if (SystemTray.isSupported()) {
                SystemTray systemTray = SystemTray.getSystemTray();
                Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(icon, "File Mover");

                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener(e -> frame.setVisible(true));

                PopupMenu popupMenu = new PopupMenu();
                MenuItem exitItem = new MenuItem("Exit");
                exitItem.addActionListener(e -> System.exit(0));
                popupMenu.add(exitItem);
                trayIcon.setPopupMenu(popupMenu);

                try {
                    systemTray.add(trayIcon);
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        frame.setVisible(false);
                        trayIcon.displayMessage("File Mover",
                                "Application is still running in the background. Click the icon to open.",
                                TrayIcon.MessageType.INFO);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(frame, "System tray not supported.");
                System.exit(1);
            }

            JButton selectPngFolderButton = new JButton("Select PNG Folder");
            selectPngFolderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedFolder = selectFolder();
                    if (selectedFolder != null) {
                        pngFolder = selectedFolder;
                        System.out.println("Selected PNG folder: " + pngFolder);
                    }
                }
            });

            JButton selectPdfFolderButton = new JButton("Select PDF Folder");
            selectPdfFolderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedFolder = selectFolder();
                    if (selectedFolder != null) {
                        pdfFolder = selectedFolder;
                        System.out.println("Selected PDF folder: " + pdfFolder);
                    }
                }
            });

            JButton startButton = new JButton("Start Monitoring");
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Pass the selected folders to FileMover
                    FileMover.startMonitoring(pngFolder, pdfFolder);
                }
            });

            JPanel panel = new JPanel();
            panel.add(selectPngFolderButton);
            panel.add(selectPdfFolderButton);
            panel.add(startButton);

            frame.getContentPane().add(panel);
            frame.setVisible(true);
        });
    }

    private static String selectFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
}
