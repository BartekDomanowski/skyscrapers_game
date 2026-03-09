/**
 * Klasa reprezentująca planszę gry (zarówno wewnętrzną siatkę, jak i zewnętrzne podpowiedzi).
 */
public class Board {
    private static final String SEPARATOR = "---------------\n";
    private int[][] boardWithClues;
    private int[][] onlyBoard;
    private int size;

    /**
     * Tworzy planszę na podstawie istniejącej tablicy z podpowiedziami.
     * @param size rozmiar planszy
     * @param difficulty poziom trudności
     * @param boardWithClues tablica zawierająca planszę wraz z podpowiedziami
     * @throws IncorrectBoardSizeException jeśli rozmiar jest nieprawidłowy
     */
    public Board(int size, Difficulty difficulty, int[][] boardWithClues) throws IncorrectBoardSizeException {
        if (size < 3 || size > 5) {
            throw new IncorrectBoardSizeException("Rozmiar musi być między 3 a 5");
        }
        else if ((size != boardWithClues.length - 2) || (size != boardWithClues[0].length - 2)) {
            throw new IncorrectBoardSizeException("Rozmiar planszy musi być zgodny z zadeklarowaną wartością");
        }
        this.size = size;
        this.boardWithClues = boardWithClues;
        this.onlyBoard = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.onlyBoard[i][j] = boardWithClues[i + 1][j + 1];
            }
        }
    }

    /**
     * Zwraca rozmiar planszy.
     * @return rozmiar
     */
    public int getSize() {
        return size;
    }

    /**
     * Domyślny konstruktor (tworzy pustą planszę o rozmiarze 0).
     */
    public Board(){
        this.size = 0;
    }
    /**
     * Tworzy pustą planszę o zadanym rozmiarze.
     * @param size rozmiar planszy
     * @throws IncorrectBoardSizeException jeśli rozmiar jest wadliwy
     */
    public Board(int size) throws IncorrectBoardSizeException {
        if (size < 3 || size > 5) {
            throw new IncorrectBoardSizeException("Rozmiar musi być między 3 a 5");
        }
        this.size = size;
        onlyBoard = new int[size][size];
        boardWithClues = new int[size + 2][size + 2];
    }
    /**
     * Ustawia górne podpowiedzi.
     * @param clues tablica podpowiedzi
     */
    public void setTopClues(int[] clues) {
        for (int i = 1; i < size + 1; i++) {
            boardWithClues[0][i] = clues[i - 1];
        }
    }
    /**
     * Ustawia dolne podpowiedzi.
     * @param clues tablica podpowiedzi
     */
    public void setDownClues(int[] clues) {
        for (int i = 1; i < size + 1; i++) {
            boardWithClues[size + 1][i] = clues[i - 1];
        }
    }
    /**
     * Ustawia lewe podpowiedzi.
     * @param clues tablica podpowiedzi
     */
    public void setLeftClues(int[] clues) {
        for (int i = 1; i < size + 1; i++) {
            boardWithClues[i][0] = clues[i - 1];
        }
    }
    /**
     * Ustawia prawe podpowiedzi.
     * @param clues tablica podpowiedzi
     */
    public void setRightClues(int[] clues) {
        for (int i = 1; i < size + 1; i++) {
            boardWithClues[i][size + 1] = clues[i - 1];
        }
    }

    /**
     * Zwraca pełną tablicę (wraz z podpowiedziami).
     * @return tablica 2D z krawędziami
     */
    public int[][] getBoardWithClues() {
        return boardWithClues;
    }

    /**
     * Zwraca tylko wewnętrzną planszę (bez podpowiedzi).
     * @return tablica 2D reprezentująca środek planszy
     */
    public int[][] getOnlyBoard() {
        return onlyBoard;
    }

    @Override
    /**
     * Reprezentacja tekstowa planszy.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size + 2; i++) {
            if (i == 1 || i == size + 1) {
                sb.append(SEPARATOR);
            }
            for (int j = 0; j < size + 2; j++) {
                if (j == 1 || j == size + 1) {
                    sb.append("| ");
                }
                if ((i == 0 && j == 0) || (i == 0 && j == size + 1) || (i == size + 1 && j == 0) || (i == size + 1 && j == size + 1)) {
                    sb.append("  ");
                    continue;
                }
                sb.append(getSingleCellOfBoard(i, j)).append(" ");
            }
            if (i < size + 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    /**
     * Zwraca wartość pojedynczej komórki z pełnej planszy (z podpowiedziami).
     * @param i indeks wiersza
     * @param j indeks kolumny
     * @return wartość komórki
     */
    public int getSingleCellOfBoard(int i, int j) {
        return boardWithClues[i][j];
    }
    /**
     * Ustawia wartość pojedynczej komórki w wewnętrznej planszy oraz aktualizuje pełną planszę.
     * @param i indeks wiersza (wewnętrzny)
     * @param j indeks kolumny (wewnętrzny)
     * @param value wartość do ustawienia
     */
    public void setSingleCellOfOnlyBoard(int i, int j, int value) {
        this.boardWithClues[i + 1][j + 1] = value;
        this.onlyBoard[i][j] = value;
    }
    /**
     * Pobiera cały wiersz z wewnętrznej planszy.
     * @param row indeks wiersza
     * @return tablica intów reprezentująca wiersz
     */
    public int[] getRowOfOnlyBoard(int row) {
        return onlyBoard[row];
    }
    /**
     * Pobiera całą kolumnę z wewnętrznej planszy.
     * @param column indeks kolumny
     * @return tablica intów reprezentująca kolumnę
     */
    public int[] getColumnOfOnlyBoard(int column) {
        int[] col = new int[size];
        for (int row = 0; row < size; row++) {
            col[row] = onlyBoard[row][column];
        }
        return col;
    }
}