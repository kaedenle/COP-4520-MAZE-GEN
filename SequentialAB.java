
import java.awt.Graphics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;

class Maze extends JPanel{
	public int[][] grid;
	private int rows;
	private int cols;
	private int walk_rows;
	private int walk_cols;
	private int[] bitmasks = {1, 2, 4, 8};
	public JFrame frame;
	public int[][] deltas = {{-1, 0},{0, 1},{1, 0},{0, -1}};
    private boolean StepMode;
	
	public Maze(int rows, int cols, boolean StepMode) {
		grid = new int[rows][cols];
		this.rows = rows;
		this.cols = cols;
		if(StepMode)
		{
			frame = new JFrame("Maze Generator");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setResizable(true);
		    frame.setSize(rows*30,cols*30);
		    frame.add(this);
		    frame.setLocationRelativeTo(null);
		    frame.setVisible(true);
		}
		
		this.generate();
		this.StepMode = StepMode;
		
		setPreferredSize(new Dimension(cols * 20, rows * 20));
	}
	
	//generate the maze
	public void generate() {
		walk_rows = new Random().nextInt(rows);
		walk_cols = new Random().nextInt(cols);
		
		//fill grid with 0s
		for(int i = 0; i < rows; i++) 
			Arrays.fill(grid[i], 0);
		
		
		int remaining = rows * cols - 1;
		//NESW
		//N = 1, E = 2, S = 4, W = 8 
		//your next step
		//north, east, south, west
		
		//do the stuff
		while(remaining > 0) {
			int direction;
			direction = new Random().nextInt(4);
			int[] delta = deltas[direction].clone();
			
			//if delta is out of bounds make it go other way
			//move vertically
			if(delta[1] == 0 && (delta[0] + walk_rows >= rows || delta[0] + walk_rows < 0)) {
				delta[0] *= -1;
				direction = (direction + 2) % 4;
			}
				
			if(delta[0] == 0 && (delta[1] + walk_cols >= cols || delta[1] + walk_cols < 0)) {
				delta[1] *= -1;
				direction = (direction + 2) % 4;
			}
			//if next cell hadn't been visited, set it and the current cell to opposite directions
			//(OPENING A PATH BETWEEN THEM)
			if(grid[walk_rows + delta[0]][walk_cols + delta[1]] == 0) {
				grid[walk_rows + delta[0]][walk_cols + delta[1]] |= bitmasks[(direction + 2) % 4];
				grid[walk_rows][walk_cols] |= bitmasks[direction];
				remaining -= 1;
			}
			walk_rows += delta[0];
			walk_cols += delta[1];
			//Optimization to prevent going backwards
			//System.out.println(directions[direction] + " " + grid[walk_rows][walk_cols]);
			if(StepMode) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				frame.validate();
				frame.repaint();
			}
			
		}
	}
	public void printRaw() {
		for(int[] rows : this.grid) {
			for(int col: rows) {
				System.out.print(col + " ");
			}
			System.out.println();
		}
	}
	 public void printMaze() {
	        System.out.print(" ");
	        for (int i = 0; i < this.cols * 2 - 1; i++) {
	            System.out.print("_");
	        }
	        System.out.printf(" \n");
	
	        for (int y = 0; y < this.rows; y++) {
	            System.out.print("|");
	            for (int x = 0; x < this.cols; x++) {
	                if ((this.grid[y][x] & this.bitmasks[2]) != 0) {
	                    System.out.print(" ");
	                } else {
	                    System.out.print("_");
	                }
	
	                if ((this.grid[y][x] & this.bitmasks[1]) != 0) {
	                    if (x + 1 < this.cols) {
	                        if (((this.grid[y][x] | this.grid[y][x + 1]) & this.bitmasks[2]) != 0) {
	                            System.out.print(" ");
	                        } else {
	                            System.out.print("_");
	                        }
	                    }
	                } else {
	                    System.out.print("|");
	                }
	            }
	            System.out.printf("\n");
	        }
	
	    }
	 @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 255, 0));
        g.fillRect(walk_cols * 20, walk_rows * 20, 20, 20);
        g.setColor(new Color(0, 0, 0));
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
            	int amount = 0;
                int x = j * 20;
                int y = i * 20;

                if ((this.grid[i][j] & bitmasks[0]) == 0) // Top wall
                {
                	g.drawLine(x, y, x + 20, y);
                	amount += 1;
                }     
                if ((this.grid[i][j] & bitmasks[1]) == 0) // Right wall
                {
                	g.drawLine(x + 20, y, x + 20, y + 20);
                	amount += 1;
                }
                if ((this.grid[i][j] & bitmasks[2]) == 0) // Bottom wall
                {    
                	g.drawLine(x + 20, y + 20, x, y + 20);
                	amount += 1;
                }    
                if ((this.grid[i][j] & bitmasks[3]) == 0) // Left wall
                {
                	g.drawLine(x, y + 20, x, y);
                	amount += 1;
                }
                if(amount > 3)
                {
                	g.setColor(new Color(220, 220, 220));
                    g.fillRect(x + 1, y + 1, 19, 19);
                    g.setColor(new Color(0, 0, 0));
                }
            }
        }
    }
        
}

public class SequentialAB {
	
	public static void main(String[] args) {
		boolean stepMode = false;
		int timesRun = 50;
        int N = 20;
        Maze m = new Maze(N, N, stepMode);
		Path fileName = Path.of(Paths.get("").toAbsolutePath().toString() + "/timeOutput.txt");
		String output = "";
		if(!stepMode) {
			for(int i = 0; i <= timesRun; i++) {
				long startTime = System.nanoTime();
				//STUFF HERE
				m = new Maze(N, N, stepMode);
				//m.printRaw();
				//m.printMaze();
				long endTime = System.nanoTime();
		        double totalTime = (endTime - startTime);
		        String timeOutput = (totalTime/1000000 + " ms");
	            if(i == 0) continue;
		        output += timeOutput + "\n";
			}

	        try {
	            Files.writeString(fileName, (output));
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		}
		
        
	}

}
