package org.boudnik.memo;

import java.io.Serializable;

public class Parameter implements Serializable {
    final String key;
    final transient Object value;

    public Parameter(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}
