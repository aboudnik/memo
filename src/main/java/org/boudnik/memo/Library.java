package org.boudnik.memo;

public interface Library {

    double presentValue(String portfolio, String security);

    double getMarketPrice(String security);

    double setMarketPrice(String security, double value);

    double holding(String portfolio, String security);

    double getAmount();
}
