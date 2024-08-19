package vsevo.world;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IpFastReaderUtilTest {
    private static final int TEST_SIZE = 1024 * 2 + 512;
    private static final int TEST_SIZE_BIG = 1024 * 1024;

    ByteBuffer getData(int size) {
        Random random = new Random();
        random.nextInt(255);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append(random.nextInt(255));
            sb.append('.');
            sb.append(random.nextInt(255));
            sb.append('.');
            sb.append(random.nextInt(255));
            sb.append('.');
            sb.append(random.nextInt(255));
            sb.append('\n');
        }

        return ByteBuffer.wrap(sb.toString().getBytes());
    }


    @Test
    void read_test() {
        ByteBuffer data = getData(TEST_SIZE);
        long[] batch1 = new long[1024];
        long[] batch2 = new long[1024];
        long[] batch3 = new long[1024];


        int read1 = IpFastReaderUtil.read(data, batch1);
        int read2 = IpFastReaderUtil.read(data, batch2);
        int read3 = IpFastReaderUtil.read(data, batch3);
        assertEquals(TEST_SIZE, read1 + read2 + read3);
    }

    @Test
    void readBlock_test() {
        ByteBuffer data = getData(TEST_SIZE);
        BooleanArray4M arr = new BooleanArray4M();
        IpFastReaderUtil.readBlock(data, arr, Optional.empty(), Optional.empty());
        long sum = arr.getSum();

        data.position(0);
        String strData = new String(data.array());
        Set<String> hashSet = new HashSet<>(List.of(strData.split("\n")));
        assertEquals(hashSet.size(), sum);
    }

    @Test
    void readBlock_big_test() {
        ByteBuffer data = getData(TEST_SIZE_BIG);
        BooleanArray4M arr = new BooleanArray4M();
        IpFastReaderUtil.readBlock(data, arr, Optional.empty(), Optional.empty());
        long sum = arr.getSum();

        data.position(0);
        String strData = new String(data.array());
        Set<String> hashSet = new HashSet<>(List.of(strData.split("\n")));
        int sumTarget = hashSet.size();
        assertEquals(sumTarget, sum);
    }

}