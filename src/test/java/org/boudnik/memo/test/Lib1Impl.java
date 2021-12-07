package org.boudnik.memo.test;

import org.boudnik.memo.Library;

public class Lib1Impl extends Library<Lib1> implements Lib1 {
    @Override
    public int inc(int x) {
        return x + 1;
    }
}
