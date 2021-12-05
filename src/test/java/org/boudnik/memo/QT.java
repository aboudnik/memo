package org.boudnik.memo;

public class QT {

    public static void main(String[] args) {
        final Cache cache = new Cache();
        final Lib1 lib1 = cache.proxy(Lib1Impl.class);
        final Lib2 lib2 = cache.proxy(Lib2Impl.class);
        System.out.println("proxy.inc(5) = " + lib1.inc(5));
        System.out.println("proxy.dec(3) = " + lib2.dec(3));
    }
}
