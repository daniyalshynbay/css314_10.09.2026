public class Task3_RaceCondition {
    private static final int NUM_THREADS = 10;
    private static final int ITERATIONS = 1_000_000;
    private static final int TARGET = 10_000_000;

    // Разделяемая переменная (volatile обеспечивает запись в память/кэш, провоцируя RMW-конфликт)
    private static volatile int raceCounter = 0;

    // Переменная и замок для синхронизированного теста
    private static int lockedCounter = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================================");
        System.out.println("TASK 3: Гонка данных (10 потоков по 1,000,000 инкрементов)");
        System.out.println("==================================================================");
        System.out.printf("%-8s | %-18s | %-18s%n", "Run", "Measured Output", "Error (10^7 - Act)");
        System.out.println("------------------------------------------------------------------");

        // Выполняем 10 прогонов без синхронизации
        for (int run = 1; run <= 10; run++) {
            raceCounter = 0;
            Thread[] threads = new Thread[NUM_THREADS];

            for (int t = 0; t < NUM_THREADS; t++) {
                threads[t] = new Thread(() -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        raceCounter++; // Неатомарный инкремент: Read -> Modify -> Write
                    }
                });
                threads[t].start();
            }

            for (Thread t : threads) {
                t.join();
            }

            int measured = raceCounter;
            int error = TARGET - measured;
            System.out.printf("#%-7d | %-18d | %-18d%n", run, measured, error);
        }

        System.out.println("==================================================================");
        System.out.println("Замер штрафа на синхронизацию (Q3.2):");

        // 1. Замер Unlocked
        raceCounter = 0;
        long startUnlocked = System.currentTimeMillis();
        Thread[] unlThreads = new Thread[NUM_THREADS];
        for (int t = 0; t < NUM_THREADS; t++) {
            unlThreads[t] = new Thread(() -> {
                for (int i = 0; i < ITERATIONS; i++) raceCounter++;
            });
            unlThreads[t].start();
        }
        for (Thread t : unlThreads) t.join();
        long endUnlocked = System.currentTimeMillis();
        long timeUnlocked = endUnlocked - startUnlocked;

        // 2. Замер Locked (с использованием synchronized)
        lockedCounter = 0;
        long startLocked = System.currentTimeMillis();
        Thread[] lckThreads = new Thread[NUM_THREADS];
        for (int t = 0; t < NUM_THREADS; t++) {
            lckThreads[t] = new Thread(() -> {
                for (int i = 0; i < ITERATIONS; i++) {
                    synchronized (lock) {
                        lockedCounter++;
                    }
                }
            });
            lckThreads[t].start();
        }
        for (Thread t : lckThreads) t.join();
        long endLocked = System.currentTimeMillis();
        long timeLocked = endLocked - startLocked;

        System.out.println("Результат для поля Q3.2:");
        System.out.printf("Unlocked = %d ms vs. Locked = %d ms (Locked Result = %d)%n",
                timeUnlocked, timeLocked, lockedCounter);
        System.out.printf("Замедление: ~%.1fx раз%n", (double) timeLocked / timeUnlocked);
        System.out.println("==================================================================");
    }
}