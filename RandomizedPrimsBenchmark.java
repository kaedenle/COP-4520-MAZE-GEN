import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RandomizedPrimsBenchmark {
    public static void main(String[] args) {
        RandomizedPrims rp;
        ConcurrentRandomizedPrims prp;

        OutputStream output = null;
        try {
            output = new FileOutputStream("output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            // rp = new RandomizedPrims(5000, 5000);
            prp = new ConcurrentRandomizedPrims(100, 100);
            long start = System.currentTimeMillis();
            // rp.run();
            prp.run();
            long elapsedTime = System.currentTimeMillis() - start;
            try {
                output.write(String.valueOf(elapsedTime).getBytes());
                output.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
