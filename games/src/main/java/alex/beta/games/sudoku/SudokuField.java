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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author alexsong
 * @version ${project.version}
 */
@SuppressWarnings("squid:S3776")
public class SudokuField extends JTextField {

    private int inputValue;
    private int suggestedValue;

    private Component upComponent;
    private Component downComponent;
    private Component leftComponent;
    private Component rightComponent;


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
                } else if (temp == KeyEvent.VK_BACK_SPACE || temp == KeyEvent.VK_DELETE) {
                    //回退或者删除
                    inputValue = 0;
                    setText("");
                } else if (((JTextField) e.getComponent()).isEditable() && (temp <= KeyEvent.VK_9 && temp > KeyEvent.VK_0)) {
                    if (!isEmpty(((JTextField) e.getComponent()).getText()) && isEmpty(((JTextField) e.getComponent()).getSelectedText())) {
                        //已有数字，并且没有被选中
                        e.consume();
                    } else {
                        inputValue = temp - KeyEvent.VK_0;
                    }
                } else {
                    e.consume();    //如果不是则消除key事件,也就是按了键盘以后没有反应;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int temp = e.getKeyCode();
                if (temp == KeyEvent.VK_UP && upComponent != null) {
                    upComponent.requestFocus();
                } else if (temp == KeyEvent.VK_DOWN && downComponent != null) {
                    downComponent.requestFocus();
                } else if (temp == KeyEvent.VK_LEFT && leftComponent != null) {
                    leftComponent.requestFocus();
                } else if (temp == KeyEvent.VK_RIGHT && rightComponent != null) {
                    rightComponent.requestFocus();
                } else if (temp == KeyEvent.VK_BACK_SPACE || temp == KeyEvent.VK_DELETE) {
                    //Nothing
                } else {
                    e.consume();
                }
            }
        });
    }

    public int getSuggestedValue() {
        return this.suggestedValue;
    }

    public void setSuggestedValue(int value) {
        this.suggestedValue = value;
    }

    public int getInputValue() {
        return this.inputValue;
    }

    public void setInputValue(int value) {
        this.inputValue = value;
    }

    public void showSuggestion(Color foreground, Color background, boolean editable) {
        this.setEditable(editable);
        if (foreground != null)
            this.setForeground(foreground);
        if (background != null)
            this.setBackground(background);
        this.setText(this.suggestedValue > 9 || this.suggestedValue < 1 ? "" : String.valueOf(this.suggestedValue));
    }

    public void showInput(Color foreground, Color background, boolean editable) {
        this.setEditable(editable);
        if (foreground != null)
            this.setForeground(foreground);
        if (background != null)
            this.setBackground(background);
        this.setText(this.inputValue > 9 || this.inputValue < 1 ? "" : String.valueOf(this.inputValue));
    }

    public boolean isSame() {
        return this.inputValue == this.suggestedValue;
    }

    public void setUpComponent(Component upComponent) {
        this.upComponent = upComponent;
    }

    public void setDownComponent(Component downComponent) {
        this.downComponent = downComponent;
    }

    public void setLeftComponent(Component leftComponent) {
        this.leftComponent = leftComponent;
    }

    public void setRightComponent(Component rightComponent) {
        this.rightComponent = rightComponent;
    }

    private static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
