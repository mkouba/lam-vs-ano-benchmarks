package org.acme.ano.beans;

import java.util.function.Supplier;

public class MyBean2 {

    public Supplier<Integer> ping(int value) {
        return new Supplier<Integer>() {
            public Integer get() {
                return value + 1;
            }
        };
    }

}
