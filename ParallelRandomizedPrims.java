import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParallelRandomizedPrims {
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
    // for created the threads
    private int[][] directions = {
            { -1, 0 }, // up
            { 1, 0 }, // down
            { 0, -1 }, // right
            { 0, 1 } // left
    };
    private Thread[] threads = new Thread[4];

    public ParallelRandomizedPrims(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.frontiers = new LinkedList<int[]>();
        this.opposite = new HashMap<>();
        this.opposite.put(this.N, this.S);
        this.opposite.put(this.S, this.N);
        this.opposite.put(this.E, this.W);
        this.opposite.put(this.W, this.E);
    }

    public void mark(int x, int y) {
        /*
         * This class represents a parallel version of the addFrontier method in the
         * sequential RandomizedPrims code.
         */
        class MarkThread implements Runnable {
            private int x;
            private int y;

            public MarkThread(int x, int y) {
                this.x = x;
                this.y = y;
            }

            /*
             * This method checks to see if the index for the cell is not out of bounds and
             * is equal to 0. If both are true, add to the frontier cell list.
             */
            @Override
            public void run() {
                if (this.x >= 0 && this.y >= 0 && this.x < width && this.y < height && grid[y][x] == 0) {
                    grid[y][x] |= FRONTIER;
                    frontiers.add(new int[] { this.x, this.y });
                }
            }
        }

        this.grid[y][x] |= this.IN;

        int i = 0;

        /*
         * Create four threads (one for each potential frontier cell).
         */
        for (int[] d : this.directions) {
            this.threads[i] = new Thread(new MarkThread(x + d[0], y + d[1]));
            this.threads[i].start();
            i += 1;
        }

        for (Thread t : this.threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            int[] f = this.frontiers.remove(r.nextInt(this.frontiers.size()));
            int fx = f[0];
            int fy = f[1];

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

        return this.grid;
    }
}