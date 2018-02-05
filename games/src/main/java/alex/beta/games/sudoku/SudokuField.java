/**
 * <p>
 * File Name: SudokuField.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/4 下午8:56
 * </p>
 */
package alex.beta.games.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuField extends JTextField implements JDuoIntValuesField {

    private static final Logger logger = LoggerFactory.getLogger(SudokuField.class);

    private int inputValue, suggestedValue;

    public SudokuField() {
        super();
        inputValue = 0;
        suggestedValue = 0;
        setFont(new Font("宋体", Font.BOLD, 20));
        setHorizontalAlignment(JTextField.CENTER);//将数字水平居中
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                int temp = e.getKeyChar();
                if (temp == KeyEvent.VK_ENTER || temp == KeyEvent.VK_TAB) {//按回车时
                    e.getComponent().transferFocus();
                } else if (((JTextField) e.getComponent()).getText().length() > 0) {
                    //已有数字
                    e.consume();
                } else if (temp <= KeyEvent.VK_9 && temp > KeyEvent.VK_0) {
                    //数字键
                    inputValue = temp - KeyEvent.VK_0;
                } else if (temp == KeyEvent.VK_BACK_SPACE || temp == KeyEvent.VK_DELETE) {
                    //回退或者删除
                } else {
                    e.consume();    //如果不是则消除key事件,也就是按了键盘以后没有反应;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public int getSuggestedValue() {
        return this.suggestedValue;
    }

    @Override
    public void setSuggestedValue(int value) {
        this.suggestedValue = value;
    }

    @Override
    public int getInputValue() {
        return this.inputValue;
    }

    @Override
    public void setInputValue(int value) {
        this.inputValue = value;
    }

    @Override
    public void showSuggestion(Color foreground, Color background, boolean editable) {
        this.setEditable(editable);
        if (foreground != null)
            this.setForeground(foreground);
        if (background != null)
            this.setBackground(background);
        this.setText(this.suggestedValue > 9 || this.suggestedValue < 1 ? "" : String.valueOf(this.suggestedValue));
    }

    @Override
    public void showInput(Color foreground, Color background, boolean editable) {
        this.setEditable(editable);
        if (foreground != null)
            this.setForeground(foreground);
        if (background != null)
            this.setBackground(background);
        this.setText(this.inputValue > 9 || this.inputValue < 1 ? "" : String.valueOf(this.inputValue));
    }

    @Override
    public boolean isSame() {
        return this.inputValue == this.suggestedValue;
    }
}