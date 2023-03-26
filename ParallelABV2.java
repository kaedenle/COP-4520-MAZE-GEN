
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;

class MazeObj2 extends JPanel{
	//grids
	public int[][] grid;
	
	//total amount of cells in rows and cols
	public int rows;
	public int cols;
	
	//public AtomicInteger remaining;
	//amount of rows and cols grid is split into
	public int RowAmount = 1;
	public int ColAmount = 1;
	//NESW
	//N = 1, E = 2, S = 4, W = 8 
	public int[] bitmasks = {1, 2, 4, 8};
	public int[][] deltas = {{-1, 0},{0, 1},{1, 0},{0, -1}};
	//Amount of threads
	public int N;
	public Node2[] nList; 
	
	
	public MazeObj2(int rows, int cols, int N) {
		//the actual maze
		grid = new int[rows][cols];
		this.rows = rows;
		this.cols = cols;
		this.N = N;
		
		//this.remaining = new AtomicInteger(rows * cols - 1);
		BoundsCalc();
		
		//fill grid with 0s
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++)
				grid[i][j] = 0;
		}
		//fill flaggrid with falses
		
		setPreferredSize(new Dimension(cols * 20, rows * 20));
	}
	
	public void SetNList(int n)
	{
		nList = new Node2[n];
	}
	private void BoundsCalc() {
		int MAX = N;
		int i = 1;
		//split up the grid evenly in equal shapes
		while(i < MAX) {
			if(N % i == 0) {
				ColAmount = N/i;
				RowAmount = i;
				MAX = ColAmount;
			}
			i++;
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
	            } 
	        	else {
	            	System.out.print("_");
	             }
	             if ((this.grid[y][x] & this.bitmasks[1]) != 0) {
	            	 if (x + 1 < this.cols) {
	            		 if (((this.grid[y][x] | this.grid[y][x + 1]) & this.bitmasks[2]) != 0) {
	            			 System.out.print(" ");
	                     } 
	            		 else {
	            			 System.out.print("_");
	                     }
	            	 }
	             } 
	             else {
	            	 System.out.print("|");
	             }
	        }
	        	System.out.printf("\n");
	    }
	
	}
	 protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        /*g.setColor(new Color(0, 255, 0));
	        g.fillRect(walk_cols * 20, walk_rows * 20, 20, 20);
	        g.setColor(new Color(0, 0, 0));*/
	        g.setColor(new Color(100, 100, 100));
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
	                /*if(amount > 3)
	                {
	                	g.setColor(new Color(220, 220, 220));
	                    g.fillRect(x + 1, y + 1, 19, 19);
	                    g.setColor(new Color(0, 0, 0));
	                }*/
	            }
	        }
	        //Draw the boundaries of each thread
	        //set stroke to be more noticable
	        Graphics2D g2 = (Graphics2D) g;
	        g2.setStroke(new BasicStroke(2));
	        
	        /*g.setColor(new Color(255, 0, 0));
	        //draw column boundaries
	        for(int i = 1; i < ColAmount; i++) {
	        	int max = (int)(((float)i/(float)ColAmount) * cols);
	        	g.drawLine(max * 20, 0, max * 20, 20 * cols);
	        }
	        //draw row boundaries
	        for(int i = 1; i < RowAmount; i++) {
	        	int max = (int)(((float)i/(float)RowAmount) * rows);
	        	g.drawLine(0, max * 20, rows * 20, max * 20);
	        }*/
	        g2.setStroke(new BasicStroke(1));
	    }
}
class Node2 implements Runnable{
	public static MazeObj2 Maze;
	public static int threashold = 50;
	public int posrows;
	public int poscols;
	public int startingrow;
	public int startingcol;
	private int ID;
	private int remaining;
	public static final Object lock = new Object();
	//how many threads are still working (used to tell all threads to start culling borders)
	public static AtomicInteger total;
	//0: UP 1: RIGHT 2: DOWN 3: LEFT
	public int[] bounds = new int[4];
	
	public Node2(int r, int c, int ID, int N, int rID, int cID) {
		this.ID = ID;
		if(Maze == null)
			MazeInit(r, c, N);
		BoundsCalc(rID, cID);
		//produce random position within box
		posrows = new Random().nextInt(Math.abs(bounds[2] - bounds[0])) + bounds[0];
		poscols = new Random().nextInt(Math.abs(bounds[3] - bounds[1])) + bounds[1];
		startingrow = posrows;
		startingcol = poscols;
	}
	
	public void BoundsCalc(int rID, int cID) {
		int ColGroup, RowGroup;
		int ColAmount = Maze.ColAmount;
		int RowAmount = Maze.RowAmount;
		ColGroup = cID % ColAmount;
		RowGroup = rID % RowAmount;
		bounds[1] = (int)((float)ColGroup/(float)ColAmount * Maze.cols);
		//not included (right)
		bounds[3] = (int)((float)(ColGroup + 1)/(float)ColAmount * Maze.cols);
		
		bounds[0] = (int)((float)RowGroup/(float)RowAmount * Maze.rows);
		//not included (down)
		bounds[2] = (int)((float)(RowGroup + 1)/(float)RowAmount* Maze.rows);
		//System.out.println("COLS: " + bounds[0] + " " + bounds[2] + "\nROWS:" + bounds[1] + " " + bounds[3]);
		
		//calculate remaining in little quadrant
		remaining = (bounds[2] - bounds[0]) * (bounds[3] - bounds[1]) - 1;
	}
	
	public static void MazeInit(int r, int c, int N) {
		Maze = new MazeObj2(r, c, N);
		total = new AtomicInteger(N);
	}
	//Erase portions of the right and bottom borders to make one spanning tree
	//wait till last thread finishes in their sector than start
	/*
	 * this is easier implementation wise. It doesn't matter if threads wait to cull, since this operation takes a set time
	 * every thread will finish culling at around the same time if they start at the same time. 
	 * Any time loss from not pre-culling the borders is minimal.
	*/
	public void CullBorders() {
		//wait till final thread is done
		synchronized(lock)
		{ 
		  total.decrementAndGet();
		  //if total is not 0, wait
		  if(0 < total.get())
		  { 
		    try {
		    	//System.out.println(Thread.currentThread().getId() + " Waiting " + count.get() + " " + Maze.N);
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  else {
			  lock.notifyAll();
		  }
		}
		//guarentee that at least one gets culled by culling last one if none was picked
		int cullcount = 0;
		if(bounds[3] != Maze.cols) {
			//move to whatever boundary is minus 1
			poscols = bounds[3] - 1;
			//move downwards, cull right
			for(int i = bounds[0]; i < bounds[2]; i++) {
				int decide = new Random().nextInt(100);
				//cull it
				if(decide < threashold || (i == bounds[2] - 1 && cullcount == 0)) {
					Maze.grid[i][poscols] |= Maze.bitmasks[1];
					Maze.grid[i][poscols + 1] |= Maze.bitmasks[3];
					cullcount++;
				}
			}
		}
		cullcount = 0;
		if(bounds[2] != Maze.rows) {
			posrows = bounds[2] - 1;
			//move right, cull downwards
			for(int i = bounds[1]; i < bounds[3]; i++) {
				int decide = new Random().nextInt(100);
				//cull it (if final square and haven't culled, cull)
				if(decide < threashold || (i == bounds[3] - 1 && cullcount == 0)) {
					Maze.grid[posrows][i] |= Maze.bitmasks[2];
					Maze.grid[posrows + 1][i] |= Maze.bitmasks[0];
					cullcount++;
				}
			}
		}
	}
	@Override
	public void run() {
		//while there's still tiles remaining (run at least once)
		while(remaining > 0) {
			int direction = new Random().nextInt(4);
			int[] delta = Maze.deltas[direction].clone();
			
			//keep thread within boundaries
			if(delta[1] == 0 && (delta[0] + posrows >= bounds[2] || delta[0] + posrows < bounds[0])) {
				//delta[0] *= -1;
				//direction = (direction + 2) % 4;
				continue;
			}
				
			if(delta[0] == 0 && (delta[1] + poscols >= bounds[3]|| delta[1] + poscols < bounds[1])) {
				//delta[1] *= -1;
				//direction = (direction + 2) % 4;
				continue;
			}
			posrows += delta[0];
			poscols += delta[1];
			//if next cell hadn't been visited, set it and the current cell to opposite directions
			//(OPENING A PATH BETWEEN THEM)
			if(Maze.grid[posrows][poscols] == 0) {
				Maze.grid[posrows][poscols] |= Maze.bitmasks[(direction + 2) % 4];
				Maze.grid[posrows - delta[0]][poscols - delta[1]] |= Maze.bitmasks[direction];
				remaining -= 1;
			}
		}
		CullBorders();
	}
}
public class ParallelABV2 {

	public static void main(String[] args) {
		//Hard limit on # of threads is N * N as each thread has one square allocated to it
		//Soft limit is N as you cannot split this up evenly in this implementation (gets weird after N)
		//Really, don't go past N/2
		//Node2 n = new Node2(10, 10, 0, 50);
		Path fileName = Path.of(Paths.get("").toAbsolutePath().toString() + "/parallelTimeOutput.txt");
		String output = "";
		int NumThreads = 2;
		int Dimensions = 1000;
		int NumRuns = 300;
		//insert cmd line args (Maze Size, Thread Num, Threashold)
		//Dimension of the maze, make sure it's initialized before this
		if(args.length >= 1)
			Dimensions = Integer.valueOf(args[0]);
		//number of threads
		if(args.length >= 2)
			NumThreads = Integer.valueOf(args[1]);
		if(args.length >= 3)
			NumRuns = Integer.valueOf(args[2]);
		if(args.length >= 4)
			Node2.threashold = Integer.valueOf(args[3]);
		
		for(int j = 0; j <= NumRuns; j++) {
			long startTime = System.nanoTime();
			Thread[] tList = new Thread[NumThreads];
			//reset maze?
			Node2.MazeInit(Dimensions, Dimensions, NumThreads);
			
			//some configurations over N can't produce subgrids of approximately equal cells and shape
			if(Node2.Maze.ColAmount > Dimensions || Node2.Maze.RowAmount > Dimensions) {
				System.out.println("Row or Col impossible for this amount");
				return;
			}

			Node2.Maze.SetNList(NumThreads);
			int rowcounter = 0, colcounter = 0;
			
			for(int i = 0; i < NumThreads; i++) {
				Node2.Maze.nList[i] = new Node2(Dimensions, Dimensions, i, NumThreads, rowcounter, colcounter);
				colcounter++;
				if(colcounter == Node2.Maze.ColAmount) {
					colcounter = 0;
					rowcounter++;
				}
				tList[i] = new Thread(Node2.Maze.nList[i]);
			}
			
			//start all threads
			for(Thread t : tList)
				t.start();
			
			//wait for all threads to die
			try{
	            for (Thread thread : tList) {
	                thread.join();
	            }
	        }
	        catch (InterruptedException e){
	            System.out.println(e);
	        }
			long endTime = System.nanoTime();
	        double totalTime = (endTime - startTime);
	        String timeOutput = Double.toString(totalTime/1000000);
            if(j == 0) continue;
	        output += timeOutput + "\n";
		}//end of runs
		try {
            Files.writeString(fileName, (output));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		JFrame frame = new JFrame("Maze Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(Node2.Maze);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
		Node2.Maze.printMaze();
	}

}
