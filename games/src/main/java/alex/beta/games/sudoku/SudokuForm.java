/**
 * <p>
 * File Name: SudokuForm.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/2 下午9:50
 * </p>
 */
package alex.beta.games.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuForm {
    private static final Logger logger = LoggerFactory.getLogger(SudokuForm.class);

    private JPanel contentPane;
    private JPanel toolbarPanel;
    private JPanel statusPanel;
    private JPanel gridPanel;
    private JTextField progressStatusField;
    private JTextField infoStatusField;
    private JButton quickStartBtn;
    private JButton customizedBtn;
    private JButton resetBtn;
    private JButton restartBtn;
    private JButton startBtn;
    private JButton submitBtn;
    private JButton showBtn;

    //Customized UI code
    private JTextField[][] textFields;

    private Sudoku engine;
    private int[][] initialData;
    private int[][] resultData;

    public SudokuForm() {
        engine = new Sudoku();

        quickStartBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputValue = "18";
                do {
                    inputValue = JOptionPane.showInputDialog("请输入显示的数字个数(9 - 45)：", inputValue);
                    logger.debug("Input string is {}", inputValue);
                } while (!inputValue.matches("\\d+") || Integer.parseInt(inputValue) < 9 || Integer.parseInt(inputValue) > 45);

                engine.setTip(Integer.parseInt(inputValue));
                progressStatusField.setText("0.0");
                boolean canSolve = true;
                do {
                    engine.setData(new int[9][9]);
                    engine.genSudo();
                    cacheInitialData(engine.getData());

                    canSolve = engine.solveSudo(false);
                    resultData = engine.getData();
                } while (!canSolve);
                setInitialData();
            }
        });

        showBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resultData == null) {
                    return;
                }
                for (int k = 0; k < 9; k++) {
                    for (int n = 0; n < 9; n++) {
                        if (!String.valueOf(resultData[k][n]).equals(textFields[k][n].getText().trim())) {
                            textFields[k][n].setForeground(Color.RED);
                        } else {
                            textFields[k][n].setForeground(Color.BLACK);
                        }
                        textFields[k][n].setText(String.valueOf(resultData[k][n]));
                        textFields[k][n].setEditable(false);
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        frame.setTitle("数独游戏");
        frame.setSize(600, 900);

        SudokuForm sudokuForm = new SudokuForm();
        frame.setContentPane(sudokuForm.contentPane);

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        gridPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        JPanel[] subGridPanels = new JPanel[9];
        for (int i = 0; i < 9; i++) {
            subGridPanels[i] = new JPanel(new GridLayout(3, 3, 3, 3));
            gridPanel.add(subGridPanels[i]);
        }

        textFields = new JTextField[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                textFields[k][n] = new JTextField("");// + rightans[k][n]);
                textFields[k][n].setHorizontalAlignment(JTextField.CENTER);//将数字水平居中
                textFields[k][n].setEditable(false);         //只可显示不可修改
                positionTextField(k, n, subGridPanels, textFields[k][n]);      //添加文本框
            }
        }
        textFields[8][8].setNextFocusableComponent(textFields[0][0]);
    }

    /**
     *
     * @param k 行
     * @param n 列
     * @param subGridPanels 子托盘数组
     * @param textField 数字框
     */
    private void positionTextField(int k, int n, JPanel[] subGridPanels, JTextField textField) {
        if (k < 3) {
            if (n < 3) {
                subGridPanels[0].add(textField);
            } else if (n < 6) {
                subGridPanels[1].add(textField);
            } else {
                subGridPanels[2].add(textField);
            }
        } else if (k < 6) {
            if (n < 3) {
                subGridPanels[3].add(textField);
            } else if (n < 6) {
                subGridPanels[4].add(textField);
            } else {
                subGridPanels[5].add(textField);
            }
        } else {
            if (n < 3) {
                subGridPanels[6].add(textField);
            } else if (n < 6) {
                subGridPanels[7].add(textField);
            } else {
                subGridPanels[8].add(textField);
            }
        }
    }

    /**
     * 显示游戏初始值
     */
    private void setInitialData() {
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                textFields[k][n].setForeground(Color.BLACK);
                if (this.initialData[k][n] != 0) {
                    textFields[k][n].setText(String.valueOf(this.initialData[k][n]));
                    textFields[k][n].setEditable(false);
                    textFields[k][n].setBackground(Color.GRAY);
                } else {
                    textFields[k][n].setText("");
                    textFields[k][n].setEditable(true);
                    textFields[k][n].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void cacheInitialData(int[][] data) {
        this.initialData = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                this.initialData[k][n] = data[k][n];
            }
        }
    }
}
