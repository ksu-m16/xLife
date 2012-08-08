package xu.life;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

enum ColorMap {
	HSV(new HSVColorMap(1024)), 
	GRAYSCALE(new GrayColorMap(1024)), 
	FIRE(new FireColorMap(1024));
	
	ColorMap(IColorMap cm) {
		map = cm;
	}
	
	IColorMap map;
	IColorMap getColorMap() {
		return map;
	}
}

interface IColorMap {
	Paint get(double level);	
}

class HSVColorMap implements IColorMap {		
	private Paint[] colors;	
	public HSVColorMap(int levels) {
		colors = new Paint[levels];
		for (int i = 0; i < levels; ++i) {
			float hue = 360.f * i / levels;
			float[] hsv = {hue, 1.f, 1.f};
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.HSVToColor(hsv));			
			colors[i] = p;
		}
	}	
	
	public Paint get(double level) {
		return colors[(int)(level * colors.length)];
	}
}

class GrayColorMap implements IColorMap {
	private Paint[] colors;	
	public GrayColorMap(int levels) {
		colors = new Paint[levels];		
		for (int i = 0; i < levels; ++i) {
			int level = (int)(255. * i / levels);			
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.rgb(level, level, level));			
			colors[levels - i - 1] = p;
		}
	}	
	
	public Paint get(double level) {
		return colors[(int)(level * colors.length)];
	}
}

class FireColorMap implements IColorMap {
	private Paint[] colors;	
	public FireColorMap(int levels) {
		colors = new Paint[levels];	
		int step = levels / 3 + 1;
		for (int i = 0; i < step; ++i) {
			int c = (int)(255. * i / step);			
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.rgb(c, 0, 0));						
			colors[levels - i - 1] = p;
		}
		
		for (int i = step; i < 2*step; ++i) {
			int c = (int)(255. * (i - step) / step);			
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.rgb(255, c, 0));						
			colors[levels - i - 1] = p;
		}
				
		for (int i = 2*step; i < levels; ++i) {
			int c = (int)(255. * (i - step) / step);			
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.rgb(255, 255, c));						
			colors[levels - i - 1] = p;
		}				
	}	
	
	public Paint get(double level) {
		return colors[(int)(level * colors.length)];
	}
}

public class LifeView extends SurfaceView implements SurfaceHolder.Callback {		

	LifeController controller;	
	SurfaceHolder surfaceHolder;	
	IColorMap colorMap = new HSVColorMap(1024);
	int[][] matrix;

	public LifeView(Context c) {
		super(c);
		getHolder().addCallback(this);		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	public void setController(LifeController controller) {
		this.controller = controller;
		matrix = new int[controller.getWidth()][controller.getHeight()];
	}
	
	public void setColorMap(IColorMap cm) {
		this.colorMap = cm;
	}
	
	Paint paint = new Paint();
	{
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLACK);		
	}
	
	Rect rect = new Rect();
	
	int lastDisplayedGeneration = -1;
	public void draw(boolean force) {
		int gen = controller.getGeneration();
		if (!force) {			
			if (gen == lastDisplayedGeneration) {
				return;
			}
		}
		lastDisplayedGeneration = gen;
		
		Canvas c = surfaceHolder.lockCanvas();
		try {
			synchronized (surfaceHolder) {
				onDraw(c);
			}
		} finally {
			surfaceHolder.unlockCanvasAndPost(c);
		}
	}
		
	protected void onDraw(Canvas c) {		
		if (controller == null) {
			return;
		}
		
		int sx = c.getWidth() / controller.getWidth();
		int sy = c.getHeight() / controller.getHeight();		
		controller.getMatrix(matrix);
		
		for (int x = 0; x < matrix.length; ++x) {
			for (int y = 0; y < matrix[x].length; ++y) {
				Paint p = paint;
				if (matrix[x][y] != 0) {
					double age = Math.min(matrix[x][y] / 100., 0.8);
					p = colorMap.get(age);					
				}			
				rect.set(x*sx, y*sy, (x + 1)*sx - 1, (y + 1)*sy - 1);
				c.drawRect(rect, p);
			}
		}		
	}	
}

