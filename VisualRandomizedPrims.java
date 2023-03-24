import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;

public class VisualRandomizedPrims extends JPanel {
    private int width;
    private int height;
    private int[][] grid;
    private int N = 1;
    private int S = 2;
    private int E = 4;
    private int W = 8;

    /*
     * Note: The width and height need to be equal for Randomized Prim's Algorithm
     */
    private static int w = 50;
    private static int h = 50;

    public VisualRandomizedPrims(int[][] grid, int width, int height) {
        this.grid = grid;
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width * 20 + 1, height * 20 + 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int x = j * 20;
                int y = i * 20;

                if ((this.grid[i][j] & this.N) == 0) // Top wall
                    g.drawLine(x, y, x + 20, y);

                if ((this.grid[i][j] & this.E) == 0) // Right wall
                    g.drawLine(x + 20, y, x + 20, y + 20);

                if ((this.grid[i][j] & this.S) == 0) // Bottom wall
                    g.drawLine(x + 20, y + 20, x, y + 20);

                if ((this.grid[i][j] & this.W) == 0) // Left wall
                    g.drawLine(x, y + 20, x, y);
            }
        }
    }

    public static void main(String[] args) {
        ConcurrentRandomizedPrims prp = new ConcurrentRandomizedPrims(w, h);
        int[][] grid = prp.run();

        VisualRandomizedPrims visualizer = new VisualRandomizedPrims(grid, w, h);

        JFrame frame = new JFrame("Maze Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(visualizer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
