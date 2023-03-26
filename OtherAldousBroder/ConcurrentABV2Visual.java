package OtherAldousBroder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;
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
	
	public int[] bitmasks = {1, 2, 4, 8};
	public int[][] deltas = {{-1, 0},{0, 1},{1, 0},{0, -1}};
	public Node2[] nList; 
	//Amount of threads
	public int N;
	public JFrame frame;
	public int CellSize = 20;
	
	public MazeObj2(int rows, int cols, int N) {
		//the actual maze
		grid = new int[rows][cols];
		this.rows = rows;
		this.cols = cols;
		this.N = N;
		//frame stuff
		frame = new JFrame("Maze Generator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setResizable(true);
	    frame.setSize(rows*30,cols*30);
	    frame.add(this);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
		
		//this.remaining = new AtomicInteger(rows * cols - 1);
		BoundsCalc();
		
		//fill grid with 0s
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++)
				grid[i][j] = 0;
		}
		
		setPreferredSize(new Dimension(cols * CellSize, rows * CellSize));
	}
	public void SetNList(int n)
	{
		nList = new Node2[n];
	}
	
	//calculate how many seperate cols and rows there will be based on number of threads
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
	        g.setColor(new Color(100, 100, 100));
	        for (int i = 0; i < this.rows; i++) {
	            for (int j = 0; j < this.cols; j++) {
	            	int amount = 0;
	                int x = j * CellSize;
	                int y = i * CellSize;

	                if ((this.grid[i][j] & bitmasks[0]) == 0) // Top wall
	                {
	                	g.drawLine(x, y, x + CellSize, y);
	                	amount += 1;
	                }     
	                if ((this.grid[i][j] & bitmasks[1]) == 0) // Right wall
	                {
	                	g.drawLine(x + CellSize, y, x + CellSize, y + CellSize);
	                	amount += 1;
	                }
	                if ((this.grid[i][j] & bitmasks[2]) == 0) // Bottom wall
	                {    
	                	g.drawLine(x + CellSize, y + CellSize, x, y + CellSize);
	                	amount += 1;
	                }    
	                if ((this.grid[i][j] & bitmasks[3]) == 0) // Left wall
	                {
	                	g.drawLine(x, y + CellSize, x, y);
	                	amount += 1;
	                }
	                if(amount > 3)
	                {
	                	g.setColor(new Color(220, 220, 220));
	                    g.fillRect(x + 1, y + 1, CellSize - 1, CellSize - 1);
	                    g.setColor(new Color(0, 0, 0));
	                }
	            }
	        }
	        g.setColor(new Color(0, 255, 0));
        	for(Node2 n : nList) {
        		if(n == null) continue;
		        g.fillRect(n.poscols * CellSize, n.posrows * CellSize, CellSize, CellSize);
        	}
	        //Draw the boundaries of each thread
	        //set stroke to be more noticable
	        Graphics2D g2 = (Graphics2D) g;
	        g2.setStroke(new BasicStroke(2));
	        
	        g.setColor(new Color(255, 0, 0));
	        //draw column boundaries
	        /*for(int i = 1; i < ColAmount; i++) {
	        	int max = (int)(((float)i/(float)ColAmount) * cols);
	        	g.drawLine(max * CellSize, 0, max * CellSize, CellSize * cols);
	        }
	        //draw row boundaries
	        for(int i = 1; i < RowAmount; i++) {
	        	int max = (int)(((float)i/(float)RowAmount) * rows);
	        	g.drawLine(0, max * CellSize, rows * CellSize, max * CellSize);
	        }*/
	        g2.setStroke(new BasicStroke(1));
	    }
}
class Node2 implements Runnable{
	public static MazeObj2 Maze;
	public int posrows;
	public int poscols;
	public int startingrow;
	public int startingcol;
	private int ID;
	private int remaining;
	public static AtomicInteger count = new AtomicInteger(0);
	public static final Object lock = new Object();
	public static final Object lockOther = new Object();
	public static AtomicInteger total;
	public static AtomicInteger totalTotally;
	public static int threashold = 50;
	private boolean hitflag = false;
	
	//0: UP 1: RIGHT 2: DOWN 3: LEFT
	public int[] bounds = new int[4];
	
	public Node2(int r, int c, int ID, int N, int rID, int cID) {
		this.ID = ID;
		if(Maze == null)
			MazeInit(r, c, N);
		BoundsCalc(rID, cID);
		//produce random position within box
		posrows = new Random().nextInt(bounds[2] - bounds[0]) + bounds[0];
		poscols = new Random().nextInt(bounds[3] - bounds[1]) + bounds[1];
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
		//not included
		bounds[3] = (int)((float)(ColGroup + 1)/(float)ColAmount * Maze.cols);
		
		bounds[0] = (int)((float)RowGroup/(float)RowAmount * Maze.rows);
		//not included
		bounds[2] = (int)((float)(RowGroup + 1)/(float)RowAmount* Maze.rows);
		//System.out.println("COLS: " + bounds[0] + " " + bounds[2] + "\nROWS:" + bounds[1] + " " + bounds[3]);
		
		//calculate remaining in little quadrant
		remaining = (bounds[2] - bounds[0]) * (bounds[3] - bounds[1]) - 1;
	}
	
	public static void MazeInit(int r, int c, int N) {
		Maze = new MazeObj2(r, c, N);
		//total amount of threads still doing work
		total = new AtomicInteger(N);
		totalTotally = new AtomicInteger(N);
	}
	public void WaitTill() {
		if(hitflag == false)
			totalTotally.decrementAndGet();
		synchronized(lock)
		{
		if(count.incrementAndGet() < totalTotally.get())
		{ 
		    try {
		    	//System.out.println(Thread.currentThread().getId() + " Waiting " + count.get() + " " + Maze.N);
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			Maze.frame.validate();
			  Maze.frame.repaint();
			  try {
				Thread.sleep(10);
			  } 
			  catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			  }
			  count.set(0);
			  lock.notifyAll();
		}
		}
	}
	public void CullBorders() {
		//wait till final thread is done
		synchronized(lockOther)
		{ 
		  //if total is not 0, wait
		  if(0 < total.get())
		  { 
		    try {
		    	//System.out.println(Thread.currentThread().getId() + " Waiting " + count.get() + " " + Maze.N);
		    	lockOther.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  else {
			  lockOther.notifyAll();
		  }
		}
		//guarentee that at least one gets culled by culling last one if none was picked
		int cullcount = 0;
		if(bounds[3] != Maze.cols) {
			hitflag = true;
			//move to whatever boundary is minus 1
			poscols = bounds[3] - 1;
			//move downwards, cull right
			for(int i = bounds[0]; i < bounds[2]; i++) {
				int decide = new Random().nextInt(100);
				//cull it
				posrows = i;
				if(decide < threashold || (i == bounds[2] - 1 && cullcount == 0)) {
					Maze.grid[posrows][poscols] |= Maze.bitmasks[1];
					Maze.grid[posrows][poscols + 1] |= Maze.bitmasks[3];
					cullcount++;
				}
				WaitTill();
			}
		}
		cullcount = 0;
		if(bounds[2] != Maze.rows) {
			hitflag = true;
			posrows = bounds[2] - 1;
			//move right, cull downwards
			for(int i = bounds[1]; i < bounds[3]; i++) {
				int decide = new Random().nextInt(100);
				//cull it (if final square and haven't culled, cull)
				poscols = i;
				if(decide < threashold || (i == bounds[3] - 1 && cullcount == 0)) {
					Maze.grid[posrows][poscols] |= Maze.bitmasks[2];
					Maze.grid[posrows + 1][poscols] |= Maze.bitmasks[0];
					cullcount++;
				}
				WaitTill();
			}
		}
		hitflag = false;
		WaitTill();
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
			//for visualization purposes
			synchronized(lock)
			{ 
			  count.incrementAndGet();
			  if(count.get() < total.get())
			  { 
			    try {
			    	//System.out.println(Thread.currentThread().getId() + " Waiting " + count.get() + " " + Maze.N);
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
			  if(count.get() != 0 || remaining <= 0) {
				  //System.out.println(Thread.currentThread().getId() + " Unlocking");
				  Maze.frame.validate();
				  Maze.frame.repaint();
				  /*try {
					Thread.sleep(1);
				  } 
				  catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				  }*/
				  count.set(0);
				  lock.notifyAll();
				  if(remaining <= 0)
					  total.decrementAndGet();
			  }
			}
		}
		CullBorders();
	}
}
public class ConcurrentABV2Visual {

	public static void main(String[] args) {
		//Hard limit on # of threads is N * N as each thread has one square allocated to it
		//Soft limit is half of N * N as anything beyond that produces strange mazes
		//Node2 n = new Node2(10, 10, 0, 50);
		int NumThreads = 20;
		int Dimensions = 100;
		Node2.threashold = 50;
		Thread[] tList = new Thread[NumThreads];
		Node2.MazeInit(Dimensions, Dimensions, NumThreads);
		Node2.Maze.SetNList(NumThreads);
		Node2.Maze.CellSize = 5;
		//counters to assign threads to quad
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
		System.out.println("FINISHED!");
	}

}
