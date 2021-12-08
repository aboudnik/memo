package org.boudnik.memo;

import org.boudnik.memo.test.Portfolio;
import org.boudnik.memo.test.PortfolioImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioTest {
    private static Context context;

    @BeforeAll
    static void setUp() throws SQLException {

        context = new Context(Dialect.H2);

        try (final PreparedStatement statement = context.getConnection().prepareStatement("drop table if exists prices")) {
            assertEquals(0, statement.executeUpdate());
        }
        try (final PreparedStatement statement = context.getConnection().prepareStatement("create table prices (id varchar(10), value double)")) {
            assertEquals(0, statement.executeUpdate());
        }
        try (final PreparedStatement insert = context.getConnection().prepareStatement("insert into prices values" +
                "('MSFT', 326.19)," +
                "('INTC', 52.86)")) {
            assertEquals(2, insert.executeUpdate());
        }
    }

    @AfterAll
    static void tireDown() throws SQLException {
        context.close();
    }

    @Test
    void test() {
        final Portfolio portfolio = context.proxy(PortfolioImpl.class);
        portfolio.getMarketPrice("MSFT");
        portfolio.getMarketPrice("MSFT");
        assertEquals(326.19 * 10, portfolio.presentValue("Smith", "MSFT"));
        assertEquals(52.86 * 10, portfolio.presentValue("Smith", "INTC"));
        assertEquals(52.86 * 10, portfolio.presentValue("Jane", "INTC"));
        assertEquals(326.19 * 10, portfolio.presentValue("Smith", "MSFT"));

        assertThrows(Exception.class, () -> assertEquals(171.18 * 10, portfolio.presentValue("Jane", "AAPL")));

        portfolio.setMarketPrice("AAPL", 171.18);
        assertEquals(171.18 * 10, portfolio.presentValue("Jane", "AAPL"));
        portfolio.setMarketPrice("MSFT", 334.35);
        assertEquals(334.35 * 10, portfolio.presentValue("Smith", "MSFT"));
        assertEquals(334.35 * 10, portfolio.presentValue("Smith", "MSFT"));
    }
}