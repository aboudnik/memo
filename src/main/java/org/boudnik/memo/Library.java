package org.boudnik.memo;

public class Library<P> {
    protected P proxy;

    public P setProxy(P proxy) {
        return this.proxy = proxy;
    }

    public P library() {
        return proxy;
    }
}
