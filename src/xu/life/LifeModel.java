package xu.life;

import java.util.Arrays;

public class LifeModel {
	private int[][] matrix;
	private int[][] count;
	private int generation = 0;
	
	public LifeModel() {
		setSize(0,0);
	}
	
	public LifeModel(int sizeX, int sizeY) {
		matrix = new int[sizeX][sizeY];
		count = new int[sizeX][sizeY];
	}	
	
	public void setSize(int width, int height) {
		matrix = new int[width][height];
		count = new int[width][height];	
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public int getWidth() {
		return matrix.length;
	}
	
	public int getHeight() {
		return matrix[0].length;
	}

	public int[][] getMatrix() {
		return matrix;
	}
	
	public void step() {
		buildCounts();
		processNext();		
		generation++;
	}	
	
	private void buildCounts() {
		int nx = matrix.length;
		int ny = matrix[0].length;
				
		for (int x = 0; x < nx; ++x) {
			Arrays.fill(count[x], 0);
		}
		
		for (int x = 1; x < nx - 1; ++x) {
			for (int y = 1; y < ny - 1; ++y) {				
				if (matrix[x][y] == 0) {
					continue;
				}
				count[x - 1][y - 1]++;
				count[x - 1][y + 1]++;
				count[x + 1][y - 1]++;
				count[x + 1][y + 1]++;
				
				count[x - 1][y]++;
				count[x + 1][y]++;
				count[x][y - 1]++;
				count[x][y + 1]++;
			}
		}
		
		for (int x = 1; x < nx - 1; ++x) {
			if (matrix[x][0] != 0) {				
				count[x - 1][0]++;
				count[x + 1][0]++;	
				count[x][1]++;
				count[x - 1][1]++;
				count[x + 1][1]++;							
			}
			if (matrix[x][ny - 1] != 0) {
				count[x - 1][ny - 1]++;
				count[x + 1][ny - 1]++;	
				count[x][ny - 2]++;				
				count[x - 1][ny - 2]++;
				count[x + 1][ny - 2]++;										
			}
		}
		
		for (int y = 1; y < ny - 1; ++y) {
			if (matrix[0][y] != 0) {				
				count[0][y - 1]++;
				count[0][y + 1]++;	
				count[1][y]++;
				count[1][y - 1]++;
				count[1][y + 1]++;							
			}
			if (matrix[nx - 1][y] != 0) {
				count[nx - 1][y - 1]++;
				count[nx - 1][y + 1]++;
				count[nx - 2][y]++;
				count[nx - 2][y - 1]++;
				count[nx - 2][y + 1]++;
			}
		}
	}
	
	private void processNext() {			
		int nx = matrix.length;
		int ny = matrix[0].length;
		for (int x = 0; x < nx; ++x) {
			for (int y = 0; y < ny; ++y) {							
				switch (count[x][y]) {
					case 2:
						if (matrix[x][y] > 0) {
							matrix[x][y]++;
						}
						break;
					case 3:
						matrix[x][y]++;
						break;
					default:
						matrix[x][y] = 0;
						break;
				}
			}
		}	
	}			
}
