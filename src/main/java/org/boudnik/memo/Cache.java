package org.boudnik.memo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class Cache {
    private static final Map<Class<?>, Object> PROXIES = new HashMap<>();

    static class Key {
        private final String method;
        private final Object[] args;
        private final boolean setter;


        public Key(Method method, Object[] args) {
            final String clazz = method.getDeclaringClass().getName();
            final String name = method.getName();
            boolean getter = name.startsWith("get");
            setter = name.startsWith("set");

            this.method = getter || setter ? clazz + "." + name.substring(3) : clazz + "." + name;
            this.args = setter ? new Object[]{args[0]} : args;
        }

        public boolean isSetter() {
            return setter;
        }

        @Override
        public int hashCode() {
            return 31 * method.hashCode() + Arrays.hashCode(args);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (!method.equals(key.method)) return false;
            return Arrays.equals(args, key.args);
        }

        @Override
        public String toString() {
            return GSON.toJson(Collections.singletonMap(method, args == null ? EMPTY : args));
        }
    }

    private final Map<Key, Set<Key>> lineage = new HashMap<>();
    private final Deque<Key> stack = new LinkedList<>();
    private final Map<Key, Object> cache = new HashMap<>() {
        @Override
        public Object put(Key key, Object value) {
            for (Entry<Key, Set<Key>> entry : lineage.entrySet()) {
                for (Key s : entry.getValue()) {
                    if (s.equals(key)) {
                        final Object removed = remove(entry.getKey());
                        if (removed != null) {
                            log("removed", entry.getKey(), removed);
                        }
                    }
                }
            }
            log("put", key, value);
            return super.put(key, value);
        }

        @Override
        public Object get(Object key) {
            final Object value = super.get(key);
            if (value != null) {
                log("get", (Key) key, value);
            }
            return value;
        }
    };

    public static final int[] EMPTY = new int[]{};
    private static final Gson GSON = new GsonBuilder().create();

    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set");
    }

    private void log(String message, Key key, Object o) {
        System.out.println("  ".repeat(stack.size()) + message + " " + key + (o == null ? "" : "=" + o));
    }

    @SuppressWarnings("unchecked")
    public <I> I proxy(Class<? extends Library<I>> clazz) {
        final Library<I> proxy = newInstance(clazz);
        return proxy.setProxy((I) PROXIES.computeIfAbsent(clazz,
                c -> Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        clazz.getInterfaces(),
                        (p, m, a) -> m.getDeclaringClass() == Object.class
                                ? m.invoke(proxy, a)
                                : invoke(proxy, m, a)
                )));
    }

    private <I> Library<I> newInstance(Class<? extends Library<I>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Object invoke(Object p, Method m, Object[] a) throws InvocationTargetException, IllegalAccessException {
        try {
            final Key key = getKey(m, a);
            log("call", key, null);
            enter(key);
            Object value;
            if (key.isSetter()) {
                cache.put(key, value = m.invoke(p, a));
            } else {
                value = cache.get(key);
                if (value == null) {
                    cache.put(key, value = m.invoke(p, a));
                }
            }
            return value;
        } finally {
            leave();
        }
    }

    private Key getKey(Method m, Object[] a) {
        return new Key(m, isSetter(m) ? new Object[]{a[0]} : a);
    }

    private void enter(Key key) {
        stack.push(key);
        for (Key c : stack) {
            if (c.equals(key))
                continue;
            lineage.computeIfAbsent(c, k -> new HashSet<>()).add(key);
        }
    }

    private void leave() {
        stack.pop();
    }
}
