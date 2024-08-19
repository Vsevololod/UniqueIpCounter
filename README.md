# Fast Ip counter
### This project is the result of reflections on [this particular task](https://github.com/Ecwid/new-job/blob/master/IP-Addr-Counter.md).

Thoughts on implementation and optimization:
 - The fastest way to read a file is using [mmap](https://man7.org/linux/man-pages/man2/mmap.2.html). In Java, it is supported "out of the box" and allows multiple threads to work simultaneously and independently on different parts of a large file.
 - Any IP address consists of four groups of numbers ranging from 0 to 255, so it can be packed into an int in memory (and unpacked back without collisions, though we won't need that). The presence or absence of an IP address can be represented as 1 or 0, meaning it fits in a single bit.
 - We need a data structure to store information about which IP addresses we have already encountered. Since we want to achieve a fast algorithm, we will use a bit array where the IP address will be the index of this array, and each bit will indicate the presence or absence of that IP address in our data. Thus, adding/checking the presence of an IP address in such an array by index will take O(1) time.
 - Regarding memory usage, we have a maximum of 4,294,967,296 IP addresses. Since one byte can hold 8 presence indicators, the maximum memory usage will be 536,870,912 bytes, which is equal to 512MB. There are more memory-efficient structures, such as an array of ranges of found values, but they are (1) slower and (2) in the worst case, will still occupy 256MB.
 - For systems with limited memory, we will provide the option to store the structure of found IP addresses on disk using mmap instead of keeping it in RAM.
 - We will also make the system multi-threaded to increase speed, as our algorithm can clearly be parallelized.

Limitations:
 - We assume that the input file is correct, with each line containing a valid IP address followed by a newline character (\n).

Testing and Execution:
```shell
chmod +x ./gradlew
./gradlew test
./gradlew jar
java -jar ./build/libs/ip-counter-0.1.0.jar -f /path/to/ip_addresses
```

To avoid misunderstandings, it is recommended to run in Docker:
```shell
docker build -t ip-app .
docker run -v /path/to/file/ip_addresses:/data/ip_addresses app -f /data/ip_addresses
```

If you have any questions or ideas, feel free to open issues.