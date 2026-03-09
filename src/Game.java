import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Game {
    public static Image appIconImage = null;
    
    /**
     * Główna metoda uruchamiająca aplikację. Konfiguruje ustawienia systemowe i inicjalizuje GUI.
     * @param args argumenty wiersza poleceń (nieużywane)
     */
    public static void main(String[] args) {
        // Ustawienia dla macOS
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Skyscrapers");
        
        // Załaduj ikonę aplikacji
        try {
            File iconFile = new File("resources/icon.jpg");
            if (iconFile.exists()) {
                appIconImage = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
                
                // Ustawienie ikony dla Docka na macOS
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        taskbar.setIconImage(appIconImage);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Nie można załadować ikony: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                new SkyscrapersGameGUI();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Błąd krytyczny: Nie można załadować plików gry.\n" + e.getMessage(),
                        "Błąd Gry",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
