/**
 * <p>
 * File Name: SudokuValidator.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/5 下午3:43
 * </p>
 */
package alex.beta.games.sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuValidator {
    private static final SudokuValidator ourInstance = new SudokuValidator();

    private static final String EMPTY_SUDOKU_DATA = "Cannot validate an empty Sudoku result";

    private SudokuValidator() {
        //To hide default public constructor
    }

    public static SudokuValidator getInstance() {
        return ourInstance;
    }

    public SudokuValidationMessages validate(SudokuField[][] fields) {
        Objects.requireNonNull(fields, EMPTY_SUDOKU_DATA);

        int[][] data = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                data[k][n] = fields[k][n].getInputValue();
            }
        }
        return validate(data);
    }

    /**
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public SudokuValidationMessages validate(int[][] data) {
        Objects.requireNonNull(data, EMPTY_SUDOKU_DATA);

        SudokuValidationMessages messages = new SudokuValidationMessages();
        //validate empty fields
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                if (data[k][n] <= 0) {
                    messages.setPassed(false);
                    messages.addMessage(new SudokuValidationMessage(k, n, 0, String.format("第%d行，第%d列没有填写答案%n", k + 1, n + 1), SudokuValidationError.EMPTY));
                }
            }
        }
        //validate row
        for (int k = 0; k < 9; k++) {
            int[] count = new int[9];
            for (int i = 0; i < 9; i++) {
                if (data[k][i] > 0) {
                    count[data[k][i] - 1]++;
                }
            }
            for (int i = 0; i < 9; i++) {
                if (count[i] == 0) {
                    messages.setPassed(false);
                    messages.addMessage(new SudokuValidationMessage(k, 0, i + 1, String.format("数字%d在第%d行中没有出现%n", i + 1, k + 1), SudokuValidationError.NOT_IN_USE_IN_ROW));
                } else if (count[i] > 1) {
                    messages.setPassed(false);
                    messages.addMessage(new SudokuValidationMessage(k, 0, i + 1, String.format("数字%d在第%d行中出现了%d次%n", i + 1, k + 1, count[i]), SudokuValidationError.DUPLICATED_IN_ROW));
                }
            }
        }
        //validate column
        for (int n = 0; n < 9; n++) {
            int[] count = new int[9];
            for (int i = 0; i < 9; i++) {
                if (data[i][n] > 0) {
                    count[data[i][n] - 1]++;
                }
            }
            for (int i = 0; i < 9; i++) {
                if (count[i] == 0) {
                    messages.setPassed(false);
                    messages.addMessage(new SudokuValidationMessage(0, n, i + 1, String.format("数字%d在第%d列中没有出现%n", i + 1, n + 1), SudokuValidationError.NOT_IN_USE_IN_COLUMN));
                } else if (count[i] > 1) {
                    messages.setPassed(false);
                    messages.addMessage(new SudokuValidationMessage(0, n, i + 1, String.format("数字%d在第%d列中出现了%d次%n", i + 1, n + 1, count[i]), SudokuValidationError.DUPLICATED_IN_COLUMN));
                }
            }
        }
        //validate block
        ArrayList<Integer>[][] blocks = new ArrayList[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                blocks[i][j] = new ArrayList<>();
            }
        }
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                blocks[k / 3][n / 3].add(data[k][n]);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int[] count = new int[9];
                for (int l = 0; l < 9; l++) {
                    if (blocks[i][j].get(l) > 0) {
                        count[blocks[i][j].get(l) - 1]++;
                    }
                }
                for (int m = 0; m < 9; m++) {
                    if (count[m] == 0) {
                        messages.setPassed(false);
                        messages.addMessage(new SudokuValidationMessage(i, j, m + 1, String.format("数字%d在(%d,%d)宫格中没有出现%n", m + 1, i + 1, j + 1), SudokuValidationError.NOT_IN_USE_IN_BLOCK));
                    } else if (count[m] > 1) {
                        messages.setPassed(false);
                        messages.addMessage(new SudokuValidationMessage(i, j, m + 1, String.format("数字%d在(%d,%d)宫格中出现了%d次%n", m + 1, i + 1, j + 1, count[m]), SudokuValidationError.DUPLICATED_IN_BLOCK));
                    }
                }
            }
        }

        return messages;
    }

    @SuppressWarnings("unchecked")
    public boolean preview(int[][] data) {
        Objects.requireNonNull(data, EMPTY_SUDOKU_DATA);

        //validate row
        for (int k = 0; k < 9; k++) {
            int[] count = new int[9];
            for (int i = 0; i < 9; i++) {
                if (data[k][i] > 0) {
                    if (count[data[k][i] - 1] >= 1) {
                        return false;
                    } else {
                        count[data[k][i] - 1]++;
                    }
                }
            }
        }
        //validate column
        for (int n = 0; n < 9; n++) {
            int[] count = new int[9];
            for (int i = 0; i < 9; i++) {
                if (data[i][n] > 0) {
                    if (count[data[i][n] - 1] >= 1) {
                        return false;
                    } else {
                        count[data[i][n] - 1]++;
                    }
                }
            }
        }
        //validate block
        ArrayList<Integer>[][] blocks = new ArrayList[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                blocks[i][j] = new ArrayList<>();
            }
        }
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                blocks[k / 3][n / 3].add(data[k][n]);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int[] count = new int[9];
                for (int l = 0; l < 9; l++) {
                    if (blocks[i][j].get(l) > 0) {
                        if (count[blocks[i][j].get(l) - 1] >= 1) {
                            return false;
                        } else {
                            count[blocks[i][j].get(l) - 1]++;
                        }
                    }
                }
            }
        }
        return true;
    }

    public enum SudokuValidationError {
        NONE,
        DUPLICATED_IN_ROW,
        DUPLICATED_IN_COLUMN,
        DUPLICATED_IN_BLOCK,
        NOT_IN_USE_IN_ROW,
        NOT_IN_USE_IN_COLUMN,
        NOT_IN_USE_IN_BLOCK,
        EMPTY
    }

    public class SudokuValidationMessages {
        private boolean passed;
        private List<SudokuValidationMessage> messages;

        public SudokuValidationMessages() {
            messages = new ArrayList<>();
            passed = true;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public List<SudokuValidationMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<SudokuValidationMessage> messages) {
            this.messages = messages;
        }

        public void addMessage(SudokuValidationMessage message) {
            messages.add(message);
        }
    }

    public class SudokuValidationMessage {
        private SudokuValidationError error = SudokuValidationError.NONE;
        private int row;
        private int column;
        private int digit;
        private String message;

        public SudokuValidationMessage(int row, int column, int digit, String message, SudokuValidationError error) {
            this.row = row;
            this.column = column;
            this.message = message;
            this.error = error;
            this.digit = digit;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public SudokuValidationError getError() {
            return error;
        }

        public void setError(SudokuValidationError error) {
            this.error = error;
        }

        public int getDigit() {
            return digit;
        }

        public void setDigit(int digit) {
            this.digit = digit;
        }

        @Override
        public boolean equals(Object var1) {
            if (var1 instanceof SudokuValidationMessage) {
                SudokuValidationMessage another = (SudokuValidationMessage) var1;
                return this.error == another.error && this.row == another.row && this.column == another.column && this.digit == another.digit;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.error.hashCode() + 31 * (this.row + 31 * (this.column + 31 * this.digit));
        }
    }
}
