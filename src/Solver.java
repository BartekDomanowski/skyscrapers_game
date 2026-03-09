/**
 * Klasa odpowiedzialna za rozwiązywanie planszy Skyscrapers i sprawdzanie poprawności rozwiązania.
 * Wykorzystuje algorytm backtrackingu.
 */
public class Solver {
    private int[][] board;
    private int[][] boardToCheck;
    private int size;
    private boolean winnerBoard;
    /**
     * Konstruktor solvera.
     * @param size rozmiar planszy
     * @param boardToCheck referencyjna plansza do sprawdzenia
     */
    public Solver(int size, int[][] boardToCheck) {
        this.size = size;
        this.boardToCheck = boardToCheck;
        this.board = new int[size + 2][size + 2];
        copySideClues(boardToCheck);}
    /**
     * Alternatywny konstruktor solvera.
     * @param size rozmiar planszy
     * @param boardToCheck plansza do skopiowania podpowiedzi
     * @param solve czy rozwiązywać (parametr nieużywany w tej wersji, ale dla kompatybilności zachowany)
     */
    public Solver(int size,int[][] boardToCheck,boolean solve){
        this.size = size;
        this.board = new int[size + 2][size + 2];
        copySideClues(boardToCheck);
    }

    /**
     * Kopiuje podpowiedzi brzegowe z planszy źródłowej do planszy roboczej.
     * @param source plansza źródłowa
     */
    private void copySideClues(int[][] source) {
        for (int i = 1; i <= size; i++) {
            board[0][i] = source[0][i];
        }
        for (int i = 1; i <= size; i++) {
            board[size + 1][i] = source[size + 1][i];
        }
        for (int i = 1; i <= size; i++) {
            board[i][0] = source[i][0];
        }
        for (int i = 1; i <= size; i++) {
            board[i][size + 1] = source[i][size + 1];
        }
    }
    public int[][] getBoard() {
        return board;
    }
    public int getSize() {
        return size;
    }
    /**
     * Zwraca czy plansza została pomyślnie rozwiązana i jest zgodna z wzorcem.
     * @return true jeśli rozwiązanie jest poprawne
     */
    public boolean isWinnerBoard() {
        return winnerBoard;
    }
    /**
     * Uruchamia proces rozwiązywania (backtracking).
     * @return true jeśli znaleziono rozwiązanie zgodne
     */
    public boolean run() {
        solve(1,1);
        return winnerBoard;
    }
    /**
     * Sprawdza, czy aktualny stan planszy spełnia wszystkie zasady Skyscrapers (widoczność budynków).
     * @return true jeśli plansza jest poprawna
     */
    public boolean skyscrapersCorrect() {
        for (int col = 1; col <= size; col++) {
            int upDownCounter = 0;
            int upDownMax = 0;
            for (int a = 1; a <= size; a++) {
                if (this.board[a][col] > upDownMax) {
                    upDownMax = this.board[a][col];
                    upDownCounter++;
                }
            }
            if (upDownCounter != this.board[0][col] && this.board[0][col] != 0) {
                return false;
            }
        }
        for (int col = 1; col <= size; col++) {
            int downUpCounter = 0;
            int downUpMax = 0;
            for (int a = size; a >= 1; a--) {
                if (this.board[a][col] > downUpMax) {
                    downUpMax = this.board[a][col];
                    downUpCounter++;
                }
            }
            if (downUpCounter != this.board[size + 1][col] && this.board[size + 1][col] != 0) {
                return false;
            }
        }
        for(int row = 1; row <= size; row++) {
            int leftRightCounter = 0;
            int leftRightMax = 0;
            for (int a = 1; a <= size; a++) {
                if (this.board[row][a] > leftRightMax) {
                    leftRightMax = this.board[row][a];
                    leftRightCounter++;
                }
            }
            if (leftRightCounter != this.board[row][0] && this.board[row][0] != 0) {
                return false;
            }
        }
        for (int row = 1; row <= size; row++) {
            int rightLeftCounter = 0;
            int rightLeftMax = 0;
            for (int a = size; a >= 1; a--) {
                if (this.board[row][a] > rightLeftMax) {
                    rightLeftMax = this.board[row][a];
                    rightLeftCounter++;
                }
            }
            if (rightLeftCounter != this.board[row][size + 1] && this.board[row][size + 1] != 0) {
                return false;
            }
        }
        return true;
    }
    /**
     * Sprawdza, czy wstawienie wartości na pozycji (i, j) nie powoduje duplikatów w wierszu lub kolumnie.
     * @param i indeks wiersza
     * @param j indeks kolumny
     * @return true jeśli brak duplikatów
     */
    public boolean checkForDuplicatesRowAndCol(int i, int j) {
        int currentValue = this.board[i][j];
        for (int col = 1; col <= size; col++) {
            if (col == j) {
                continue;
            }
            if (this.board[i][col] == currentValue && this.board[i][col] > 0) {
                return false;
            }
        }
        for (int row = 1; row <= size; row++) {
            if (row == i) {
                continue;
            }
            if (this.board[row][j] == currentValue && this.board[row][j] > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rekurencyjna metoda rozwiązująca planszę metodą backtrackingu.
     * Próbuje wstawiać liczby 1-size i sprawdza ich poprawność.
     * @param i aktualny wiersz
     * @param j aktualna kolumna
     */
    public void solve(int i, int j) {
        if (i == size + 1 && j == size + 1) {
            if (skyscrapersCorrect()) {
                System.out.println("Rozwiązanie: ");
                System.out.println(this);
                boolean allSame = true;
                for (int x = 1; x <= size; x++) {
                    for (int y = 1; y <= size; y++) {
                        if (this.board[x][y] != this.boardToCheck[x][y]) {
                            allSame = false;
                        }
                    }
                }
                if (allSame) winnerBoard = true;
            }
            return;
        }
        for (int liczba = 1; liczba <= size; liczba++) {
            this.board[i][j] = liczba;
            if (checkForDuplicatesRowAndCol(i,j)) {
                if (i == size && j == size) {
                    solve(i + 1, j + 1);
                }
                else if (j + 1 <= size) {
                    solve(i, j + 1);
                } else {
                    solve(i + 1, 1);
                }
            }
            this.board[i][j] = 0;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size + 2; i++) {
            if (i == 1 || i == size + 1) {
                sb.append("---------------\n");
            }
            for (int j = 0; j < size + 2; j++) {
                if (j == 1 || j == size + 1) {
                    sb.append("| ");
                }
                if ((i == 0 && j == 0) || (i == 0 && j == size + 1) || (i == size + 1 && j == 0) || (i == size + 1 && j == size + 1)) {
                    sb.append("  ");
                    continue;
                }
                sb.append(this.board[i][j]).append(" ");
            }
            if (i < size + 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
