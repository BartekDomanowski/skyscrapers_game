/**
 * Wyjątek rzucany, gdy rozmiar planszy jest nieprawidłowy (np. spoza dozwolonego zakresu).
 */
public class IncorrectBoardSizeException extends Exception {
    /**
     * Tworzy nowy wyjątek z podaną wiadomością błędu.
     * @param message wiadomość opisująca błąd
     */
    public IncorrectBoardSizeException(String message) {
        super(message);
    }
}
