import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;
public class GeneratorTest {
    @Test
    public void testReverse() {
        int[] input = {1, 2, 3, 4, 5};
        int[] expected = {5, 4, 3, 2, 1};
        assertArrayEquals(expected, Generator.reverse(input));

        int[] input2 = {1, 2, 3, 4};
        int[] expected2 = {4, 3, 2, 1};
        assertArrayEquals(expected2, Generator.reverse(input2));
    }
    @Test
    public void testGetPossibleNumbers() throws Exception {
        Method method = Generator.class.getDeclaredMethod("getPossibleNumbers", int.class, int[].class, int[].class);
        method.setAccessible(true);

        int size = 5;
        int[] row = {1, 2};
        int[] column = {3, 4};

        TreeSet<Integer> expected = new TreeSet<>();
        expected.add(5);
        Set<Integer> result = (Set<Integer>) method.invoke(null, size, row, column);
        assertEquals(expected, result);
    }

    @Test
    public void testGetRandomElement() throws Exception {
        Method method = Generator.class.getDeclaredMethod("getRandomElement", Set.class);
        method.setAccessible(true);

        TreeSet<Integer> possibleNumbers = new TreeSet<>();
        possibleNumbers.add(1);
        possibleNumbers.add(2);
        possibleNumbers.add(3);
        possibleNumbers.add(4);
        possibleNumbers.add(5);

        int result = (int) method.invoke(null, possibleNumbers);
        assertTrue(possibleNumbers.contains(result));
    }
    @Test
    public void testGenerateBoard() throws Exception {
        Method method = Generator.class.getDeclaredMethod("generateBoard", int.class);
        method.setAccessible(true);

        int size = 4;
        Board result = (Board) method.invoke(null, size);

        assertNotNull(result);
        assertEquals(size, result.getSize());
    }
    @Test
    public void testGenerateClues() throws Exception {
        Board board = new Board(4);
        Method method = Generator.class.getDeclaredMethod("generateClues", Board.class, Difficulty.class);
        method.setAccessible(true);
        Board result = (Board) method.invoke(null, board, Difficulty.EASY);

        assertNotNull(result);
        assertEquals(4, result.getSize());
    }
    @Test
    public void testHideClues() throws Exception {
        Method method = Generator.class.getDeclaredMethod("hideClues", int[].class, int.class);
        method.setAccessible(true);

        int[] clues = {1, 2, 3, 4, 5};
        int amountOfHiddenNumbers = 2;

        int[] result = (int[]) method.invoke(null, clues, amountOfHiddenNumbers);
        assertEquals(clues.length, result.length);
        long hiddenCount = Arrays.stream(result).filter(n -> n == 0).count();
        assertEquals(amountOfHiddenNumbers, hiddenCount);
    }

    @Test
    public void testGenerate() throws IncorrectBoardSizeException {
        Board result = Generator.generate(4, Difficulty.EASY);
        assertNotNull(result);
        assertEquals(4, result.getSize());
    }
}