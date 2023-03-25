import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RandomizedPrimsBenchmark {
    public static void main(String[] args) {
        RandomizedPrims rp;
        ConcurrentRandomizedPrims2 prp2;
        ConcurrentRandomizedPrims4 prp4;

        OutputStream output = null;
        try {
            output = new FileOutputStream("output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            // rp = new RandomizedPrims(1000, 1000);
            // prp2 = new ConcurrentRandomizedPrims2(50, 50);
            prp4 = new ConcurrentRandomizedPrims4(50, 50);
            long start = System.currentTimeMillis();
            // rp.run();
            // prp2.run();
            prp4.run();
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
