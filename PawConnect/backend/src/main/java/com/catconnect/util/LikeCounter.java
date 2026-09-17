package com.catconnect.util;

public final class LikeCounter {

    private LikeCounter() {
    }

    public static int next(Integer current) {
        return (current == null ? 0 : current) + 1;
    }
}
