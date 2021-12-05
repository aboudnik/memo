package org.boudnik.memo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Cache {
    static final Map<Class<Class<?>>, Object> PROXIES = new HashMap<>();

    @SuppressWarnings("unchecked")
    <P> P proxy(Class<P> clazz) {
        final P proxy = newInstance(clazz);
        return (P) PROXIES.computeIfAbsent((Class<Class<?>>) clazz,
                c -> Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        clazz.getInterfaces(),
                        (p, m, a) -> m.getDeclaringClass() == Object.class
                                ? m.invoke(proxy, a)
                                : invoke(proxy, m, a)
                ));
    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalAccessError("");
        }
    }

    private Object invoke(Object p, Method m, Object[] a) throws Throwable {
        try {
            enter("");
            return m.invoke(p, a);
        } finally {
            leave();
        }
    }

    private void enter(String s) {

    }

    private void leave() {

    }
}
