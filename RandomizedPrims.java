import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomizedPrims {
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

    public RandomizedPrims(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.frontiers = new ArrayList<int[]>();
        this.opposite = new HashMap<>();
        this.opposite.put(this.N, this.S);
        this.opposite.put(this.S, this.N);
        this.opposite.put(this.E, this.W);
        this.opposite.put(this.W, this.E);
    }

    public void addFrontier(int x, int y) {
        if (x >= 0 && y >= 0 && x < this.width && y < this.height && this.grid[y][x] == 0) {
            this.grid[y][x] |= this.FRONTIER;
            this.frontiers.add(new int[] { x, y });
        }
    }

    public void mark(int x, int y) {
        this.grid[y][x] |= IN;
        addFrontier(x - 1, y);
        addFrontier(x + 1, y);
        addFrontier(x, y - 1);
        addFrontier(x, y + 1);
    }

    public List<int[]> getNeighbors(int x, int y) {
        List<int[]> neighbors = new LinkedList<>();

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

        int x = r.nextInt(width);
        int y = r.nextInt(height);

        mark(x, y);

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
