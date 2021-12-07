package org.boudnik.memo.test;

public interface Portfolio {

    double presentValue(String portfolio, String security);

    default double getMarketPrice(String security) {
        throw new IllegalArgumentException("must be cached");
    }

    double setMarketPrice(String security, double value);

    double holding(String portfolio, String security);

}
