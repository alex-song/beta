/**
 * <p>
 * File Name: SudokuValidatorTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/6 下午8:45
 * </p>
 */
package alex.beta.games.sudoku;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import alex.beta.games.sudoku.SudokuValidator.*;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuValidatorTest extends TestCase {
    private static final int[][] initialData = new int[][] {//here is a good sudoku result
            {2, 7, 9, 4, 3, 8, 6, 5, 1},
            {5, 3, 8, 2, 1, 6, 4, 9, 7},
            {1, 6, 4, 5, 9, 7, 8, 2, 3},

            {8, 9, 2, 6, 4, 3, 7, 1, 5},
            {3, 4, 7, 1, 5, 9, 2, 6, 8},
            {6, 5, 1, 8, 7, 2, 9, 3, 4},

            {9, 2, 3, 7, 8, 5, 1, 4, 6},
            {7, 1, 6, 3, 2, 4, 5, 8, 9},
            {4, 8, 5, 9, 6, 1, 3, 7, 2}
    };

    private SudokuValidator validator;

    @Override
    @Before
    protected void setUp() {
        validator = SudokuValidator.getInstance();
    }

    @Override
    @After
    protected void tearDown() {
        validator = null;
    }

    private int[][] cloneInitialData() {
        int[][] temp = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                temp[k][n] = initialData[k][n];
            }
        }
        return temp;
    }

    @Test
    public void testDuplicated() throws Exception {
        int[][] testData = cloneInitialData();
        testData[3][5] = 5;

        SudokuValidationMessages messages = validator.validate(testData);
        assertFalse(messages.isPassed());
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(3, 0, 5,null, SudokuValidationError.DUPLICATED_IN_ROW)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(0, 5, 5,null, SudokuValidationError.DUPLICATED_IN_COLUMN)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(1, 1, 5,null, SudokuValidationError.DUPLICATED_IN_BLOCK)));
    }

    @Test
    public void testNotInUse() throws Exception {
        int[][] testData = cloneInitialData();
        testData[2][6] = 5;

        SudokuValidationMessages messages = validator.validate(testData);
        assertFalse(messages.isPassed());
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(2, 0, 8,null, SudokuValidationError.NOT_IN_USE_IN_ROW)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(0, 6, 8,null, SudokuValidationError.NOT_IN_USE_IN_COLUMN)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(0, 2, 8,null, SudokuValidationError.NOT_IN_USE_IN_BLOCK)));
    }

    @Test
    public void testEmpty() throws Exception {
        int[][] testData = cloneInitialData();
        testData[4][7] = 0;

        SudokuValidationMessages messages = validator.validate(testData);
        assertFalse(messages.isPassed());
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(4, 7, 0,null, SudokuValidationError.EMPTY)));
    }

    public void testFoundAll() throws Exception {
        int[][] testData = cloneInitialData();
        testData[8][8] = 1;

        SudokuValidationMessages messages = validator.validate(testData);
        assertFalse(messages.isPassed());
        assertEquals(6, messages.getMessages().size());
        int[] expectedMessages = new int[5];
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(0, 8, 1, null, SudokuValidationError.DUPLICATED_IN_COLUMN)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(8, 0, 1, null, SudokuValidationError.DUPLICATED_IN_ROW)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(2, 2, 1, null, SudokuValidationError.DUPLICATED_IN_BLOCK)));

        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(0, 8, 2, null, SudokuValidationError.NOT_IN_USE_IN_COLUMN)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(8, 0, 2, null, SudokuValidationError.NOT_IN_USE_IN_ROW)));
        assertTrue(messages.getMessages().contains(validator.new SudokuValidationMessage(2, 2, 2, null, SudokuValidationError.NOT_IN_USE_IN_BLOCK)));
    }
}
