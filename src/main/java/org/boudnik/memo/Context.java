package org.boudnik.memo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Context implements AutoCloseable {
    private static final Map<Class<?>, Object> PROXIES = new HashMap<>();
    private final Dialect dialect;

    static final Logger LOGGER;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel(Level.FINER);
        rootLog.getHandlers()[0].setLevel(Level.ALL);
        LOGGER = Logger.getLogger(Context.class.getName());
    }

    public Context() {
        this(null);
    }

    public Context(Dialect dialect) {
        this.dialect = dialect;
    }

    public Object get(String table, Parameter... ids) {
        LOGGER.finer(() -> String.format("data  read value from %s where id = %s", table, Arrays.toString(ids)));
        return dialect.get(table, ids);
    }

    public Object set(String table, Object value, Parameter... ids) {
        LOGGER.finer(() -> String.format("data  save %s to %s where id = %s", value, table, Arrays.toString(ids)));
        return dialect.set(table, value, ids);
    }

    public void dump(String table) {
        dialect.dump(table);
    }


    public void close() throws SQLException {
        dialect.close();
    }

    public Connection getConnection() {
        return dialect.getConnection();
    }

    static class Key {
        private final String method;
        private final Object[] args;
        private final boolean setter;
        private final boolean getter;


        public Key(@NotNull Method method, Object[] args) {
            final String clazz = method.getDeclaringClass().getName();
            final String name = method.getName();
            getter = name.startsWith("get");
            setter = name.startsWith("set");

            this.method = clazz + "." + (getter || setter ? name.substring(3) : name);
            this.args = setter ? new Object[]{args[0]} : args;
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
                            LOGGER.finer(() -> String.format("clear %s=%s", entry.getKey(), removed));
                        }
                    }
                }
            }
            LOGGER.finer(() -> String.format("c.put %s=%s", key, value));
            return super.put(key, value);
        }

        @Override
        public Object get(Object key) {
            final Object value = super.get(key);
            if (value != null) {
                LOGGER.finer(() -> String.format("c.get %s=%s", key, value));
            }
            return value;
        }
    };

    public static final int[] EMPTY = new int[]{};
    public static final Gson GSON = new GsonBuilder().create();

    @SuppressWarnings("unchecked")
    public <I> I proxy(Class<? extends Library<I>> clazz) {
        final Library<I> proxy = newInstance(clazz);
        return proxy.setProxy(this, (I) PROXIES.computeIfAbsent(clazz,
                c -> Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        clazz.getInterfaces(),
                        (p, m, a) -> m.getDeclaringClass() == Object.class
                                ? m.invoke(proxy, a)
                                : invoke(proxy, m, a)
                )));
    }

    private <I> @NotNull Library<I> newInstance(Class<? extends Library<I>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Object invoke(Object p, Method m, Object[] a) throws InvocationTargetException, IllegalAccessException {
        final Key key = new Key(m, a);
        enter(key);
        try {
            Object value;
            if (key.setter) { // check if value is the same and do nothing
                LOGGER.fine(() -> String.format((cache.containsKey(key) ? "over  %s" : "write %s"), GSON.toJson(Collections.<String, Object>singletonMap(key.method, a))));
                cache.put(key, value = m.invoke(p, a));
            } else if (key.getter) {
                if (cache.containsKey(key)) {
                    LOGGER.fine(() -> String.format("reuse %s", key));
                    value = cache.get(key);
                } else {
                    LOGGER.fine(() -> String.format("read  %s", key));
                    cache.put(key, value = m.invoke(p, a));
                }
            } else {
                if (cache.containsKey(key)) {
                    LOGGER.fine(() -> String.format("reuse %s", key));
                    value = cache.get(key);
                } else {
                    LOGGER.fine(() -> String.format("eval  %s", key));
                    cache.put(key, value = m.invoke(p, a));
                }
            }
            return value;
        } finally {
            leave();
        }
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
