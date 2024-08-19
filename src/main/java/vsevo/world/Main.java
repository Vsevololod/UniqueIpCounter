package vsevo.world;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    private static final String HOW_TO_USE = "Usage: java -jar counter.jar -f /path/to/file [-sd /path/to/temp_dir]\n" +
            "\t -sd id optional parameter; only if you wont to use external swap";


    private static List<Long> countAndGetBatchPointers(RandomAccessFile file) throws IOException {
        long GB = 1024 * 1024 * 1024;
        int bathCount = (int) (file.length() / GB);
        if (bathCount == 0) {
            return List.of(0L, file.length());
        }
        ArrayList<Long> bathPointers = new ArrayList<>(bathCount);
        bathPointers.add(0L);
        for (int i = 1; i <= bathCount; i++) {
            file.seek(GB * i);
            int j = 0;
            while (true) {
                byte b = file.readByte();
                if (b == 10) {
                    break;
                } else {
                    j++;
                }
            }
            bathPointers.add(GB * i + (long) j);
        }
        bathPointers.add(file.length());
        return bathPointers;
    }


    private static long readAndCount(String fileName, String swapDirectory) throws IOException {
        long sum = 0;
        try (RandomAccessFile file = new RandomAccessFile(fileName, "r");
             BooleanArray4M arr = swapDirectory != null ? new BooleanArray4M(swapDirectory) : new BooleanArray4M()) {
            List<Long> pointers = countAndGetBatchPointers(file);
            System.out.println("Tasks count: " + pointers.size());

            try (ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4)) {
                CountDownLatch countDownLatch = new CountDownLatch(pointers.size() - 1);
                for (int i = 0; i < pointers.size() - 1; i++) {
                    ByteBuffer tmp = file.getChannel().map(
                            FileChannel.MapMode.READ_ONLY,
                            pointers.get(i),
                            pointers.get(i + 1) - pointers.get(i));
                    int finalI = i;
                    threadPoolExecutor.execute(() -> IpFastReaderUtil.readBlock(
                            tmp, arr, Optional.of(countDownLatch), Optional.of(finalI)));
                }
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sum = arr.getSum();
        }
        return sum;
    }


    public static void main(String[] args) throws IOException {
        String fileName = null;
        String swapDirectory = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f") && i < args.length - 1) {
                fileName = args[i + 1];
                i++;
            }
            if (args[i].equals("-sd") && i < args.length - 1) {
                swapDirectory = args[i + 1];
                i++;
            }
            if (args[i].equals("--help")) {
                System.out.println(HOW_TO_USE);
            }
        }
        if (fileName == null) {
            System.out.println(HOW_TO_USE + "\n-f is mandatory parameter!");
            System.exit(0);
        }

        if (!Files.exists(Path.of(fileName))) {
            System.out.println("file not found: " + fileName);
        }

        if (swapDirectory != null && !Files.isDirectory(Path.of(swapDirectory))) {
            System.out.println("Directory is not a directory: " + swapDirectory);
        }
        long result = readAndCount(fileName, swapDirectory);
        System.out.println("you have: " + result + " unique ip");
    }
}