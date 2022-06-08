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
        try (final PreparedStatement statement = context.getConnection().prepareStatement("create table prices (security varchar(10), \"value\" double)")) {
            assertEquals(0, statement.executeUpdate());
        }
        try (final PreparedStatement statement = context.getConnection().prepareStatement("drop table if exists portfolios")) {
            assertEquals(0, statement.executeUpdate());
        }
        try (final PreparedStatement statement = context.getConnection().prepareStatement("create table portfolios (portfolio varchar(10), security varchar(10), \"value\" double)")) {
            assertEquals(0, statement.executeUpdate());
        }
    }

    @AfterAll
    static void tireDown() throws SQLException {
        context.close();
    }

    @Test
    void test() {
        final Portfolio portfolio = context.proxy(PortfolioImpl.class);

        portfolio.setMarketPrice("MSFT", 326.19);
        portfolio.setMarketPrice("INTC", 52.86);

        context.dump("prices");

        portfolio.setHolding("Smith", "MSFT", 10);
        portfolio.setHolding("Smith", "INTC", 10);
        portfolio.setHolding("Jane", "INTC", 10);

        context.dump("portfolios");

        assertEquals(326.19 * 10, portfolio.presentValue("Smith", "MSFT"));
        assertEquals(52.86 * 10, portfolio.presentValue("Smith", "INTC"));
        assertEquals(52.86 * 10, portfolio.presentValue("Jane", "INTC"));
        assertEquals(326.19 * 10, portfolio.presentValue("Smith", "MSFT"));

        assertThrows(Exception.class, () -> assertEquals(171.18 * 10, portfolio.presentValue("Jane", "AAPL")));

        portfolio.setMarketPrice("AAPL", 171.18);
        portfolio.setHolding("Jane", "AAPL", 10);
        assertEquals(171.18 * 10, portfolio.presentValue("Jane", "AAPL"));

        portfolio.setMarketPrice("MSFT", 334.35);
        assertEquals(334.35 * 10, portfolio.presentValue("Smith", "MSFT"));
        portfolio.setMarketPrice("MSFT", 334.35);
        assertEquals(334.35 * 10, portfolio.presentValue("Smith", "MSFT"));
        portfolio.setMarketPrice("AAPL", 140.3);
        assertEquals(140.3 * 10, portfolio.presentValue("Jane", "AAPL"));
        assertEquals(140.3 * 10, portfolio.presentValue("Jane", "AAPL"));
    }
}