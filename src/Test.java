public class Test {
    public static void main(String[] args) {
        long t0 = System.nanoTime();

        for (int i = 0; i < 2000000000; i++) {
            if (i % 200000000 == 0) {
                System.out.println((i / 20000000) + "% complete.");
            }
        }
        long t1 = System.nanoTime();
        System.out.println((t1 - t0) / 1e9 + " seconds");
    }
}
