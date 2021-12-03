package org.boudnik.memo;

class LibImpl implements All {
    @Override
    public int inc(int x) {
        return x + 1;
    }

    @Override
    public int dec(int x) {
        return x - 1;
    }
}
