package vsevo.world;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;


class BooleanArray4MTest {


    long get255() {
        int a = 255;
        int b = 255;
        int c = 255;
        int d = 255;
        long test = 0;
        test = test | (long) a << 8 * 3;
        test = test | (long) b << 8 * 2;
        test = test | (long) c << 8;
        test = test | (long) d;
        return test;
    }

    @Test
    void increment_and_get_test() throws IOException {
        try (BooleanArray4M booleanArray4M = new BooleanArray4M()) {
            long i255 = get255();
            booleanArray4M.increment(i255);
            for (int i = 0; i < 10; i++) {
                booleanArray4M.increment(i);
            }

            Assertions.assertEquals(1, booleanArray4M.get(i255));
            Assertions.assertEquals(0, booleanArray4M.get(i255 - 1));
            Assertions.assertEquals(1, booleanArray4M.get(0));
            Assertions.assertEquals(1, booleanArray4M.get(1));
            Assertions.assertEquals(1, booleanArray4M.get(9));
            Assertions.assertEquals(0, booleanArray4M.get(10));
        }
    }

    @Test
    void getSum() throws IOException {
        HashSet<Long> longs = new HashSet<>(10_000);
        try (BooleanArray4M booleanArray4M = new BooleanArray4M()) {
            long initSum = booleanArray4M.getSum();
            Assertions.assertEquals(0, initSum);
            for (int i = 0; i < 10_000; i++) {
                long temp = (long) (Math.random() * 100_000_000);
                longs.add(temp);
                booleanArray4M.increment(temp);
            }
            Assertions.assertEquals(longs.size(), booleanArray4M.getSum());
        }
    }
}