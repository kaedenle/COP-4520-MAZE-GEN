import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MazeGeneratorDFS extends JPanel {
    private int rows;
    private int cols;
    private int cellSize;
    private boolean[][] visited;
    private boolean[][][] walls;
    private Random random;

    public MazeGeneratorDFS(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.visited = new boolean[rows][cols];
        this.walls = new boolean[rows][cols][4];
        this.random = new Random();
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        generateMaze();
    }

    private void generateMaze() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                visited[i][j] = false;
                walls[i][j][0] = true;
                walls[i][j][1] = true;
                walls[i][j][2] = true;
                walls[i][j][3] = true;
            }
        }

        dfs(0, 0);

        // Open entrance and exit
        walls[0][0][3] = false;
        walls[rows - 1][cols - 1][1] = false;
    }

    private void dfs(int i, int j) {
        visited[i][j] = true;
        int[] directions = {0, 1, 2, 3};
        shuffleArray(directions, random);

        for (int direction : directions) {
            int ni = i, nj = j;
            switch (direction) {
                case 0: // Up
                    ni = i - 1;
                    break;
                case 1: // Right
                    nj = j + 1;
                    break;
                case 2: // Down
                    ni = i + 1;
                    break;
                case 3: // Left
                    nj = j - 1;
                    break;
            }

            if (ni < 0 || ni >= rows || nj < 0 || nj >= cols || visited[ni][nj])
                continue;

            // Knock down the wall between (i, j) and (ni, nj)
            if (direction == 0) {
                walls[i][j][0] = false;
                walls[ni][nj][2] = false;
            } else if (direction == 1) {
                walls[i][j][1] = false;
                walls[ni][nj][3] = false;
            } else if (direction == 2) {
                walls[i][j][2] = false;
                walls[ni][nj][0] = false;
            } else {
                walls[i][j][3] = false;
                walls[ni][nj][1] = false;
            }

            dfs(ni, nj);
        }
    }

    private void shuffleArray(int[] array, Random rnd) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * cellSize;
                int y = i * cellSize;

                if (walls[i][j][0]) // Top wall
                    g.drawLine(x, y, x + cellSize, y);

                if (walls[i][j][1]) // Right wall
                    g.drawLine(x + cellSize, y, x + cellSize, y + cellSize);

                if (walls[i][j][2]) // Bottom wall
                    g.drawLine(x + cellSize, y + cellSize, x, y + cellSize);

                if (walls[i][j][3]) // Left wall
                    g.drawLine(x, y + cellSize, x, y);
            }
        }
    }

    public static void main(String[] args) {
        MazeGeneratorDFS maze = new MazeGeneratorDFS(20, 20, 20);
        JFrame frame = new JFrame("Maze Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(maze);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

