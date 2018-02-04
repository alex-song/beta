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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuForm {
    private static final Logger logger = LoggerFactory.getLogger(SudokuForm.class);

    private JPanel contentPane;
    private JPanel toolbarPanel;
    private JPanel gridPanel;
    private JButton restartBtn;
    private JButton startBtn;
    private JButton submitBtn;
    private JLabel countingField;

    //Customized UI code
    private JTextField[][] textFields;
    private JMenuBar menuBar;
    private JMenuItem quickStartButton;
    private JMenuItem customizedButton;
    private JMenuItem showButton;
    private JMenuItem exitButton;

    //Controller
    private SudokuEngine engine;

    private boolean customizedGame;
    private boolean shownResult;
    private boolean gameStarted;
    private boolean gameStopped;
    //private boolean gamePaused;

    private int[][] initialData;

    public SudokuForm() {
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine == null) {
                    JOptionPane.showMessageDialog(contentPane, "请从菜单中选择游戏方式");
                    return;
                }
                if (gameStarted && !gameStopped) {
                    //已有游戏在进行
                    return;
                }

                if (customizedGame) {
                    for (int k = 0; k < 9; k++) {
                        for (int n = 0; n < 9; n++) {
                            initialData[k][n] = ((SudokuField)textFields[k][n]).getInputValue();
                        }
                    }
                    engine.setData(initialData);

                    if (engine.solveSudo(false)) {
                        int[][] resultData = engine.getData();
                        for (int k = 0; k < 9; k++) {
                            for (int n = 0; n < 9; n++) {
                                ((SudokuField)textFields[k][n]).setSuggestedValue(resultData[k][n]);
                                if (initialData[k][n] != 0) {
                                    ((SudokuField)textFields[k][n]).showInput(Color.BLACK, Color.GRAY, false);
                                } else {
                                    ((SudokuField)textFields[k][n]).showInput(Color.BLACK, Color.WHITE, true);
                                }
                            }
                        }
                        engine.printSudo();
                    } else {
                        JOptionPane.showMessageDialog(contentPane, "无解，请修改游戏");
                        return;
                    }
                }
                //初始化计时器
                countingField.setText("00:00:00 000");
                //启动计时程序
                Thread countingThread = new Thread() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        do {
                            countingField.setText(formatMilliseconds(System.currentTimeMillis() - startTime));

                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                logger.error("Counter thread is interrupted", ex);
                                countingField.setText("99:99:99 999");
                            }
                        } while (!gameStopped);
                    }
                };
                countingThread.setDaemon(true);
                countingThread.start();
                //初始化控制变量
                gameStarted = true;
                gameStopped = false;
                shownResult = false;
                startBtn.setEnabled(false);
            }

            /**
             * 将毫秒数格式化
             */
            private final String formatMilliseconds(long elapsed) {
                int hour, minute, second, milli;

                milli = (int) (elapsed % 1000);
                elapsed = elapsed / 1000;

                second = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                minute = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                hour = (int) (elapsed % 60);

                return String.format("%02d:%02d:%02d %03d", hour, minute, second, milli);
            }
        });

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO
            }

            private final boolean validateRow(int row) {
                int count = 0;
                for (int n = 0; n < 9; n++) {
                    count += Integer.parseInt(textFields[row][n].getText().trim());
                }
                if (count != 45) {
                    JOptionPane.showMessageDialog(contentPane, "第" + row + "行有重复的数字");
                    return false;
                }
                return true;
            }

            private final boolean validateColumn(int column) {
                int count = 0;
                for (int k = 0; k < 9; k++) {
                    count += Integer.parseInt(textFields[k][column].getText().trim());
                }
                if (count != 45) {
                    JOptionPane.showMessageDialog(contentPane, "第" + column + "列有重复的数字");
                    return false;
                }
                return true;
            }

            private final boolean validateBlock(int row, int column) {
                int count = 0;
                if (row < 3) {
                    if (column < 3) {
                        count += Integer.parseInt(textFields[0][0].getText().trim());
                        count += Integer.parseInt(textFields[0][1].getText().trim());
                        count += Integer.parseInt(textFields[0][2].getText().trim());

                        count += Integer.parseInt(textFields[1][0].getText().trim());
                        count += Integer.parseInt(textFields[1][1].getText().trim());
                        count += Integer.parseInt(textFields[1][2].getText().trim());

                        count += Integer.parseInt(textFields[2][0].getText().trim());
                        count += Integer.parseInt(textFields[2][1].getText().trim());
                        count += Integer.parseInt(textFields[2][2].getText().trim());
                    } else if (column < 6) {
                        count += Integer.parseInt(textFields[0][3].getText().trim());
                        count += Integer.parseInt(textFields[0][4].getText().trim());
                        count += Integer.parseInt(textFields[0][5].getText().trim());

                        count += Integer.parseInt(textFields[1][3].getText().trim());
                        count += Integer.parseInt(textFields[1][4].getText().trim());
                        count += Integer.parseInt(textFields[1][5].getText().trim());

                        count += Integer.parseInt(textFields[2][3].getText().trim());
                        count += Integer.parseInt(textFields[2][4].getText().trim());
                        count += Integer.parseInt(textFields[2][5].getText().trim());
                    } else {
                        count += Integer.parseInt(textFields[0][6].getText().trim());
                        count += Integer.parseInt(textFields[0][7].getText().trim());
                        count += Integer.parseInt(textFields[0][8].getText().trim());

                        count += Integer.parseInt(textFields[1][6].getText().trim());
                        count += Integer.parseInt(textFields[1][7].getText().trim());
                        count += Integer.parseInt(textFields[1][8].getText().trim());

                        count += Integer.parseInt(textFields[2][6].getText().trim());
                        count += Integer.parseInt(textFields[2][7].getText().trim());
                        count += Integer.parseInt(textFields[2][8].getText().trim());
                    }
                } else if (row < 6) {
                    if (column < 3) {
                        count += Integer.parseInt(textFields[3][0].getText().trim());
                        count += Integer.parseInt(textFields[4][1].getText().trim());
                        count += Integer.parseInt(textFields[5][2].getText().trim());

                        count += Integer.parseInt(textFields[3][0].getText().trim());
                        count += Integer.parseInt(textFields[4][1].getText().trim());
                        count += Integer.parseInt(textFields[5][2].getText().trim());

                        count += Integer.parseInt(textFields[3][0].getText().trim());
                        count += Integer.parseInt(textFields[4][1].getText().trim());
                        count += Integer.parseInt(textFields[5][2].getText().trim());
                    } else if (column < 6) {
                        count += Integer.parseInt(textFields[3][3].getText().trim());
                        count += Integer.parseInt(textFields[4][4].getText().trim());
                        count += Integer.parseInt(textFields[5][5].getText().trim());

                        count += Integer.parseInt(textFields[3][3].getText().trim());
                        count += Integer.parseInt(textFields[4][4].getText().trim());
                        count += Integer.parseInt(textFields[5][5].getText().trim());

                        count += Integer.parseInt(textFields[3][3].getText().trim());
                        count += Integer.parseInt(textFields[4][4].getText().trim());
                        count += Integer.parseInt(textFields[5][5].getText().trim());
                    } else {
                        count += Integer.parseInt(textFields[3][6].getText().trim());
                        count += Integer.parseInt(textFields[4][7].getText().trim());
                        count += Integer.parseInt(textFields[5][8].getText().trim());

                        count += Integer.parseInt(textFields[3][6].getText().trim());
                        count += Integer.parseInt(textFields[4][7].getText().trim());
                        count += Integer.parseInt(textFields[5][8].getText().trim());

                        count += Integer.parseInt(textFields[3][6].getText().trim());
                        count += Integer.parseInt(textFields[4][7].getText().trim());
                        count += Integer.parseInt(textFields[5][8].getText().trim());
                    }
                } else {
                    if (column < 3) {
                        count += Integer.parseInt(textFields[6][0].getText().trim());
                        count += Integer.parseInt(textFields[7][1].getText().trim());
                        count += Integer.parseInt(textFields[8][2].getText().trim());

                        count += Integer.parseInt(textFields[6][0].getText().trim());
                        count += Integer.parseInt(textFields[7][1].getText().trim());
                        count += Integer.parseInt(textFields[8][2].getText().trim());

                        count += Integer.parseInt(textFields[6][0].getText().trim());
                        count += Integer.parseInt(textFields[7][1].getText().trim());
                        count += Integer.parseInt(textFields[8][2].getText().trim());
                    } else if (column < 6) {
                        count += Integer.parseInt(textFields[6][3].getText().trim());
                        count += Integer.parseInt(textFields[7][4].getText().trim());
                        count += Integer.parseInt(textFields[8][5].getText().trim());

                        count += Integer.parseInt(textFields[6][3].getText().trim());
                        count += Integer.parseInt(textFields[7][4].getText().trim());
                        count += Integer.parseInt(textFields[8][5].getText().trim());

                        count += Integer.parseInt(textFields[6][3].getText().trim());
                        count += Integer.parseInt(textFields[7][4].getText().trim());
                        count += Integer.parseInt(textFields[8][5].getText().trim());
                    } else {
                        count += Integer.parseInt(textFields[6][6].getText().trim());
                        count += Integer.parseInt(textFields[7][7].getText().trim());
                        count += Integer.parseInt(textFields[8][8].getText().trim());

                        count += Integer.parseInt(textFields[6][6].getText().trim());
                        count += Integer.parseInt(textFields[7][7].getText().trim());
                        count += Integer.parseInt(textFields[8][8].getText().trim());

                        count += Integer.parseInt(textFields[6][6].getText().trim());
                        count += Integer.parseInt(textFields[7][7].getText().trim());
                        count += Integer.parseInt(textFields[8][8].getText().trim());
                    }
                }
                if (count != 45) {
                    JOptionPane.showMessageDialog(contentPane, "9宫格中有重复的数字：\n第" + row + "行，第" + column + "列");
                    return false;
                }
                return true;
            }

            private final boolean validateNotEmpty() {
                boolean passed = true;
                int k = 0, n = 0;
                for (k = 0; k < 9 && passed; k++) {
                    for (n = 0; n < 9 && passed; n++) {
                        if (textFields[k][n].getText() == null || textFields[k][n].getText().trim().isEmpty()) {
                            passed = false;
                            break;
                        } else {
                            try {
                                int temp = Integer.parseInt(textFields[k][n].getText().trim());
                                if (temp < 1 || temp > 9) {
                                    passed = false;
                                    break;
                                }
                            } catch (Exception ex) {
                                passed = false;
                                break;
                            }
                        }
                    }
                }
                if (!passed) {
                    JOptionPane.showMessageDialog(contentPane, "还有未完成的格子：\n第" + k + "行，第" + n + "列");
                }
                return passed;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void createUIComponents() {
        gridPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        JPanel[] subGridPanels = new JPanel[9];
        for (int i = 0; i < 9; i++) {
            subGridPanels[i] = new JPanel(new GridLayout(3, 3, 3, 3));
            gridPanel.add(subGridPanels[i]);
        }

        textFields = new SudokuField[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                textFields[k][n] = new SudokuField();
                textFields[k][n].setEditable(false);         //只可显示不可修改
                positionTextField(k, n, subGridPanels, textFields[k][n]);      //添加文本框
            }
        }
        textFields[8][8].setNextFocusableComponent(textFields[0][0]);

        //Menu
        menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        quickStartButton = new JMenuItem("快速开始");
        quickStartButton.setToolTipText("随机生成一局数独游戏");
        quickStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted && !gameStopped) {
                    return;
                }
                String inputValue = "18";
                do {
                    inputValue = JOptionPane.showInputDialog("请输入显示的数字个数(9 - 45)：", inputValue);
                    logger.debug("Input string is {}", inputValue);
                }
                while (!inputValue.matches("\\d+") || Integer.parseInt(inputValue) < 9 || Integer.parseInt(inputValue) > 45);
                //初始化游戏引擎
                engine = new SudokuEngine();
                initialData = new int[9][9];
                engine.setTip(Integer.parseInt(inputValue));
                countingField.setText("00:00:00 000");
                boolean canSolve = true;
                do {
                    engine.genSudo();
                    engine.printSudo();
                    cacheInitialData(engine.getData());

                    canSolve = engine.solveSudo(false);
                    if (canSolve) {
                        int[][] resultData = engine.getData();
                        for (int k = 0; k < 9; k++) {
                            for (int n = 0; n < 9; n++) {
                                ((SudokuField)textFields[k][n]).setSuggestedValue(resultData[k][n]);
                            }
                        }
                    }
                } while (!canSolve);
                //初始化九宫格
                setInitialData();
                //初始化系统变量
                customizedGame = false;
                gameStarted = false;
                gameStopped = true;
                //gamePaused = false;
                shownResult = false;
                //初始化计数器
                countingField.setText("00:00:00 000");
                //初始化按钮
                startBtn.setEnabled(true);
            }
        });
        gameMenu.add(quickStartButton);

        customizedButton = new JMenuItem("定制游戏");
        customizedButton.setToolTipText("自己定制一局数独游戏");
        customizedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted && !gameStopped) {
                    return;
                }
                //初始化游戏引擎
                engine = new SudokuEngine();
                initialData = new int[9][9];
                for (int k = 0; k < 9; k++) {
                    for (int n = 0; n < 9; n++) {
                        initialData[k][n] = 0;
                    }
                }
                setInitialData();
                textFields[0][0].requestFocusInWindow();
                customizedGame = true;
                gameStarted = false;
                gameStopped = true;
                //gamePaused = false;
                shownResult = false;
                countingField.setText("00:00:00 000");
                startBtn.setEnabled(true);
            }
        });
        gameMenu.add(customizedButton);

        showButton = new JMenuItem("显示结果");
        showButton.setToolTipText("显示当前游戏结果，并停止计时");
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shownResult || engine == null) {
                    return;
                }
                for (int k = 0; k < 9; k++) {
                    for (int n = 0; n < 9; n++) {
                        if (((SudokuField)textFields[k][n]).isSame()) {
                            ((SudokuField)textFields[k][n]).showSuggestion(Color.BLACK, null, false);
                        } else {
                            ((SudokuField)textFields[k][n]).showSuggestion(Color.RED, null, false);
                        }
                    }
                }
                shownResult = true;
                gameStopped = true;
                startBtn.setEnabled(false);
            }
        });
        gameMenu.add(showButton);

        gameMenu.addSeparator();

        exitButton = new JMenuItem("退出");
        exitButton.setToolTipText("退出游戏");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStopped = true;
                System.exit(0);
            }
        });
        gameMenu.add(exitButton);

        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(contentPane, "作者：Alex Song \n E-mail: song_liping@hotmail.com");
            }
        });

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
    }

    /**
     * @param k             行
     * @param n             列
     * @param subGridPanels 子托盘数组
     * @param textField     数字框
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
                ((SudokuField)textFields[k][n]).setInputValue(this.initialData[k][n]);

                if (this.initialData[k][n] != 0) {
                    ((SudokuField)textFields[k][n]).showInput(Color.BLACK, Color.GRAY, false);
                } else {
                    ((SudokuField)textFields[k][n]).showInput(Color.BLACK, Color.WHITE, true);
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

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();

        frame.setTitle("数独游戏");
        frame.setSize(600, 900);

        SudokuForm sudokuForm = new SudokuForm();
        frame.setContentPane(sudokuForm.contentPane);
        frame.setJMenuBar(sudokuForm.menuBar);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
