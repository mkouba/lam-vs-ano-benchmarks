package org.acme.lam.beans;

import java.util.function.Supplier;

public class MyBean5 {

    public Supplier<Integer> ping(int value) {
        return () -> value + 1;
    }

}
