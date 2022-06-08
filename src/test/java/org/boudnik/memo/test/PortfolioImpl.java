package org.boudnik.memo.test;

import org.boudnik.memo.Library;
import org.boudnik.memo.Parameter;

public class PortfolioImpl extends Library<Portfolio> implements Portfolio {

    @Override
    public double presentValue(String portfolio, String security) {
        return library().getMarketPrice(security) * library().getHolding(portfolio, security);
    }

    @Override
    public double getMarketPrice(String security) {
        return (double) context().get("prices", new Parameter("security", security));
    }

    @Override
    public double setMarketPrice(String security, double value) {
        return (double) context().set("prices", value, new Parameter("security", security));
    }

    @Override
    public double getHolding(String portfolio, String security) {
        return (double) context().get("portfolios",
                new Parameter("portfolio", portfolio),
                new Parameter("security", security));
    }


    public double setHolding(String portfolio, String security, double amount) {
        return (double) context().set("portfolios", amount,
                new Parameter("portfolio", portfolio),
                new Parameter("security", security));
    }

    @Override
    public void dump(String table) {
        context().dump(table);
    }
}
