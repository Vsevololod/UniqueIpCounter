package vsevo.world;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;


class BooleanArray4M implements Closeable {
    private final RandomAccessFile file;
    private final ByteBuffer out;
    private final int CAPACITY = Integer.MAX_VALUE / 4 + 1;
    private final Path swapFilePath;

    public BooleanArray4M() {
        file = null;
        swapFilePath = null;
        out = ByteBuffer.allocateDirect(CAPACITY);
    }

    public BooleanArray4M(String dirPath) throws IOException {
        swapFilePath = Path.of(dirPath, "swap.data");
        Files.deleteIfExists(swapFilePath);
        file = new RandomAccessFile(swapFilePath.toFile(), "rw");
        out = file.getChannel().
                map(FileChannel.MapMode.READ_WRITE, 0, CAPACITY);
        out.put(Integer.MAX_VALUE - 1, (byte) 0);
    }

    private void indexCheck(long index) {
        if (index < 0 || index > Integer.MAX_VALUE * 2L + 1L) {
            throw new IndexOutOfBoundsException();
        }
    }

    public long getSum() {
        long sum = 0;
        for (int index = 0; index < CAPACITY; index++) {
            byte temp = out.get(index);
            if (temp == 0) {
                continue;
            }
            for (int i = 0; i < 8; i++) {
                sum += (temp >> i) & 1;
            }
        }
        return sum;
    }

    public int get(long index) {
        indexCheck(index);

        int batch = (int) (index / 8);
        int pos = (int) (index % 8);

        return (out.get(batch) >> pos) & 1;
    }

    public void increment(long index) {
        indexCheck(index);

        int batch = (int) (index / 8L);
        int pos = (int) (index % 8L);

        byte temp = out.get(batch);
        temp |= (byte) (1 << pos);
        out.put(batch, temp);
    }

    @Override
    public void close() throws IOException {
        if (file != null) {
            file.close();
            Files.deleteIfExists(swapFilePath);
        }
    }
}