import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    @Test
    public void testConstructorWithSizeAndDifficulty() throws IncorrectBoardSizeException {
        int[][] boardWithClues = {
                {0, 1, 2, 3, 0},
                {4, 0, 0, 0, 5},
                {3, 0, 0, 0, 2},
                {2, 0, 0, 0, 3},
                {0, 1, 2, 3, 0}
        };
        Board board = new Board(3, Difficulty.EASY, boardWithClues);

        assertEquals(3, board.getSize());
        assertArrayEquals(new int[]{0, 0, 0}, board.getOnlyBoard()[0]);
    }

    @Test
    public void testConstructorWithInvalidSize() {
        assertThrows(IncorrectBoardSizeException.class, () -> {
            new Board(6, Difficulty.EASY, new int[8][8]);
        });
    }

    @Test
    public void testSetTopClues() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        int[] clues = {1, 2, 3};
        board.setTopClues(clues);

        int[] expectedClues = {0, 1, 2, 3, 0};
        assertArrayEquals(expectedClues, board.getBoardWithClues()[0]);
    }

    @Test
    public void testSetDownClues() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        int[] clues = {1, 2, 3};
        board.setDownClues(clues);

        int[] expectedClues = {0, 1, 2, 3, 0};
        assertArrayEquals(expectedClues, board.getBoardWithClues()[4]);
    }

    @Test
    public void testSetLeftClues() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        int[] clues = {1, 2, 3};
        board.setLeftClues(clues);

        assertEquals(1, board.getBoardWithClues()[1][0]);
        assertEquals(2, board.getBoardWithClues()[2][0]);
        assertEquals(3, board.getBoardWithClues()[3][0]);
    }

    @Test
    public void testSetRightClues() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        int[] clues = {1, 2, 3};
        board.setRightClues(clues);

        assertEquals(1, board.getBoardWithClues()[1][4]);
        assertEquals(2, board.getBoardWithClues()[2][4]);
        assertEquals(3, board.getBoardWithClues()[3][4]);
    }

    @Test
    public void testSetSingleCellOfOnlyBoard() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        board.setSingleCellOfOnlyBoard(1, 1, 5);

        assertEquals(5, board.getOnlyBoard()[1][1]);
        assertEquals(5, board.getBoardWithClues()[2][2]);
    }

    @Test
    public void testGetRowOfOnlyBoard() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        board.setSingleCellOfOnlyBoard(0, 0, 1);
        board.setSingleCellOfOnlyBoard(0, 1, 2);
        board.setSingleCellOfOnlyBoard(0, 2, 3);

        assertArrayEquals(new int[]{1, 2, 3}, board.getRowOfOnlyBoard(0));
    }

    @Test
    public void testGetColumnOfOnlyBoard() throws IncorrectBoardSizeException {
        Board board = new Board(3);
        board.setSingleCellOfOnlyBoard(0, 0, 1);
        board.setSingleCellOfOnlyBoard(1, 0, 2);
        board.setSingleCellOfOnlyBoard(2, 0, 3);

        assertArrayEquals(new int[]{1, 2, 3}, board.getColumnOfOnlyBoard(0));
    }
}