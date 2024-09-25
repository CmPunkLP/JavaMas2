import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static int[] array;
    private static int currentLength;
    private static int numberOfThreads;
    private static final Object lockObject = new Object();
    private static AtomicInteger completedPairs;
    private static final Object resetEvent = new Object();

    public static void main(String[] args) {
        
        Random rand = new Random();
        currentLength = 16;
        array = rand.ints(currentLength, 1, 10).toArray();

        System.out.println("Початковий масив: " + Arrays.toString(array));

        numberOfThreads = Runtime.getRuntime().availableProcessors();
        completedPairs = new AtomicInteger(0);

        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);

        while (currentLength > 1) {
            // Обчислюємо пари
            completedPairs.set(0);
            int pairsToProcess = currentLength / 2;

            for (int i = 0; i < pairsToProcess; i++) {
                int index1 = i;
                int index2 = currentLength - 1 - i;
                threadPool.submit(() -> Worker(index1, index2, pairsToProcess));
            }

            // Очікуємо на завершення
            synchronized (resetEvent) {
                try {
                    resetEvent.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            currentLength = pairsToProcess;
            System.out.println("Проміжний масив: " + Arrays.toString(Arrays.copyOf(array, currentLength)));
        }

        System.out.println("Фінальна сума: " + array[0]);
        threadPool.shutdown();
    }

    private static void Worker(int index1, int index2, int pairsToProcess) {
        int sum = array[index1] + array[index2];
        array[index1] = sum;

        synchronized (lockObject) {
            if (completedPairs.incrementAndGet() == pairsToProcess) {
                synchronized (resetEvent) {
                    resetEvent.notify();
                }
            }
        }
    }
}
