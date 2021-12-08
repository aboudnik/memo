package org.boudnik.memo;

import org.boudnik.memo.test.Lib1;
import org.boudnik.memo.test.Lib1Impl;
import org.boudnik.memo.test.Lib2;
import org.boudnik.memo.test.Lib2Impl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextTest {

    private static Context context;

    @BeforeAll
    static void setUp() {
        context = new Context();
    }

    @Test
    void test() {
        final Lib1 lib1 = context.proxy(Lib1Impl.class);
        assertEquals(6, lib1.inc(5));
        assertEquals(6, lib1.inc(5));

        final Lib2 lib2 = context.proxy(Lib2Impl.class);
        assertEquals(2, lib2.dec(3));
    }
}
