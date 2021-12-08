package org.boudnik.memo;

public class Library<P> {
    protected P proxy;
    private Context context;

    P setProxy(Context context, P proxy) {
        this.context = context;
        return this.proxy = proxy;
    }

    public P library() {
        return proxy;
    }

    public Context context() {
        return context;
    }
}
