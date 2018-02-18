/**
 * <p>
 * File Name: TranslationStatus.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/17 下午9:38
 * </p>
 */
package alex.beta.onlinetranslation.persistence;

/**
 * @author alexsong
 * @version ${project.version}
 */
public enum TranslationStatus {
    SUBMITTED("SUBMITTED"),
    PROCESSING("PROCESSING"),
    READY("READY"),
    ERROR("ERROR"),
    NOT_AUTHORIZED("NOT_AUTHORIZED"),
    TIMEOUT("TIMEOUT");

    private String value;

    TranslationStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static TranslationStatus fromString(String value) {
        if (value == null) {
            return null;
        } else if (value.equalsIgnoreCase("SUBMITTED")) {
            return SUBMITTED;
        } else if (value.equalsIgnoreCase("PROCESSING")) {
            return PROCESSING;
        } else if (value.equalsIgnoreCase("READY")) {
            return READY;
        } else if (value.equalsIgnoreCase("ERROR")) {
            return ERROR;
        } else if (value.equalsIgnoreCase("NOT_AUTHORIZED")) {
            return NOT_AUTHORIZED;
        } else if (value.equalsIgnoreCase("TIMEOUT")) {
            return TIMEOUT;
        } else {
            return null;
        }
    }
}
