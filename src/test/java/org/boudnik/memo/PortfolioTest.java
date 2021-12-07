package org.boudnik.memo;

import org.boudnik.memo.test.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {
    private static Cache cache;

    @BeforeAll
    static void setUp() {
        cache = new Cache();
    }

    @Test
    void test() {
        final Portfolio portfolio = cache.proxy(PortfolioImpl.class);
        portfolio.setMarketPrice("MSFT", 326.19);
        portfolio.setMarketPrice("INTC", 52.86);
        assertEquals(326.19 * 10, portfolio.presentValue("Smith", "MSFT"));
        portfolio.setMarketPrice("MSFT", 334.35);
        assertEquals(334.35 * 10, portfolio.presentValue("Smith", "MSFT"));
        assertEquals(52.86 * 10, portfolio.presentValue("Smith", "INTC"));
    }
}