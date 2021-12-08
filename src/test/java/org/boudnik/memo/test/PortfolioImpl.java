package org.boudnik.memo.test;

import org.boudnik.memo.Library;

public class PortfolioImpl extends Library<Portfolio> implements Portfolio {

    @Override
    public double presentValue(String portfolio, String security) {
        return library().getMarketPrice(security) * library().holding(portfolio, security);
    }

    @Override
    public double getMarketPrice(String security) {
        return (double) context().get("prices", security);
    }

    @Override
    public double setMarketPrice(String security, double value) {
        return (double) context().set("prices", security, value);
    }

    @Override
    public double holding(String portfolio, String security) {
        return 10;
    }

}
