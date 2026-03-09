import java.util.*;

/**
 * Klasa finalna odpowiedzialna za generowanie plansz do gry Skyscrapers.
 * Zawiera metody do tworzenia losowych układów oraz generowania i ukrywania podpowiedzi.
 */
public final class Generator {
    /**
     * Odwraca kolejność elementów w tablicy.
     * @param o tablica wejściowa
     * @return odwrócona tablica
     */
    public static int[] reverse(int[] o){
        for(int i = 0; i < o.length / 2; i++)
        {
            int temp = o[i];
            o[i] = o[o.length - i - 1];
            o[o.length - i - 1] = temp;
        }
        return o;
    }
    /**
     * Zwraca zbiór liczb możliwych do wstawienia na danej pozycji, uwzględniając reguły Sudoku (unikalność w wierszu i kolumnie).
     * @param size rozmiar planszy
     * @param row aktualny wiersz
     * @param column aktualna kolumna
     * @return zbiór dostępnych liczb
     */
    private static Set<Integer> getPossibleNumbers(int size,int[] row, int[] column){
        Set<Integer> posibleNumbers = new TreeSet<Integer>();
        for(int i=0; i<size;i++){
            posibleNumbers.add(Integer.valueOf(i+1));
        }
        for(int i: row){
            posibleNumbers.remove(Integer.valueOf(i));
        }
        for(int i: column){
            posibleNumbers.remove(Integer.valueOf(i));
        }
        return posibleNumbers;
    }
    /**
     * Zwraca losowy element ze zbioru liczb.
     * @param possibleNumbers zbiór liczb
     * @return wylosowana liczba lub 0, jeśli zbiór jest pusty
     */
    private static int getRandomElement(Set<Integer> possibleNumbers){
        if(possibleNumbers.isEmpty()){
            return 0;
        }
        List<Integer> list = new ArrayList<>(possibleNumbers);
        return list.get(new Random().nextInt(list.size()));
    }
    /**
     * Generuje wypełniony środek planszy (bez podpowiedzi).
     * @param size rozmiar planszy
     * @return obiekt Board z wypełnioną planszą
     * @throws IncorrectBoardSizeException jeśli rozmiar jest nieprawidłowy
     */
    private static Board generateBoard(int size) throws IncorrectBoardSizeException { //generuje środek planszy
        Board b = new Board(size);
        Set<Integer> possibleNumbers;
        int value;
        boolean foundZero = true;
        for(int i=0;i<size;i++){ //przechodzi po kolei po każdym wierszu
            foundZero = true;
            while(foundZero) {  //jeżeli w którymś wierszu pojawi się 0 (czyli nie ma żadnych możliwych liczb do wstawienia) to generowanie wiersza jest powtarzane
                foundZero = false;
                for (int j = 0; j < size; j++) { //generowanie wierszy
                    possibleNumbers = getPossibleNumbers(size, b.getRowOfOnlyBoard(i), b.getColumnOfOnlyBoard(j));
                    value = getRandomElement(possibleNumbers);
                    b.setSingleCellOfOnlyBoard(i, j, value);
                    if(value==0){
                        foundZero= true;
                        for(int k=0;k<size;k++) { //kasowanie błędnego wiersza
                            b.setSingleCellOfOnlyBoard(i, k, 0);
                        }
                        break;
                    }
                }
            }
        }
        return b;
    }
    /**
     * Generuje podpowiedzi na krawędziach planszy na podstawie widoczności budynków.
     * Dodatkowo ukrywa część podpowiedzi w trybie trudnym.
     * @param b plansza z wypełnionym środkiem
     * @param difficulty poziom trudności
     * @return nowa plansza z wygenerowanymi podpowiedziami
     * @throws IncorrectBoardSizeException
     */
    private static Board generateClues(Board b,Difficulty difficulty) throws IncorrectBoardSizeException {
        Board newBoard = new Board(b.getSize(), difficulty, b.getBoardWithClues());
        int[] list;
        int[] clues = new int[b.getSize() + 2];
        int visible;
        int previousBiggest;
        int ammountOfHiddenNumbers = 0;
        for (int direction = 0; direction < 4; direction++) {
            for (int i = 0; i < b.getSize(); i++) {
                switch (direction) { //pobieranie odpowiedniego rzędu/kolumny do generowania podpowiedzi
                    case 0:
                        list = b.getColumnOfOnlyBoard(i);
                        break;
                    case 1:
                        list = b.getColumnOfOnlyBoard(i);
                        list = reverse(list);
                        break;
                    case 2:
                        list = b.getRowOfOnlyBoard(i);
                        break;
                    case 3:
                        list = b.getRowOfOnlyBoard(i);
                        list = reverse(list);
                        break;
                    default:
                        list = b.getColumnOfOnlyBoard(i);
                }
                visible = 0;
                previousBiggest = 0;
                for (int j = 0; j < list.length; j++) { //generowanie podpowiedzi
                    if (list[j] > previousBiggest) {
                        visible++;
                        previousBiggest = list[j];
                    }
                }
                clues[i] = visible;
            }

            if(difficulty == Difficulty.HARD){ //ukrywanie podpowiedzi
                ammountOfHiddenNumbers = new Random().nextInt(1, b.getSize() - 1);
                clues = hideClues(clues, ammountOfHiddenNumbers);
            }

            switch (direction){ //ustawianie podpowiedzi w zależności od kierunku
                case(0):
                    newBoard.setTopClues(clues);
                    break;
                case(1):
                    newBoard.setDownClues(clues);
                    break;
                case(2):
                    newBoard.setLeftClues(clues);
                    break;
                case(3):
                    newBoard.setRightClues(clues);
                    break;
            }
        }
        return newBoard;
    }
    /**
     * Ukrywa losowe podpowiedzi, zastępując je wartością 0.
     * @param clues tablica podpowiedzi
     * @param ammountOfHiddenNumbers liczba podpowiedzi do ukrycia
     * @return tablica z częściowo ukrytymi podpowiedziami
     */
    private static int[] hideClues(int[] clues,int ammountOfHiddenNumbers){
        TreeSet<Integer> indexesToHide = new TreeSet<>();
        while (indexesToHide.size()<ammountOfHiddenNumbers){ //dodajemy do zbioru losowe indexy aż będzie ich tyle ile chcemy liczb ukryć
            indexesToHide.add(new Random().nextInt(1,clues.length-1));
        }
        for(int i =0;i<clues.length;i++){ //ukrywamy liczby o wylosowanych indeksach
            if(indexesToHide.contains(Integer.valueOf(i))) {
                clues[i-1] = 0;
            }
        }
        return clues;
    }

    /**
     * Główna metoda generująca kompletną planszę gry (środek + podpowiedzi).
     * @param size rozmiar planszy
     * @param difficulty poziom trudności
     * @return gotowa plansza
     * @throws IncorrectBoardSizeException
     */
    public static Board generate(int size, Difficulty difficulty) throws IncorrectBoardSizeException {
        Board br = generateBoard(size);
        br = generateClues(br, difficulty);
        return br;
    }
}
