package org.boudnik.memo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class Memoizer {

    public static final int[] EMPTY = new int[]{};
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, Set<String>> lineage = new HashMap<>();
    private final Deque<String> stack = new LinkedList<>();
    private final Map<String, Object> cache = new HashMap<>() {
        @Override
        public Object put(String key, Object value) {
            for (Entry<String, Set<String>> entry : lineage.entrySet()) {
                for (String s : entry.getValue()) {
                    if (s.equals(key)) {
                        final Object removed = remove(entry.getKey());
                        if (removed != null) {
                            log("removed " + entry.getKey() + "=" + removed);
                        }
                    }
                }
            }
            log("put " + key + "=" + value);
            return super.put(key, value);
        }

        @Override
        public Object get(Object key) {
            final Object value = super.get(key);
            if (value != null) {
                log("get " + key + "=" + value);
            }
            return value;
        }
    };

    private static String key(Method method, Object... args) {
        final String clazz = method.getDeclaringClass().getName();
        final String name = method.getName();
        return GSON.toJson(Collections.singletonMap(Memoizer.isSetter(method) || Memoizer.isGetter(method)
                ? clazz + "." + name.substring(3)
                : clazz + "." + name, args == null ? EMPTY : args));
    }

    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set");
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get");
    }

    public Library init() {
        return LibraryImpl.LIBRARY = proxy(new LibraryImpl());
    }

    private void log(String message) {
        System.out.println("  ".repeat(stack.size()) + message);
    }

    private Object exec(Object proxy, Method method, Object[] args) {
        try {
            log("call " + Memoizer.key(method, args));
            return method.invoke(proxy, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T proxy(T library) {
        final Object o = Proxy.newProxyInstance(
                Library.class.getClassLoader(),
                new Class[]{Library.class},
                (p, m, a) -> {
                    if (m.getDeclaringClass() == Object.class) {
                        return exec(library, m, a);
                    }
                    Object value;
                    if (Memoizer.isSetter(m)) {
                        final String key = Memoizer.key(m, a[0]);
                        enter(key);
                        cache.put(key, value = exec(library, m, a));
                        leave();
                    } else {
                        final String key = Memoizer.key(m, a);
                        enter(key);
                        value = cache.get(key);
                        if (value == null) {
                            cache.put(key, value = exec(library, m, a));
                        }
                        leave();
                    }
                    return value;
                }
        );
        //noinspection unchecked
        return (T) o;
    }

    private void enter(String key) {
        stack.push(key);
        for (String s : stack) {
            if (s.equals(key))
                continue;
            lineage.computeIfAbsent(s, k -> new HashSet<>()).add(key);
        }
    }

    private void leave() {
        stack.pop();
    }
}
