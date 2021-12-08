package org.boudnik.memo;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Dialect {

    public abstract Object get(String table, Object id) throws SQLException;

    public abstract Object set(String table, Object id, Object value) throws SQLException;

    public void close() throws SQLException {
        connection.close();
    }

    protected final Connection connection;

    protected Dialect(DataSource dataSource) {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static final Dialect H2 = new Dialect(getH2dataSource("jdbc:h2:mem:test")) {
        @Override
        public Object get(String table, Object id) throws SQLException {
            try (final PreparedStatement statement = connection.prepareStatement(String.format("SELECT value FROM %s WHERE id = ?", table))) {
                statement.setObject(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getObject(1);
                    }
                }
            }
            throw new SQLException(String.format("no such row %s in %s", id, table));
        }

        @Override
        public Object set(String table, Object id, Object value) throws SQLException {
            try (final PreparedStatement statement = connection.prepareStatement(String.format("MERGE INTO %s KEY (id) VALUES (?, ?)", table))) {
                statement.setObject(1, id);
                statement.setObject(2, value);
                if (1 != statement.executeUpdate())
                    throw new RuntimeException(String.format("cannot merge %s into %s", id, table));
                return value;
            }
        }
    };

    @SuppressWarnings("SameParameterValue")
    private static JdbcDataSource getH2dataSource(String url) {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }
}
