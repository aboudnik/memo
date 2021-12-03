package org.boudnik.memo;

public class QT {

    public static void main(String[] args) {
        final Cache cache = new Cache();
        All proxy = cache.proxy(LibImpl.class);
        System.out.println("proxy = " + proxy);
        System.out.println("proxy.inc(5) = " + proxy.inc(5));
        System.out.println("proxy.inc(5) = " + proxy.dec(3));
        System.out.println("proxy.dec(3) = " + proxy.dec(3));

        proxy = cache.proxy(LibImpl.class);
        System.out.println("proxy.inc(5) = " + proxy.inc(5));
        System.out.println("proxy.inc(5) = " + proxy.dec(3));
        System.out.println("proxy.dec(3) = " + proxy.dec(3));
    }
}
