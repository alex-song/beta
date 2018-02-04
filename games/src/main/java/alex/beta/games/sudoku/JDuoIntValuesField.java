/**
 * <p>
 * File Name: JDuoIntValuesField.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/4 下午8:58
 * </p>
 */
package alex.beta.games.sudoku;

import java.awt.Color;

/**
 * @author alexsong
 * @version ${project.version}
 */
public interface JDuoIntValuesField {

    public int getSuggestedValue();

    public void setSuggestedValue(int value);

    public int getInputValue();

    public void setInputValue(int value);

    public void showSuggestion(Color foreground, Color background, boolean editable);

    public void showInput(Color foreground, Color background, boolean editable);

    public boolean isSame();
}
