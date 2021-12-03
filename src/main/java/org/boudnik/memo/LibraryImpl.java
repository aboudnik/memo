package org.boudnik.memo;

public class LibraryImpl implements Library {

    static Library LIBRARY;
//    private static final Library LIBRARY = new Memoizer().proxy(new LibraryImpl());

    public LibraryImpl() {
    }

    public static Library library() {
        return LIBRARY;
    }

    @Override
    public double presentValue(String portfolio, String security) {
        return library().getMarketPrice(security) * library().holding(portfolio, security);
    }
//reoineoirb
    @Override
    /**
     *
     */
    public double getMarketPrice(String security) {
        throw new IllegalStateException("must be cached");
    }

    @Override
    public double setMarketPrice(String security, double value) {
        return value;
    }

    @Override
    public double holding(String portfolio, String security) {
        return library().getAmount();
    }

    @Override
    public double getAmount() {
        return 10;
    }
}
