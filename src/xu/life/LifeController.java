package xu.life;

import java.util.Arrays;
import xu.life.LifeModel;

public class LifeController {
	LifeModel model;
	
	public LifeController(LifeModel model) {
		setModel(model);
	}
	
	//Model access
	public LifeModel getModel() {
		return model;
	}	
	
	public void setModel(LifeModel model) {		
		this.model = model;	
	}

	private void updateBorder() {
		switch (borderMode) {
			case random:
				fillBorderRandom();
				break;
			case weighted:
				fillBorderWeighted();
				break;
			default:
				break;
		}
	}	
	
	public void step() {
		updateBorder();
		model.step();
	}

	//Data access
	public void getMatrix(int[][] m) {
		int[][] matrix = model.getMatrix();
		int nx = matrix.length;
		int ny = matrix[0].length;
		
		for (int x = 0; x < nx; ++x) {			
			for (int y = 0; y < ny; ++y) {
				m[x][y] = matrix[x][y];
			}
		}		
	}	
	
	public int getWidth() {
		return model.getWidth();
	}
	
	public int getHeight() {
		return model.getHeight();
	}	
	
	public int getGeneration() {
		return model.getGeneration();
	}
	
	//Border generation control algorithms
	private double weightX = 0;
	private double weightY = 0;
	private double borderFactor = 0.8;
	public static enum BorderMode {
		none, random, weighted;		
	}	
	private BorderMode borderMode = BorderMode.random;
	public BorderMode getBorderMode() { 
		return borderMode;
	}
	
	public void setBorderMode(BorderMode mode) {		
		borderMode = mode;		
	}
	
	public void setBorderWeights(double wx, double wy) {	
		weightX = wx;
		weightY = wy;		
	}
	
	public void setBorderFactor(double factor) {
		borderFactor = factor;
	}
	
	public void fillBorderRandom() {
		int[][] m = model.getMatrix();		
		int nx = m.length;		
		int ny = m[0].length;		 
		
		for (int x = 0; x < nx; ++x) {
			m[x][0] = Math.random() < borderFactor ?1:0;
			m[x][ny - 1] = Math.random() < borderFactor?1:0;
		}
		
		for (int y = 0; y < ny; ++y) {
			m[0][y] = Math.random() < borderFactor?1:0;
			m[nx - 1][y] = Math.random() < borderFactor?1:0;			
		}	
	}
		
	public void fillBorderWeighted() {
		int[][] m = model.getMatrix();
		
		int nx = m.length;
		double hx = (double)nx / 2.;
		int ny = m[0].length;
		double hy = (double)ny / 2.;
		
		for (int x = 0; x < nx; ++x) {
			double w = borderFactor * (x - hx) / hx;
			double w0 = w * weightX + weightY;
			double wN = -w * weightX + weightY;
			m[x][0] = -Math.random() > w0?1:0;
			m[x][ny - 1] = Math.random() < wN?1:0;
		}
		
		for (int y = 0; y < ny; ++y) {
			double w = borderFactor * (y - hy) / hy;			
			double w0 = w * weightY + weightX;
			double wN = -w * weightY + weightX;

			m[0][y] = Math.random() < w0?1:0;
			m[nx - 1][y] = -Math.random() > wN?1:0;			
		}
	}	
	
	//Arbitrary data generation algorithms
	public void fillZone(int px, int py, double pr, double wf) {
		int[][] m = model.getMatrix();
		int nx = m.length;
		int ny = m[0].length;		
		int d = (int)pr;		
		
		int x0 = Math.max(0, px - d);
		int xN = Math.min(nx - 1, px + d);
		int y0 = Math.max(0, py - d);
		int yN = Math.min(ny - 1, py + d);
		
		for (int x = x0; x <= xN; ++x) {
			for (int y = y0; y < yN; ++y) {
				if (m[x][y] != 0) {
					continue;
				}
				
				int dx = x - px;
				int dy = y - py;
				double r = Math.sqrt((dx*dx) + (dy*dy));
				double w = wf * (1. - r / pr);							
				m[x][y] = Math.random() < w?1:0;
			}
		}
	}
		
	public void placePattern(int px, int py, int[][] pattern) {
		int[][] m = model.getMatrix();		
		int nx = m.length;
		int ny = m[0].length;				
		int mx = pattern.length;
		int my = pattern[0].length;
		
		for (int x = 0; x < mx; ++x) {
			for (int y = 0; y < my; ++y) {								
				int xx = px + x;
				int yy = py + y;				
				if ((xx < 0) || (yy < 0) || (xx >= nx) || (yy >= ny)) {
					continue;
				}				
				m[xx][yy] = pattern[x][y];
			}
		}			
	}
	
	public void placeCenteredPattern(int px, int py, int[][] pattern) {
		int[][] m = model.getMatrix();		
		int nx = m.length;
		int ny = m[0].length;		
		int mx = pattern.length;
		int my = pattern[0].length;
		
		for (int x = 0; x < mx; ++x) {
			for (int y = 0; y < my; ++y) {
				if (pattern[x][y] == 0) {
					continue;
				}				
				int xx = px - mx/2 + x;
				int yy = py - my/2 + y;				
				if ((xx < 0) || (yy < 0) || (xx >= nx) || (yy >= ny)) {
					continue;
				}
				
				m[xx][yy] = 1;
			}
		}		
	}
	
	public void randomize() {
		int[][] m = model.getMatrix();
		int nx = m.length;
		int ny = m[0].length;
		for (int x = 0; x < nx; ++x) {
			for (int y = 0; y < ny; ++y) {
				m[x][y] = (Math.random() > 0.5)?1:0;
			}
		}
	}	
	
	public void clear() {
		int[][] m = model.getMatrix();
		int nx = m.length;			
		for (int x = 0; x < nx; ++x) {
			Arrays.fill(m[x], 0);
		}
	}
}
