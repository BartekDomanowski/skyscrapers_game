import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SkyscrapersGameGUI extends JFrame {

    private static final int MIN_WINDOW_SIZE = 600;

    private static final int FONT_SIZE_TITLE = 32;
    private static final int FONT_SIZE_BUTTON = 32;
    private static final int FONT_SIZE_MEDIUM = 30;
    private static final int FONT_SIZE_CELL = 24;
    private static final String FONT_FAMILY = "Segoe UI Black";
    private static final String BACKGROUND_IMAGE_PATH = "resources/skyscrapers_game_visual.png";
    private static final String GAME_BACKGROUND_IMAGE_PATH = "resources/skyscrapers_lobby.png";

    private static final Color COLOR_VALID_MOVE = new Color(0, 128, 0);
    private static final Color COLOR_INVALID_MOVE = new Color(255, 0, 0);
    private static final Color COLOR_DEFAULT = Color.BLACK;

    private int[][] inputGameBoard; // Tablica przechowująca stan gry jako inty
    private int[][] boardToOpen; //Przechowuje tablice z otwierania
    private int size; // Rozmiar planszy
    private Difficulty difficulty; // Trudność planszy
    private JTextField[][] textFields; // Pola tekstowe reprezentujące planszę
    private JPanel gamePanel; // Panel zawierający planszę
    private List<int[][]> listOfBoards = new ArrayList<>(); // Historia ruchów (undo)
    private List<int[][]> redoBoards = new ArrayList<>(); // Historia cofniętych ruchów (redo)
    private boolean isUpdating = false;

    private long startTime;
    private long accumulatedTime;
    private boolean isTimerRunning;
    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    /**
     * Rozpoczyna grę od wejścia do "Lobby" zdefiniowanego w metodzie chooseGame
     * @throws IOException
     */
    public SkyscrapersGameGUI() throws IOException {
        // Ustawienie ikony okna (działa na Windows i macOS)
        if (Game.appIconImage != null) {
            setIconImage(Game.appIconImage);
        }
        chooseGame();
    }

    /**
     * Wczytuje ikonę z podanej ścieżki pliku.
     * @param path ścieżka do pliku z ikoną
     * @return obiekt ImageIcon lub null w przypadku błędu
     */
    private ImageIcon loadIcon(String path) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            return new ImageIcon(path);
        } else {
            System.err.println("Nie znaleziono pliku: " + path);
            return null;
        }
    }
    /**
     * Ustawia domyślny rozmiar okna, minimalne wymiary oraz wyśrodkowuje je na ekranie.
     * Dodaje również listener zapobiegający zmniejszeniu okna poniżej ustalonego minimum.
     */
    private void setDefaultGUISize() {
        setSize(MIN_WINDOW_SIZE, MIN_WINDOW_SIZE);
        setMinimumSize(new Dimension(MIN_WINDOW_SIZE, MIN_WINDOW_SIZE)); // Minimalny rozmiar okna
        setLocationRelativeTo(null); // Wyśrodkowanie okna
        setVisible(true);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Dimension newSize = componentEvent.getComponent().getSize();
                int width = Math.max(newSize.width, 600);
                int height = Math.max(newSize.height, 600);
                setSize(width, height);
            }
        });
    }

    /**
     * Sprawdza, czy ruch (wstawienie wartości) jest poprawny w danym wierszu i kolumnie.
     * @param row indeks wiersza
     * @param col indeks kolumny
     * @param value wstawiana wartość
     * @return true jeśli ruch jest poprawny, false w przeciwnym razie
     */
    private boolean isValidMove(int row, int col, int value) {
        if (value == 0) return true;

        for (int j = 1; j <= size; j++) {
            if (j != col && inputGameBoard[row][j] == value) {
                return false;
            }
        }

        for (int i = 1; i <= size; i++) {
            if (i != row && inputGameBoard[i][col] == value) {
                return false;
            }
        }

        return true;
    }

    /**
     * Waliduje komórkę i aktualizuje kolory na całej planszy.
     * @param row indeks wiersza
     * @param col indeks kolumny
     * @param textField pole tekstowe reprezentujące komórkę
     */
    private void validateAndColorCell(int row, int col, JTextField textField) {
        refreshAllColors();
    }

    /**
     * Liczy liczbę widocznych budynków w danym ciągu wartości (wiersz lub kolumna).
     * @param values tablica wysokości budynków
     * @return liczba widocznych budynków lub -1 jeśli ciąg zawiera puste pola (0)
     */
    private int countVisibleBuildings(int[] values) {
        int visible = 0;
        int maxHeight = 0;
        boolean hasEmpty = false;
        for (int value : values) {
            if (value == 0) {
                hasEmpty = true;
            } else if (value > maxHeight) {
                maxHeight = value;
                visible++;
            }
        }
        return hasEmpty ? -1 : visible;
    }

    /**
     * Liczy liczbę aktualnie widocznych budynków, ignorując puste pola.
     * Używane do sprawdzania warunków w trakcie gry.
     * @param values tablica wysokości budynków
     * @return liczba widocznych budynków
     */
    private int countCurrentlyVisible(int[] values) {
        int visible = 0;
        int maxHeight = 0;
        for (int value : values) {
            if (value > 0 && value > maxHeight) {
                maxHeight = value;
                visible++;
            }
        }
        return visible;
    }

    /**
     * Sprawdza, czy podpowiedź (liczba widocznych budynków) jest spełniona dla danej linii.
     * @param clue wartość podpowiedzi
     * @param values tablica wysokości budynków w linii
     * @return true jeśli podpowiedź jest spełniona lub jeszcze możliwa do spełnienia
     */
    private boolean isClueValid(int clue, int[] values) {
        if (clue == 0) return true;

        int visible = countVisibleBuildings(values);
        if (visible == -1) {
            int currentVisible = countCurrentlyVisible(values);
            return currentVisible <= clue;
        }
        return visible == clue;
    }

    /**
     * Odwraca kolejność elementów w tablicy intów.
     * @param arr tablica wejściowa
     * @return nowa tablica z odwróconą kolejnością
     */
    private int[] reverseArray(int[] arr) {
        int[] reversed = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            reversed[i] = arr[arr.length - 1 - i];
        }
        return reversed;
    }

    /**
     * Sprawdza poprawność wszystkich podpowiedzi na planszy i koloruje je odpowiednio.
     * Ustawia kolor zielony dla spełnionych warunków i czerwony dla niespełnionych.
     */
    private void validateAllClues() {

        for (int i = 1; i <= size; i++) {
            int[] row = new int[size];
            for (int j = 1; j <= size; j++) {
                row[j - 1] = inputGameBoard[i][j];
            }

            int leftClue = inputGameBoard[i][0];
            if (leftClue != 0 && textFields[i][0] != null) {
                boolean leftValid = isClueValid(leftClue, row);
                textFields[i][0].setForeground(leftValid ? COLOR_DEFAULT : COLOR_INVALID_MOVE);
            }

            int rightClue = inputGameBoard[i][size + 1];
            if (rightClue != 0 && textFields[i][size + 1] != null) {
                int[] reversedRow = reverseArray(row);
                boolean rightValid = isClueValid(rightClue, reversedRow);
                textFields[i][size + 1].setForeground(rightValid ? COLOR_DEFAULT : COLOR_INVALID_MOVE);
            }
        }

        for (int j = 1; j <= size; j++) {
            int[] col = new int[size];
            for (int i = 1; i <= size; i++) {
                col[i - 1] = inputGameBoard[i][j];
            }

            int topClue = inputGameBoard[0][j];
            if (topClue != 0 && textFields[0][j] != null) {
                boolean topValid = isClueValid(topClue, col);
                textFields[0][j].setForeground(topValid ? COLOR_DEFAULT : COLOR_INVALID_MOVE);
            }

            int bottomClue = inputGameBoard[size + 1][j];
            if (bottomClue != 0 && textFields[size + 1][j] != null) {
                int[] reversedCol = reverseArray(col);
                boolean bottomValid = isClueValid(bottomClue, reversedCol);
                textFields[size + 1][j].setForeground(bottomValid ? COLOR_DEFAULT : COLOR_INVALID_MOVE);
            }
        }
    }

    /**
     * Przyjmuje poniżej opisane parametry i ustawia tło na takie jak plik graficzny oraz umieszcza podpis
     * oraz przyciski w danym oknie
     * @param filename plik graficzny, który jest ustawiany jako tło
     * @param topPanel napis na górze danego okienka
     * @param listOfButtons lista przycisków niezbędnych w danym oknie
     */
    private void createBackground(String filename, JPanel topPanel, JButton[] listOfButtons) {
        BackgroundPanel p = new BackgroundPanel(java.awt.Toolkit.getDefaultToolkit().getImage(filename));
        
        // Panel łączący tytuł i przyciski
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Dodaj mały odstęp od góry (pod logo Skyscrapers)
        centerPanel.add(Box.createVerticalStrut(20));
        
        // Tytuł wyśrodkowany
        topPanel.setBackground(new Color(255, 255, 255, 0));
        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(topPanel);
        
        // Mały odstęp między tytułem a przyciskami
        centerPanel.add(Box.createVerticalStrut(10));
        
        // Panel przycisków
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        for (JButton current : listOfButtons) {
            current.setAlignmentX(Component.CENTER_ALIGNMENT);
            current.setMinimumSize(current.getPreferredSize());
            current.setMaximumSize(current.getPreferredSize());
            buttons.add(current);
            buttons.add(Box.createVerticalStrut(15)); // Odstęp między przyciskami
        }
        
        centerPanel.add(buttons);
        
        // Wyśrodkuj cały panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1;
        p.add(centerPanel, gbc);
        
        add(p);
        repaint();
        revalidate();
    }
    /**
     *  Metoda, która definiuje początek gry, czyli wybór skąd (plik czy tworzenie nowej) ma być wzięta pierwsza plansza
     */
    /**
     *  Metoda, która definiuje początek gry, czyli wybór skąd (plik czy tworzenie nowej) ma być wzięta pierwsza plansza
     */
    private void chooseGame() {
        getContentPane().removeAll();
        setTitle("Skyscrapers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        NeonLabel gameLabel = new NeonLabel("Wybierz grę");
        gameLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE));
        gameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(gameLabel, BorderLayout.CENTER);
        JButton newGameButton = createMenuButton("Nowa gra", e -> {
            repaint();
            try { difficultySelection(); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        JButton fileButton = createMenuButton("Wprowadź z pliku", e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("Desktop"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Skyscrapers Save (*.sky)", "sky"));
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(fileChooser.getSelectedFile()))) {
                    GameState state = (GameState) in.readObject();
                    size = state.getSize();
                    difficulty = state.getDifficulty();
                    boardToOpen = state.getBoard();
                    repaint();
                    generateGameBoard(true);
                    refreshAllColors();
                } catch (IOException | ClassNotFoundException | IncorrectBoardSizeException ex) {
                    JOptionPane.showMessageDialog(SkyscrapersGameGUI.this, "Błąd wczytywania pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        setDefaultGUISize();
        createBackground(BACKGROUND_IMAGE_PATH, topPanel, new JButton[]{newGameButton, fileButton});
    }
    /**
     * Metoda definiuje wygląd okna z wyborem poziomu trudności
     * @throws IOException
     */
    private void difficultySelection() throws IOException {
        getContentPane().removeAll();
        setTitle("Skyscrapers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        NeonLabel difficultyLabel = new NeonLabel("Wybierz poziom trudności");
        difficultyLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE));
        difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(difficultyLabel, BorderLayout.CENTER);
        JButton easyButton = createMenuButton("Łatwy", e -> { difficulty = Difficulty.EASY; repaint(); initializeSizeSelection(); });
        JButton hardButton = createMenuButton("Trudny", e -> { difficulty = Difficulty.HARD; repaint(); initializeSizeSelection(); });
        setDefaultGUISize();
        createBackground(BACKGROUND_IMAGE_PATH, topPanel, new JButton[]{easyButton, hardButton});
    }
    /**
     * Metoda definiuje okno z wyborem rozmiaru planszy
     */
    private void initializeSizeSelection() {
        getContentPane().removeAll();
        setTitle("Wybór rozmiaru");
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        NeonLabel sizeLabel = new NeonLabel("Wybierz rozmiar planszy");
        sizeLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE));
        sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(sizeLabel, BorderLayout.CENTER);
        JButton size3x3 = createMenuButton("3x3", e -> startGame(3));
        JButton size4x4 = createMenuButton("4x4", e -> startGame(4));
        JButton size5x5 = createMenuButton("5x5", e -> startGame(5));
        setDefaultGUISize();
        createBackground(BACKGROUND_IMAGE_PATH, topPanel, new JButton[]{size3x3, size4x4, size5x5});
    }
    
    /**
     * Uruchamia nową grę o zadanym rozmiarze planszy. Generuje nową planszę i odświeża widok.
     * @param boardSize rozmiar planszy (np. 3, 4, 5)
     */
    private void startGame(int boardSize) {
        size = boardSize;
        try { repaint(); generateGameBoard(false); } catch (IncorrectBoardSizeException ex) { throw new RuntimeException(ex); }
    }
    
    /**
     * Tworzy stylizowany przycisk menu z podanym tekstem i akcją.
     * @param text tekst na przycisku
     * @param action akcja do wykonania po kliknięciu
     * @return skonfigurowany obiekt JButton
     */
    private JButton createMenuButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new SemiTransparentButton(text);
        btn.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BUTTON));
        btn.setPreferredSize(new Dimension(400, 70));
        btn.addActionListener(action);
        return btn;
    }
    /**
     * Metoda wykorzystywana do kopiowania aktualnej zwartości tablicy 'inputGameBoard'
     * @return zwraca nową tablicę z przekopiowaną zawartością tablicy 'inputGameBoard'
     */
    private int[][] copyBoard() {
        int[][] copy = new int[inputGameBoard.length][];
        for (int i = 0; i < inputGameBoard.length; i++) {
            copy[i] = new int[inputGameBoard[i].length];
            System.arraycopy(inputGameBoard[i], 0, copy[i], 0, inputGameBoard[i].length);
        }
        return copy;
    }
    /**
     * Funkcja generująca pierwszą planszę widzianą przez użytkownika
     * Ma za zadanie wyświetlić okno z aktualną rozgrywką oraz kontrolować jej przebieg
     * @param alreadyGenerated zmienna kontrolująca czy należy wygenrować planszę od zera, czy może użytkownik chce ją odtworzyć z pliku
     * @throws IncorrectBoardSizeException
     */
    private void generateGameBoard(boolean alreadyGenerated) throws IncorrectBoardSizeException {
        getContentPane().removeAll(); // Usuwa wszystkie komponenty z poprzedniego okna
        setTitle("Skyscrapers");

        ImageIcon bgIcon = loadIcon(GAME_BACKGROUND_IMAGE_PATH);
        Image bgImage = bgIcon != null ? bgIcon.getImage() : null;
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setBackgroundType(BackgroundPanel.SCALED);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);
        textFields = new JTextField[size + 2][size + 2];
        inputGameBoard = new int[size + 2][size + 2];

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.setOpaque(false);

        timerLabel = new SemiTransparentLabel("Czas: 00:00");
        timerLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_MEDIUM));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton stopTimerButton = new SemiTransparentButton("Stop");
        JButton startTimerButton = new SemiTransparentButton("Start");

        stopTimerButton.addActionListener(e -> stopTimerLogic());

        startTimerButton.addActionListener(e -> startTimerLogic());

        timerPanel.add(startTimerButton);
        timerPanel.add(timerLabel);
        timerPanel.add(stopTimerButton);
        add(timerPanel, BorderLayout.NORTH);

        accumulatedTime = 0;
        startTime = System.currentTimeMillis();
        isTimerRunning = true;

        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new javax.swing.Timer(1000, e -> updateTimerLabel());
        gameTimer.start();
        // Generowanie planszy gry
        gamePanel = new JPanel(new GridLayout(size + 2, size + 2));
        gamePanel.setOpaque(false);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Gra");
        JMenu editMenu = new JMenu("Edycja");
        JMenuItem newGame = new JMenuItem(new AbstractAction("Lobby") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = showNeonConfirmDialog("Czy na pewno chcesz porzucić obecną grę?", "Potwierdzenie");
                if (response == JOptionPane.YES_OPTION) {
                    getContentPane().removeAll();
                    revalidate();
                    repaint();
                    chooseGame();
                }
            }
        });
        JMenuItem endGame = new JMenuItem(new AbstractAction("Zakończ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = showNeonConfirmDialog("Czy na pewno chcesz zakończyć grę?", "Potwierdzenie");
                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0); // Zamyka aplikację
                }
            }
        });
        JMenuItem saveGame = new JMenuItem(new AbstractAction("Zapisz") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("Desktop"));
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Skyscrapers Save (*.sky)", "sky"));
                int response = fileChooser.showSaveDialog(null);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    // Dodaj rozszerzenie .sky jeśli brak
                    if (!file.getName().endsWith(".sky")) {
                        file = new File(file.getAbsolutePath() + ".sky");
                    }
                    try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                            new java.io.FileOutputStream(file))) {
                        GameState state = new GameState(inputGameBoard, size, difficulty);
                        out.writeObject(state);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        JMenuItem backMove = new JMenuItem(new AbstractAction("Cofnij ruch") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listOfBoards.size() > 1) {
                    int[][] undoneBoard = listOfBoards.removeLast();
                    redoBoards.add(undoneBoard);
                    backUpdate();
                }
            }
        });
        JMenuItem redoMove = new JMenuItem(new AbstractAction("Ponów ruch") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!redoBoards.isEmpty()) {
                    int[][] redoneBoard = redoBoards.removeLast();
                    listOfBoards.add(redoneBoard);
                    redoUpdate();
                }
            }
        });
        JMenuItem checkGame = new JMenuItem(new AbstractAction("Sprawdź rozwiązanie!") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Solver boardToCheck = new Solver(size,inputGameBoard);
                boardToCheck.run();
                if (boardToCheck.isWinnerBoard()) {
                    stopTimerLogic();
                    String timeStr = getElapsedTimeString();
                    showNeonMessageDialog("Udało ci się rozwiązać w: " + timeStr, "Gratulacje!", true);
                } else {
                    showNeonMessageDialog("Niestety, to nie jest poprawne rozwiązanie!", "Błędne rozwiązanie", false);
                }
            }
        });
        fileMenu.add(saveGame);
        fileMenu.add(newGame);
        fileMenu.add(endGame);
        editMenu.add(backMove);
        editMenu.add(redoMove);
        editMenu.add(checkGame);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        this.setJMenuBar(menuBar);
        Board board;
        if (!alreadyGenerated) {
            board = Generator.generate(size, difficulty);
        } else {
            board = new Board(size, difficulty, boardToOpen);
        }
        //tak wygląda cała wygenerowana plansza
        System.out.println(board);
        //wypełnienie planszy
        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                //tutaj wiadomo, że coś nie jest rogiem
                if (!((i == 0 || i == size + 1) && (j == 0 || j == size + 1))) {
                    JTextField textField = new SemiTransparentTextField(2);
                    textField.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_CELL));
                    textField.setHorizontalAlignment(JTextField.CENTER);
                    if (i == 0 || i == size + 1 || j == 0 || j == size + 1) {
                        int value = board.getSingleCellOfBoard(i, j);
                        inputGameBoard[i][j] = value;
                        setTextFieldState(i,j,value);
                        if (value == 0) {
                            textField.setText("");
                        } else {
                            textField.setText(Integer.toString(value));
                        }
                        textField.setEditable(false);
                    } else {
                        if (!alreadyGenerated) {
                            inputGameBoard[i][j] = 0; // Inicjalizacja pustymi znakami
                            setTextFieldState(i, j, 0);
                        } else {
                            int value = board.getSingleCellOfBoard(i, j);
                            inputGameBoard[i][j] = value;
                            setTextFieldState(i, j, value);
                            if (value == 0) {
                                textField.setText("");
                            } else {
                                textField.setText(Integer.toString(value));
                            }
                        }
                        // Dodanie filtru dokumentu do pola tekstowego
                        PlainDocument doc = (PlainDocument) textField.getDocument();
                        doc.setDocumentFilter(new DocumentFilter() {
                            @Override
                            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                                String regex = "[1-" + size + "]";
                                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                                currentText += text; // Aktualny tekst w polu tekstowym
                                if (currentText.length() <= 1 && text.matches(regex)) {
                                    super.replace(fb, offset, length, text, attrs);
                                }
                            }
                        });
                        final int row = i;
                        final int col = j;
                        textField.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(row, col, textField);
                            }
                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(row, col, textField);
                            }
                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(row, col, textField);
                            }
                        });
                    }
                    gamePanel.add(textField);
                    textFields[i][j] = textField;
                } else {
                    gamePanel.add(new JLabel());
                }
            }
        }
        //Pierwsza pusta kopia planszy
        int[][] copy = copyBoard();
        listOfBoards.add(copy);
        //Opis przycisku Sprawdź rozwiązanie
        JButton checkButton = new SemiTransparentButton("Sprawdź rozwiązanie!");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Solver boardToCheck = new Solver(size, inputGameBoard);
                boardToCheck.run();
                if (boardToCheck.isWinnerBoard()) {
                    stopTimerLogic();
                    String timeStr = getElapsedTimeString();
                    showNeonMessageDialog("Udało ci się rozwiązać w: " + timeStr, "Gratulacje!", true);
                } else {
                    showNeonMessageDialog("Niestety, to nie jest poprawne rozwiązanie!", "Błędne rozwiązanie", false);
                }
            }
        });
        //Opis przycisku Cofnij ruch
        JButton backButton = new SemiTransparentButton("Cofnij ruch");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listOfBoards.size() > 1) {
                    int[][] undoneBoard = listOfBoards.removeLast();
                    redoBoards.add(undoneBoard);
                    backUpdate();
                }
            }
        });
        JButton redoButton = new SemiTransparentButton("Ponów ruch");
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!redoBoards.isEmpty()) {
                    int[][] redoneBoard = redoBoards.removeLast();
                    listOfBoards.add(redoneBoard);
                    redoUpdate();
                }
            }
        });
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // Add gaps
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        controlPanel.add(backButton);
        controlPanel.add(redoButton);
        controlPanel.add(checkButton);
        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        setDefaultGUISize();
    }
    /**
     * Metoda ta służy do przemalowania- odświeżenia planszy po cofnięciu ruchu
     */
    private void paintBoard() {
        gamePanel.removeAll();
        int value;
        for (int i = 0; i <= size + 1; i++) {
            for (int j = 0; j <= size + 1; j++) {
                if (!((i == 0 || i == size + 1) && (j == 0 || j == size + 1))) {
                    JTextField label = new SemiTransparentTextField(2);
                    label.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_CELL));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    JTextField textField = textFields[i][j];
                    String text = textField.getText();
                    value = 0;
                    if (!text.isEmpty()) {
                        value = Integer.parseInt(text);
                    }
                    if (i == 0 || i == size + 1 || j == 0 || j == size + 1) {
                        label.setEditable(false);
                        if (value == 0) {
                            label.setText("");
                        } else {
                            label.setText(String.valueOf(value));
                        }
                        textFields[i][j] = label;
                        gamePanel.add(label);
                    } else {
                        if (value == 0) {
                            label.setText("");
                        } else {
                            label.setText(String.valueOf(value));
                        }
                        PlainDocument doc = (PlainDocument) label.getDocument();
                        doc.setDocumentFilter(new DocumentFilter() {
                            @Override
                            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                                String regex = "[1-" + size + "]";
                                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                                currentText += text; // Aktualny tekst w polu tekstowym
                                if (currentText.length() <= 1 && text.matches(regex)) {
                                    super.replace(fb, offset, length, text, attrs);
                                }
                            }
                        });
                        int finalI = i;
                        int finalJ = j;
                        label.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                textFields[finalI][finalJ] = label;
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(finalI, finalJ, label);
                            }
                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                textFields[finalI][finalJ] = label;
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(finalI, finalJ, label);
                            }
                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                updateInputGameBoardAndListOfBoards();
                                validateAndColorCell(finalI, finalJ, label);
                            }
                        });
                        textFields[i][j] = label;
                        gamePanel.add(label);
                    }
                } else {
                    gamePanel.add(new JLabel());
                }
            }
        }
        gamePanel.revalidate();
        gamePanel.repaint();
    }
    /**
     * Funkcja ta refreshuje aktualną planszę gry, gdy użytkownik klinkie przycisk "Cofnij ruch"
     */
    private void backUpdate() {
        isUpdating = true;
        try {
            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    if (listOfBoards.getLast()[i][j] != inputGameBoard[i][j]) {
                        inputGameBoard[i][j] = listOfBoards.getLast()[i][j];
                        setTextFieldState(i,j,listOfBoards.getLast()[i][j]);
                    }
                }
            }
            paintBoard();
            refreshAllColors();
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Przywraca ostatnio cofnięty ruch z historii redo.
     */
    private void redoUpdate() {
        isUpdating = true;
        try {
            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    if (listOfBoards.getLast()[i][j] != inputGameBoard[i][j]) {
                        inputGameBoard[i][j] = listOfBoards.getLast()[i][j];
                        setTextFieldState(i, j, listOfBoards.getLast()[i][j]);
                    }
                }
            }
            paintBoard();
            refreshAllColors();
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Odświeża kolory wszystkich pól na planszy oraz sprawdza poprawność podpowiedzi.
     * Koloruje pola na zielono (poprawne) lub czerwono (błędne) w zależności od reguł gry.
     */
    private void refreshAllColors() {

        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                JTextField textField = textFields[i][j];
                if (textField != null) {
                    String text = textField.getText();
                    if (text.isEmpty()) {
                        textField.setForeground(COLOR_DEFAULT);
                    } else {
                        int value = Integer.parseInt(text);
                        if (isValidMove(i, j, value)) {
                            textField.setForeground(COLOR_VALID_MOVE);
                        } else {
                            textField.setForeground(COLOR_INVALID_MOVE);
                        }
                    }
                }
            }
        }
        validateAllClues();
    }

    /**
     * Zatrzymuje licznik czasu gry i aktualizuje zgromadzony czas.
     */
    private void stopTimerLogic() {
        if (isTimerRunning) {
            if (gameTimer != null) gameTimer.stop();
            accumulatedTime += System.currentTimeMillis() - startTime;
            isTimerRunning = false;
            updateTimerLabel();
        }
    }

    /**
     * Uruchamia licznik czasu gry, jeśli nie jest już uruchomiony.
     */
    private void startTimerLogic() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis();
            if (gameTimer != null) gameTimer.start();
            isTimerRunning = true;
        }
    }

    /**
     * Aktualizuje etykietę wyświetlającą czas gry.
     */
    private void updateTimerLabel() {
        if (timerLabel != null) {
            timerLabel.setText("Czas: " + getElapsedTimeString());
        }
    }

    /**
     * Oblicza całkowity czas gry sformatowany jako ciąg znaków MM:SS.
     * @return sformatowany czas gry
     */
    private String getElapsedTimeString() {
        long currentSegment = 0;
        if (isTimerRunning) {
            currentSegment = System.currentTimeMillis() - startTime;
        }
        long totalMillis = accumulatedTime + currentSegment;
        long totalSeconds = totalMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Pomocnicza metoda do wstawiania wartości do tablicy 'textFields'
     * @param x kolumna tablicy 'textFields'
     * @param y wiersz tablicy 'textFields'
     * @param val wartość, którą należ wstawić do tablicy 'textFields'
     */
    private void setTextFieldState(int x, int y, int val) {
        String textValue;
        if (val == 0) {
            textValue = "";
        }
        else {
            textValue = Integer.toString(val);
        }
        JTextField textField = new SemiTransparentTextField(2);
        textField.setText(textValue);
        textField.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_CELL));
        textFields[x][y] = textField;
    }
    // Metoda do aktualizacji zawartości tablicy 'InputGameBoard' oraz 'listOfBoards'
    // (Juz miała komentarz, ale dodajemy Javadoc dla spójności)
    /**
     * Aktualizuje stan gry w tablicy inputGameBoard na podstawie wartości wpisanych w polach tekstowych.
     * Zapisuje również historię zmian do obsługi undo/redo.
     */
    private void updateInputGameBoardAndListOfBoards() {
        if (isUpdating) return;
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                JTextField textField = textFields[i][j];
                String text = textField.getText();
                String regex = "[1-" + size + "]";
                int value = 0;
                if (!text.isEmpty() && text.matches(regex)) {
                    value = Integer.parseInt(text);
                }
                inputGameBoard[i][j] = value;
            }
        }
        int[][] copy = copyBoard();
        listOfBoards.add(copy);
        redoBoards.clear();
    }

    /**
     * Klasa reprezentująca etykietę z półprzezroczystym tłem.
     */
    private static class SemiTransparentLabel extends JLabel {
        private Color bgColor;

        public SemiTransparentLabel(String text) {
            super(text);
            setOpaque(false);
            bgColor = new Color(255, 255, 255, 180);
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            this.bgColor = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }

    /**
     * Klasa reprezentująca pole tekstowe z półprzezroczystym tłem i specjalnym stylem.
     */
    private static class SemiTransparentTextField extends JTextField {
        private Color bgColor;

        public SemiTransparentTextField(int cols) {
            super(cols);
            setOpaque(false);
            bgColor = new Color(255, 255, 255, 180); // Default alpha
            setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Custom border prevents opaque painting
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            this.bgColor = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Rysuj mały boxik tła pod cyfrą dla lepszej widoczności
            String text = getText();
            if (text != null && !text.isEmpty()) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                FontMetrics fm = g2d.getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                
                // Oblicz pozycję boxika (wyśrodkowany pod tekstem)
                int boxWidth = textWidth + 12;
                int boxHeight = textHeight + 4;
                int boxX = (getWidth() - boxWidth) / 2;
                int boxY = (getHeight() - boxHeight) / 2;
                
                // Rysuj mały biały boxik z większą widocznością
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 6, 6);
                
                g2d.dispose();
            }
            
            super.paintComponent(g);
        }
    }

    /**
     * Klasa reprezentująca etykietę z efektem neonowym i cieniem tekstu.
     * Służy do wyświetlania nagłówków i tytułów.
     */
    private static class NeonLabel extends JLabel {
        private Color outlineColor;
        
        public NeonLabel(String text) {
            super(text);
            setForeground(new Color(0, 255, 255)); // Neonowy cyjan
            outlineColor = new Color(0, 0, 0, 200); // Ciemny outline
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            String text = getText();
            FontMetrics fm = g2d.getFontMetrics(getFont());
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            
            g2d.setFont(getFont());
            
            // Rysuj outline (cień) w kilku kierunkach
            g2d.setColor(outlineColor);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
            }
            
            // Rysuj główny tekst
            g2d.setColor(getForeground());
            g2d.drawString(text, x, y);
            
            g2d.dispose();
        }
    }

    /**
     * Klasa reprezentująca przycisk z półprzezroczystym tłem, zaokrąglonymi rogami i neonową ramką.
     */
    private static class SemiTransparentButton extends JButton {
        private Color bgColor;
        private Color textColor;

        public SemiTransparentButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            bgColor = new Color(20, 10, 50, 220); // Ciemne tło
            textColor = new Color(0, 255, 255); // Neonowy cyjan
            setForeground(textColor);
            setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            this.bgColor = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2d.setColor(new Color(60, 20, 100, 240));
            } else if (getModel().isRollover()) {
                g2d.setColor(new Color(40, 15, 80, 240));
            } else {
                g2d.setColor(bgColor);
            }
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            
            // Neonowa ramka
            g2d.setColor(new Color(0, 255, 255, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Wyświetla niestandardowe okno dialogowe z potwierdzeniem akcji, stylizowane na neonowo.
     * @param message treść wiadomości
     * @param title tytuł okna
     * @return wybrana opcja (JOptionPane.YES_OPTION lub JOptionPane.NO_OPTION)
     */
    private int showNeonConfirmDialog(String message, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(15, 5, 30, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(0, 255, 255, 150));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Tytuł
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 0, 255)); // Magenta
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Wiadomość
        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 20));
        msgLabel.setForeground(new Color(0, 255, 255)); // Cyjan
        mainPanel.add(msgLabel, BorderLayout.CENTER);
        
        // Przyciski
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        final int[] result = {-1};
        
        SemiTransparentButton yesBtn = new SemiTransparentButton("Tak");
        yesBtn.setPreferredSize(new Dimension(120, 50));
        yesBtn.addActionListener(e -> { result[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
        
        SemiTransparentButton noBtn = new SemiTransparentButton("Nie");
        noBtn.setPreferredSize(new Dimension(120, 50));
        noBtn.addActionListener(e -> { result[0] = JOptionPane.NO_OPTION; dialog.dispose(); });
        
        buttonPanel.add(yesBtn);
        buttonPanel.add(noBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(mainPanel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setVisible(true);
        
        return result[0];
    }
    
    /**
     * Wyświetla niestandardowe okno dialogowe z informacją, stylizowane na neonowo.
     * @param message treść wiadomości
     * @param title tytuł okna
     * @param isSuccess określa czy wiadomość jest o sukcesie (zielony) czy błędzie (czerwony)
     */
    private void showNeonMessageDialog(String message, String title, boolean isSuccess) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(15, 5, 30, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                Color borderColor = isSuccess ? new Color(0, 255, 100, 150) : new Color(255, 50, 50, 150);
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Tytuł
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 28));
        titleLabel.setForeground(isSuccess ? new Color(0, 255, 100) : new Color(255, 50, 50));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Wiadomość
        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 20));
        msgLabel.setForeground(new Color(0, 255, 255));
        mainPanel.add(msgLabel, BorderLayout.CENTER);
        
        // Przycisk OK
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        SemiTransparentButton okBtn = new SemiTransparentButton("OK");
        okBtn.setPreferredSize(new Dimension(150, 50));
        okBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(mainPanel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setVisible(true);
    }
}