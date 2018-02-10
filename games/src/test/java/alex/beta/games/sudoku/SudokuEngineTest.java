/**
 * <p>
 * File Name: SudokuEngineTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/10 下午10:27
 * </p>
 */
package alex.beta.games.sudoku;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuEngineTest {

    private static final int[][] initialData = new int[][]{//here is a good sudoku result
            {2, 7, 9, 4, 3, 8, 6, 5, 1},
            {5, 3, 8, 2, 1, 6, 4, 9, 7},
            {1, 6, 4, 5, 9, 7, 8, 2, 3},

            {8, 9, 2, 6, 4, 3, 7, 1, 5},
            {3, 4, 7, 1, 5, 9, 2, 6, 8},
            {6, 5, 1, 8, 7, 2, 9, 3, 4},

            {9, 2, 3, 7, 8, 5, 1, 4, 6},
            {7, 1, 6, 3, 2, 4, 5, 8, 9},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
            //{4, 8, 5, 9, 6, 1, 3, 7, 2}
    };

    private static final int[][] randomData = new int[][]{
            {0, 0, 0, 0, 7, 0, 0, 1, 0},
            {0, 0, 0, 4, 0, 0, 0, 0, 5},
            {1, 0, 3, 0, 0, 8, 0, 0, 0},

            {0, 0, 0, 0, 4, 1, 3, 0, 0},
            {4, 0, 8, 0, 0, 0, 0, 0, 0},
            {9, 0, 0, 0, 0, 0, 0, 0, 0},

            {0, 0, 0, 0, 0, 4, 0, 0, 0},
            {2, 0, 0, 7, 0, 0, 0, 0, 0},
            {0, 0, 4, 0, 0, 0, 0, 0, 6}
    };

    private SudokuEngine engine;

    @Before
    public void setUp() {
        engine = new SudokuEngine();
    }

    @After
    public void tearDown() {
        engine = null;
    }

    @Test
    public void testSetData() {
        engine.setData(randomData);
        assertEquals(18, engine.getTip());
    }

    @Test
    public void testSolveSudoku() {
        engine.setData(randomData);
        assertTrue(engine.solveSudo(false));
    }

    @Test
    public void testRandomSudoku() {
        engine.setTip(18);
        engine.genSudo();
        assertTrue(engine.solveSudo(false));

        int[][] result = engine.getData();
        assertTrue(SudokuValidator.getInstance().preview(result));
    }

    @Test
    public void testSolveSudoku2() {
        engine.setData(initialData);
        assertTrue(engine.solveSudo(false));
        int[] result = engine.getData()[8];
        assertArrayEquals(new int[]{4, 8, 5, 9, 6, 1, 3, 7, 2}, result);
    }
}
