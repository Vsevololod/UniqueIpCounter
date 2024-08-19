package vsevo.world;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class IpFastReaderUtil {
    private static final byte POINT_NUMBER = 46;
    private static final byte NEW_LINE_NUMBER = 10;
    private static final byte FIRST_DIGIT_NUMBER = 48;
    public static final int MB = 1024 * 1024;

    private IpFastReaderUtil() {
    }

    public static void readBlock(
            ByteBuffer inputBuffer,
            BooleanArray4M arr,
            Optional<CountDownLatch> cdl,
            Optional<Integer> taskId) {
        int capacity = inputBuffer.capacity();
        int batchCount = capacity / MB;
        if(capacity%MB>0) {
            batchCount++;
        }

        for (int i = 0; i < batchCount; i++) {
            long[] batch = new long[MB];
            int ipsReadCount = IpFastReaderUtil.read(inputBuffer, batch);
            for (int j = 0; j < ipsReadCount; j++) {
                arr.increment(batch[j]);
            }
        }
        taskId.ifPresent(tid -> System.out.println("taskId: " + tid + " is completed"));
        cdl.ifPresent(CountDownLatch::countDown);
    }

    public static int read(ByteBuffer out, long[] arr) {
        int ipsReaded = 0;
        while (out.position() < out.capacity() && ipsReaded < arr.length) {
            int groupsIndex = 3; // ip address contains 4 groups of numbers
            long hashOfIp = 0;
            while (out.position() < out.capacity()) {
                byte[] tempSegment = new byte[3];
                byte temp = 0;
                byte segmentPosition = 0;
                while (out.position() < out.capacity()) {

                    temp = out.get();
                    if (temp == POINT_NUMBER || temp == NEW_LINE_NUMBER) {
                        break;
                    }
                    tempSegment[segmentPosition] = (byte) (temp - FIRST_DIGIT_NUMBER);
                    segmentPosition++;

                }
                int segment = switch (segmentPosition) {
                    case 3 -> tempSegment[0] * 100 + tempSegment[1] * 10 + tempSegment[2];
                    case 2 -> tempSegment[0] * 10 + tempSegment[1];
                    case 1 -> tempSegment[0];
                    default -> 0;
                };
                hashOfIp = hashOfIp | (long) segment << 8 * groupsIndex;
                groupsIndex--;
                if (temp == 10) {
                    break;
                }
            }
            arr[ipsReaded] = hashOfIp;
            ipsReaded++;
        }
        return ipsReaded;
    }
}
