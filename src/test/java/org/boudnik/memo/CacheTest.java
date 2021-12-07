package org.boudnik.memo;

import org.boudnik.memo.test.Lib1;
import org.boudnik.memo.test.Lib1Impl;
import org.boudnik.memo.test.Lib2;
import org.boudnik.memo.test.Lib2Impl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheTest {

    private static Cache cache;

    @BeforeAll
    static void setUp() {
        cache = new Cache();
    }

    @Test
    void test() {
        final Lib1 lib1 = cache.proxy(Lib1Impl.class);
        assertEquals(6, lib1.inc(5));
        assertEquals(6, lib1.inc(5));

        final Lib2 lib2 = cache.proxy(Lib2Impl.class);
        assertEquals(2, lib2.dec(3));
    }
}
