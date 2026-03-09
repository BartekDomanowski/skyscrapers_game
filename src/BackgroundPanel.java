import java.awt.*;
import javax.swing.JPanel;

/**
 * Klasa dziedzicząca po JPanel, umożliwiająca ustawienie obrazu w tle.
 * Obsługuje dwa tryby wyświetlania tła: wyśrodkowany (CENTERED) i skalowany (SCALED).
 */
public class BackgroundPanel extends JPanel {

    public static final int CENTERED = 0;
    public static final int SCALED   = 1;

    private Image backgroundImage = null;
    private int   backgroundType  = CENTERED;

    /**
     * Tworzy panel z zadanym obrazem tła. Domyślny tryb to SCALED (skalowany).
     * @param i obraz do ustawienia jako tło
     */
    public BackgroundPanel(Image i) {
        super();
        setBackgroundImage(i);
        setBackgroundType(BackgroundPanel.SCALED);
        setLayout(new GridBagLayout());
    }
    public BackgroundPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public BackgroundPanel(LayoutManager layout) {
        super(layout);
    }

    public BackgroundPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    /**
     * Ustawia nowy obraz tła i odświeża widok.
     * @param image nowy obraz tła
     */
    public void setBackgroundImage(Image image) {
        backgroundImage = image;
        repaint();
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Ustawia tryb wyświetlania tła (CENTERED lub SCALED).
     * @param type typ tła (BackgroundPanel.CENTERED lub BackgroundPanel.SCALED)
     * @throws IllegalArgumentException jeśli podano nieprawidłowy typ
     */
    public void setBackgroundType(int type) {
        if (type == CENTERED || type == SCALED) {
            backgroundType = type;
            repaint();
        } else {
            throw new IllegalArgumentException("Typ tła powinien być SCALED lub CENTERED.");
        }
    }

    public int getBackgroundType() {
        return backgroundType;
    }

    /**
     * Rysuje komponent wraz z tłem, zgodnie z wybranym trybem.
     * @param g kontekst graficzny
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            if (backgroundType == CENTERED) {
                int imageX = (getWidth() - backgroundImage.getWidth(this)) / 2;
                int imageY = (getHeight() - backgroundImage.getHeight(this)) / 2;
                imageX = Math.max(0, imageX);
                imageY = Math.max(0, imageY);
                g.drawImage(backgroundImage, imageX, imageY, this);
            } else if (backgroundType == SCALED) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}