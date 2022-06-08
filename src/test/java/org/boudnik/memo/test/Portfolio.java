package org.boudnik.memo.test;

public interface Portfolio {

    double presentValue(String portfolio, String security);

    double getMarketPrice(String security);

    double setMarketPrice(String security, double value);

    double getHolding(String portfolio, String security);

    double setHolding(String portfolio, String security, double amount);

    void dump(String table);
}
