import java.util.concurrent.*;

public class Task2_Benchmark {
    // Верхняя граница поиска (дает ~2.5 - 3.5 секунды на 1 потоке вашего Ryzen 5)
    private static final long LIMIT = 12_000_000L;

    // Вычислительно-интенсивная проверка на простоту
    private static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        int[] threadCounts = {1, 2, 4, 8, 16, 32};
        int runs = 3;

        System.out.println("=========================================================================================");
        System.out.println("Запуск TASK 2 на AMD Ryzen 5 5625U (12 Logical Processors)... Подождите ~30-40 секунд");
        System.out.println("=========================================================================================");

        double t1_avg = 0;

        // Заголовок будущей таблицы
        System.out.printf("%-12s | %-10s | %-10s | %-10s | %-12s | %-10s | %-10s%n",
                "Threads (N)", "Run 1 (s)", "Run 2 (s)", "Run 3 (s)", "Avg Time (s)", "Speedup", "Efficiency");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (int threads : threadCounts) {
            double[] runTimes = new double[runs];
            double sumTime = 0;

            for (int r = 0; r < runs; r++) {
                // Принудительная сборка мусора перед замером
                System.gc();
                Thread.sleep(100);

                long startTime = System.nanoTime();

                ExecutorService executor = Executors.newFixedThreadPool(threads);
                CountDownLatch latch = new CountDownLatch(threads);

                for (int t = 0; t < threads; t++) {
                    final int threadId = t;
                    executor.submit(() -> {
                        long count = 0;
                        // Чередующееся распределение для идеальной балансировки нагрузки
                        for (long i = 2 + threadId; i <= LIMIT; i += threads) {
                            if (isPrime(i)) count++;
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();

                long endTime = System.nanoTime();
                double durationSec = (endTime - startTime) / 1_000_000_000.0;
                runTimes[r] = durationSec;
                sumTime += durationSec;
            }

            double avgTime = sumTime / runs;
            if (threads == 1) {
                t1_avg = avgTime;
            }

            double speedup = t1_avg / avgTime;
            double efficiency = (speedup / threads) * 100.0;

            System.out.printf("N = %-8d | %-10.3f | %-10.3f | %-10.3f | %-12.3f | %-9.2fx | %-9.1f%%%n",
                    threads, runTimes[0], runTimes[1], runTimes[2], avgTime, speedup, efficiency);
        }
        System.out.println("=========================================================================================");
        System.out.println("Готово! Скопируйте эти числа в таблицу Task 2.");
    }
}