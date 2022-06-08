package org.boudnik.memo;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.boudnik.memo.Context.LOGGER;

public abstract class Dialect {

    protected final Connection connection;

    protected Dialect(DataSource dataSource) {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public abstract Object get(String table, Parameter... ids);

    public abstract Object set(String table, Object value, Parameter... ids);

    public abstract void dump(String table);

    public void close() throws SQLException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public static final Dialect H2 = new Dialect(getH2dataSource("jdbc:h2:mem:test")) {
        @Override
        public Object get(String table, Parameter... ids) {
            try (final PreparedStatement statement = connection.prepareStatement(String.format("SELECT \"value\" FROM %s %s", table, where(ids)))) {
                int index = 1;
                for (Parameter p : ids) {
                    statement.setObject(index++, p.value);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getObject(1);
                    }
                    throw new RuntimeException(String.format("no such row %s in %s", Arrays.toString(ids), table));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object set(String table, Object value, Parameter... ids) {
            try (final PreparedStatement statement = connection.prepareStatement(String.format("MERGE INTO %s %s", table, keyValues(ids)))) {
                int index = 1;
                for (Parameter p : ids) {
                    statement.setObject(index++, p.value);
                }
                statement.setObject(index, value);
                if (1 != statement.executeUpdate())
                    throw new RuntimeException(String.format("cannot merge %s into %s", Arrays.toString(ids), table));
                return value;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void dump(String table) {
            try (final PreparedStatement statement = connection.prepareStatement(String.format("SELECT * FROM %s", table))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    final int count = resultSet.getMetaData().getColumnCount();
                    while (resultSet.next()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < count; i++) {
                            sb.append(resultSet.getObject(i + 1)).append(" ");
                        }
                        LOGGER.finer(sb::toString);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static String where(Parameter... ids) {
        StringBuilder sb = new StringBuilder("WHERE ");
        String and = "";
        for (Parameter parameter : ids) {
            sb.append(and);
            sb.append(parameter.key);
            sb.append(" = ?");
            and = " and ";
        }
        return sb.toString();
    }

    private static String keyValues(Parameter... ids) {
        StringBuilder sb = new StringBuilder("KEY (");
        String comma = "";
        for (Parameter parameter : ids) {
            sb.append(comma).append(parameter.key);
            comma = ",";
        }
        sb.append(") VALUES (");
        comma = "";
        for (@SuppressWarnings("unused") Parameter parameter : ids) {
            sb.append(comma).append("?");
            comma = ",";
        }
        sb.append(",?)");
        return sb.toString();
    }

    @SuppressWarnings("SameParameterValue")
    private static JdbcDataSource getH2dataSource(String url) {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }
}
