import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentRandomizedPrims {
    private int width;
    private int height;
    private int[][] grid;
    private List<int[]> frontiers;
    private int IN = 0x10;
    private int FRONTIER = 0x20;
    private int N = 1;
    private int S = 2;
    private int E = 4;
    private int W = 8;
    private Map<Integer, Integer> opposite;
    // for creating the threads
    private int[][] directions = {
            { -1, 0 }, // up
            { 1, 0 }, // down
            { 0, -1 }, // right
            { 0, 1 } // left
    };
    private Thread[] threads = new Thread[2];
    private MarkThread[] markThreads = new MarkThread[2];
    private AtomicBoolean mark1 = new AtomicBoolean();
    private AtomicBoolean mark2 = new AtomicBoolean();

    public ConcurrentRandomizedPrims(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.frontiers = new ArrayList<int[]>();
        this.opposite = new HashMap<>();
        this.opposite.put(this.N, this.S);
        this.opposite.put(this.S, this.N);
        this.opposite.put(this.E, this.W);
        this.opposite.put(this.W, this.E);
        this.markThreads[0] = new MarkThread(0, 0, 0, 0, this.mark1);
        this.threads[0] = new Thread(this.markThreads[0]);
        this.markThreads[1] = new MarkThread(0, 0, 0, 0, this.mark2);
        this.threads[1] = new Thread(this.markThreads[1]);
    }

    class MarkThread implements Runnable {
        private int x1;
        private int y1;
        private int x2;
        private int y2;
        private AtomicBoolean mark;
        private boolean running;

        public MarkThread(int x1, int y1, int x2, int y2, AtomicBoolean mark) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.mark = mark;
            this.running = true;
        }

        public void setXandY(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public void stop() {
            this.running = false;
        }

        public void addFrontierCells() {
            synchronized (frontiers) {
                if (this.x1 >= 0 && this.y1 >= 0 && this.x1 < width && this.y1 < height && grid[y1][x1] == 0) {
                    grid[y1][x1] |= FRONTIER;
                    frontiers.add(new int[] { this.x1, this.y1 });
                }

                if (this.x2 >= 0 && this.y2 >= 0 && this.x2 < width && this.y2 < height && grid[y2][x2] == 0) {
                    grid[y2][x2] |= FRONTIER;
                    frontiers.add(new int[] { this.x2, this.y2 });
                }
            }
        }

        /*
         * This method checks to see if the index for the cell is not out of bounds and
         * is equal to 0. If both are true, add to the frontier cell list.
         */
        @Override
        public void run() {
            while (this.running) {
                if (this.mark.get() == false)
                    continue;
                this.addFrontierCells();
                this.mark.set(false);
            }
        }
    }

    public void mark(int x, int y) {
        /*
         * This class represents a parallel version of the addFrontier method in the
         * sequential RandomizedPrims code.
         */

        this.grid[y][x] |= this.IN;

        /*
         * Create two threads
         */

        this.markThreads[0].setXandY(x + this.directions[0][0], y + this.directions[0][1], x + this.directions[1][0],
                y + this.directions[1][1]);
        this.mark1.set(true);
        this.markThreads[1].setXandY(x + this.directions[2][0], y + this.directions[2][1], x + this.directions[3][0],
                y + this.directions[3][1]);
        this.mark2.set(true);

        while (this.mark1.get() || this.mark2.get()) {
        }
    }

    /*
     * Returns a list of a cell's neighbors that are marked in
     */
    public List<int[]> getNeighbors(int x, int y) {
        List<int[]> neighbors = new LinkedList<>();

        /*
         * The outer if statements make sure the values of x and y are not out of bounds
         * for the array.
         * 
         * The inner if statements check to see if the neighbor has already been marked
         * as inside the maze. If so, add that cell to the list of neighbors
         */

        if (x > 0) {
            if ((grid[y][x - 1] & this.IN) != 0) {
                neighbors.add(new int[] { x - 1, y });
            }
        }
        if (x + 1 < this.width) {
            if ((grid[y][x + 1] & this.IN) != 0) {
                neighbors.add(new int[] { x + 1, y });
            }
        }
        if (y > 0) {
            if ((grid[y - 1][x] & this.IN) != 0) {
                neighbors.add(new int[] { x, y - 1 });
            }
        }
        if (y + 1 < this.height) {
            if ((grid[y + 1][x] & this.IN) != 0) {
                neighbors.add(new int[] { x, y + 1 });
            }
        }

        return neighbors;
    }

    /*
     * Returns the direction the neighbor cell is in from the frontier cell
     */
    public int getDirection(int fx, int fy, int tx, int ty) {
        if (fx < tx) {
            return this.E;
        } else if (fx > tx) {
            return this.W;
        } else if (fy < ty) {
            return this.S;
        } else if (fy > ty) {
            return this.N;
        } else {
            System.out.println("Error with direction function");
            return 0;
        }
    }

    public int[][] run() {
        Random r = new Random();

        int x = r.nextInt(this.width);
        int y = r.nextInt(this.height);

        this.threads[0].start();
        this.threads[1].start();

        mark(x, y);

        /*
         * While there exists frontier cells in the grid:
         * 
         * 1. Remove a frontier cell from the list
         * 2. Get the neighbors of that frontier cell
         * 3. Select a random neighbor
         * 4. Add the direction that neighbor is in from the frontier cell
         * 5. Add the direction that the frontier cell is in from the neighbor
         * 6. Mark the frontier cell as being inside the maze
         */
        while (!this.frontiers.isEmpty()) {
            int[] f;
            int fx;
            int fy;
            try {
                f = this.frontiers.remove(r.nextInt(this.frontiers.size()));
                fx = f[0];
                fy = f[1];
            } catch (NullPointerException e) {
                e.printStackTrace();
                break;
            }

            List<int[]> neighbors = getNeighbors(fx, fy);

            int[] n = neighbors.get(r.nextInt(neighbors.size()));
            int nx = n[0];
            int ny = n[1];

            int dir = getDirection(fx, fy, nx, ny);
            this.grid[fy][fx] |= dir;
            int opp = this.opposite.get(dir);
            this.grid[ny][nx] |= opp;
            mark(fx, fy);
        }

        this.markThreads[0].stop();
        this.markThreads[1].stop();

        return this.grid;
    }
}