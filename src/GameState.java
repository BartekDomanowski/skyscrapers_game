import java.io.Serializable;

/**
 * Klasa przechowująca stan gry do serializacji
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[][] board;
    private final int size;
    private final Difficulty difficulty;

    /**
     * Tworzy nowy stan gry
     * @param board Plansza gry (tablica 2D)
     * @param size Rozmiar planszy (bez krawędzi ze wskazówkami)
     * @param difficulty Poziom trudności
     */
    public GameState(int[][] board, int size, Difficulty difficulty) {
        // Kopiujemy tablicę, aby uniknąć modyfikacji zewnętrznych
        this.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            this.board[i] = board[i].clone();
        }
        this.size = size;
        this.difficulty = difficulty;
    }

    /**
     * Zwraca planszę gry.
     * @return tablica 2D reprezentująca planszę
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Zwraca rozmiar planszy.
     * @return rozmiar planszy
     */
    public int getSize() {
        return size;
    }

    /**
     * Zwraca poziom trudności gry.
     * @return poziom trudności
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }
}