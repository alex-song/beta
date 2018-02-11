/**
 * <p>
 * File Name: SudokuFieldTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/11 下午1:24
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
public class SudokuFieldTest {
    private SudokuField field;

    @Before
    public void setUp() {
        field = new SudokuField();
        field.setInputValue(1);
        field.setSuggestedValue(2);
    }

    @After
    public void tearDown() {
        field = null;
    }

    @Test
    public void testBasic() {
        field.showInput(null, null, true);

        assertFalse(field.isSame());
        assertTrue(field.isEditable());
    }

    @Test
    public void testShowInput() {
        field.showInput(null, null, true);

        assertEquals("1", field.getText());
    }

    @Test
    public void testShowSuggestion() {
        field.showSuggestion(null, null, true);

        assertEquals("2", field.getText());
    }

    @Test
    public void testInvalidValue() {
        SudokuField invalidFied = new SudokuField();
        invalidFied.setInputValue(0);
        invalidFied.setSuggestedValue(10);

        invalidFied.showSuggestion(null, null, true);
        assertEquals("", field.getText());

        invalidFied.showInput(null, null, true);
        assertEquals("", field.getText());
    }
}
