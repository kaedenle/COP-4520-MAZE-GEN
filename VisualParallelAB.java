
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Nodes implements Runnable{
	public static MazeObjs Maze = null;
	int posrows, poscols;
	public static AtomicInteger count = new AtomicInteger(0);
	public static final Object lock = new Object();
	
	public Nodes(int r, int c, boolean StepMode, int N) {
		if(Maze == null)
			Maze = new MazeObjs(r, c, StepMode, N);
		this.poscols = Maze.startingcols;
		this.posrows = Maze.startingrow;
	}
	
	@Override
	public void run() {
		//while there's still tiles remaining
		while(Maze.remaining.get() > 0) {
			int direction = new Random().nextInt(4);
			int[] delta = Maze.deltas[direction].clone();
			
			//keep thread within boundaries
			if(delta[1] == 0 && (delta[0] + posrows >= Maze.rows || delta[0] + posrows < 0)) {
				delta[0] *= -1;
				direction = (direction + 2) % 4;
			}
				
			if(delta[0] == 0 && (delta[1] + poscols >= Maze.cols || delta[1] + poscols < 0)) {
				delta[1] *= -1;
				direction = (direction + 2) % 4;
			}
			
			//if square trying to access is occupied, find a new direction (also try to occupy it)
			if(Maze.gridflag[posrows + delta[0]][poscols + delta[1]].getAndSet(true))
				continue;
			posrows += delta[0];
			poscols += delta[1];
			//if next cell hadn't been visited, set it and the current cell to opposite directions
			//(OPENING A PATH BETWEEN THEM)
			if(Maze.grid[posrows][poscols] == 0) {
				Maze.grid[posrows][poscols] |= Maze.bitmasks[(direction + 2) % 4];
				Maze.grid[posrows - delta[0]][poscols - delta[1]] |= Maze.bitmasks[direction];
				Maze.remaining.decrementAndGet();
			}
			//leaving maze set false
			Maze.gridflag[posrows][poscols].set(false);
			
			//for visualization purposes
			if(Maze.StepMode)
			{
				synchronized(lock)
				{ 
				  count.incrementAndGet();
				  if(count.get() < Maze.N )
				  { 
				    try {
				    	//System.out.println(Thread.currentThread().getId() + " Waiting " + count.get() + " " + Maze.N);
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  }
				  if(count.get() != 0) {
					  //System.out.println(Thread.currentThread().getId() + " Unlocking");
					  Maze.frame.validate();
					  Maze.frame.repaint();
					  try {
						Thread.sleep(50);
					  } 
					  catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					  }
					  count.set(0);
					  lock.notifyAll();
				  }
				  //take the action here;
				}
			}
		}
	}
}

class MazeObjs extends JPanel{
	public int[][] grid;
	public AtomicBoolean[][] gridflag;
	public int startingrow;
	public int startingcols;
	public int rows;
	public int cols;
	public AtomicInteger remaining;
	public int[] bitmasks = {1, 2, 4, 8};
	public int[][] deltas = {{-1, 0},{0, 1},{1, 0},{0, -1}};
	public boolean StepMode;
	public Nodes[] nList; 
	public int N;
	public JFrame frame;
	
	public MazeObjs(int rows, int cols, boolean StepMode, int N) {
		//the actual maze
		grid = new int[rows][cols];
		//detects if a thread is inside a given cell
		gridflag = new AtomicBoolean[rows][cols];
		this.rows = rows;
		this.cols = cols;
		this.StepMode = StepMode;
		nList = new Nodes[N];
		this.N = N;
		ResetMaze();
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
		setPreferredSize(new Dimension(cols * 20, rows * 20));
	}
	public void ResetMaze() {
		startingrow = new Random().nextInt(rows);
		startingcols = new Random().nextInt(cols);
		//fill grid with 0s
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++)
				grid[i][j] = 0;
		}
		//reset remaining
		this.remaining = new AtomicInteger(rows * cols - 1);
		//fill grid flag with falses
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++)
				gridflag[i][j] = new AtomicBoolean(false);
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
	 protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
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
	        if(StepMode) {
	        	g.setColor(new Color(0, 255, 0));
	        	for(Nodes n : nList) {		
			        g.fillRect(n.poscols * 20, n.posrows * 20, 20, 20);
	        	}
	        	g.setColor(new Color(0, 0, 0));
	        }
	    }
}
public class VisualParallelAB {

	public static void main(String[] args) {
		int RunAmount = 1;
		boolean StepMode = true;
		int N = 10;
		
		int ThreadCount = 5;
		Path fileName = Path.of(Paths.get("").toAbsolutePath().toString() + "/timeOutputParallel.txt");
		String output = "";
		if(StepMode)
			RunAmount = 0;
		Nodes.Maze = new MazeObjs(N, N, StepMode, ThreadCount);
		for(int j = 0; j <= RunAmount; j++) {
			long startTime = System.nanoTime();
			
			Thread[] tList = new Thread[ThreadCount];
			for(int i = 0; i < ThreadCount; i++) {
				Nodes.Maze.nList[i] = new Nodes(N, N, StepMode, ThreadCount);
				tList[i] = new Thread(Nodes.Maze.nList[i]);
			}
			
			Nodes.Maze.N = ThreadCount;
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
	        String timeOutput = (totalTime/1000000 + " ms");
	        if(j != RunAmount) Nodes.Maze.ResetMaze();
			if(j == 0) continue;
			output += timeOutput + "\n";
			//if(j != RunAmount) Node.Maze = null;
		}
		try {
            Files.writeString(fileName, (output));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		Nodes.Maze.printMaze();
		System.out.println(output);
		/*JFrame frame = new JFrame("Maze Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(Node.Maze);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);*/
	}
		

}
