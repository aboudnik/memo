package org.boudnik.memo.test;

import org.boudnik.memo.Library;

public class Lib2Impl extends Library<Lib2> implements Lib2 {
    @Override
    public int dec(int x) {
        return x - 1;
    }
}
