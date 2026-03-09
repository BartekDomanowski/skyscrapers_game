import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SolverTest {
    private int[][] boardToCheck;

    @BeforeEach
    public void setUp() {
        // Przygotowanie planszy do testów
        boardToCheck = new int[][] {
                {0, 3, 0, 0, 2, 0},
                {2, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 3, 0}
        };
    }
    @Test
    public void testConstructorWithSolutionCheck() {
        Solver solver = new Solver(4, boardToCheck);
        assertNotNull(solver.getBoard());
        assertEquals(4, solver.getSize());
    }

    @Test
    public void testConstructorSolveOnly() {
        Solver solver = new Solver(4, boardToCheck, true);
        assertNotNull(solver.getBoard());
        assertEquals(4, solver.getSize());
    }
    @Test
    public void testCheckForDuplicatesRowAndCol() {
        Solver solver = new Solver(4, boardToCheck);
        solver.getBoard()[1][1] = 1;
        solver.getBoard()[1][2] = 2;
        solver.getBoard()[2][1] = 3;
        solver.getBoard()[2][2] = 4;
        assertTrue(solver.checkForDuplicatesRowAndCol(1, 1));
        solver.getBoard()[1][2] = 1;
        assertFalse(solver.checkForDuplicatesRowAndCol(1, 2));
    }
}