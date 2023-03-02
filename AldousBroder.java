import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

class Maze{
	public int[][] grid;
	private int rows;
	private int cols;
	private int[] bitmasks = {1, 2, 4, 8};
	
	public Maze(int rows, int cols) {
		grid = new int[rows][cols];
		this.rows = rows;
		this.cols = cols;
		this.generate();
	}
	
	//generate the maze
	public void generate() {
		int walk_rows = new Random().nextInt(rows);
		int walk_cols = new Random().nextInt(cols);
		
		//fill grid with 0s
		for(int i = 0; i < rows; i++) 
			Arrays.fill(grid[i], 0);
		
		
		int remaining = rows * cols - 1;
		//NESW
		//N = 1, E = 2, S = 4, W = 8 
		//your next step
		//north, east, south, west
		int[][] deltas = {{-1, 0},{0, 1},{1, 0},{0, -1}};
		int lastDirection = -1;
		String[] directions = {"NORTH", "EAST", "SOUTH", "WEST"};
		
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
			lastDirection = direction;
			//System.out.println(directions[direction] + " " + grid[walk_rows][walk_cols]);
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
        
}

public class AldousBroder {
	
	public static void main(String[] args) {
		int timesRun = 300;
        int N = 200;
        Maze m;
		Path fileName = Path.of(Paths.get("").toAbsolutePath().toString() + "/timeOutput.txt");
		String output = "";
		for(int i = 0; i <= timesRun; i++) {
			long startTime = System.nanoTime();
			//STUFF HERE
			m = new Maze(N, N);
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
