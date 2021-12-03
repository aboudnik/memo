package org.boudnik.memo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Cache {
    static Map<Class<Class<?>>, Object> proxies = new HashMap<>();

    public Object invoke(Object p, Method m, Object[] a) throws Throwable {
        if (m.getDeclaringClass() == Object.class) {
            return m.invoke(p, a);
        } else {
            return m.invoke(p, a);
        }
    }

    @SuppressWarnings("unchecked")
    <P> P proxy(Class<P> clazz) {
        return (P) proxies.computeIfAbsent((Class<Class<?>>) clazz, c -> {
            final P proxy = getProxy(clazz);
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    clazz.getInterfaces(),
                    (p, m, a) -> invoke(proxy, m, a)
            );
        });
    }

    private <T> T getProxy(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalAccessError("");
        }
    }
}
