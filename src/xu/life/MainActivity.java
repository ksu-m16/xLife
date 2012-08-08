package xu.life;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import xu.life.LifeController.BorderMode;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	int cellSize = 12;	
	int updateDelay = 100;
	BorderMode borderMode = BorderMode.weighted;
	ColorMap colorMap = ColorMap.HSV;
	
	LifeController controller;	
	LifeView view;
	Timer timer;
	
	boolean swapAccXY = false;

	boolean initController() {
		int nx = (view.getWidth() / cellSize);
		int ny = (view.getHeight() / cellSize);
		if ((nx == 0) || (ny == 0)) { 
			return false;
		}
		
		controller = new LifeController(
			new LifeModel(nx, ny)
		);
		controller.randomize();
		controller.setBorderMode(LifeController.BorderMode.weighted);			
		view.setController(controller);
		return true;
	}
	
	void updateController() {
		AccSample acc = accListener.get();
		if (!swapAccXY) {
			controller.setBorderWeights(acc.x / 10., acc.y / 10.);
		} else {
			controller.setBorderWeights(acc.y / 10., -acc.x / 10.);
		}		
		if (!view.isShown()) {
			return;
		}
		if (!paused) {
			controller.step();
		}
		view.draw(paused);
	}
	
	void onInitComplete() {
		scheduleUpdate();		
	}
	
	class InitTask extends TimerTask {
		@Override
		public void run() {
			if (!initController()) {
				return;
			}
			onInitComplete();
		}		
	};
	TimerTask initTask = null; 
	
	class UpdateTask extends TimerTask {					
		@Override		
		public void run() {
			updateController();
		}		
	};
	TimerTask updateTask = null;
	
	void clearTimers() {
		if (initTask != null) {
			initTask.cancel();
			initTask = null;
		}
		
		if (updateTask != null) {
			updateTask.cancel();
			updateTask = null;
		}
		
		timer.purge();
	}
	
	void scheduleInit() {
		clearTimers();
		initTask = new InitTask(); 
		timer.schedule(initTask, 0, 100);
	}
	
	void scheduleUpdate() {
		clearTimers();
		updateTask = new UpdateTask();
		timer.schedule(updateTask, 0, updateDelay);
		
		controller.setBorderMode(borderMode);
		view.setColorMap(colorMap.getColorMap());
	}
	
	static class AccSample {
		double x;
		double y;
		double z;
	}
	
	class AccListener implements SensorEventListener {		
		AccSample acc = new AccSample();		
		public AccSample get() {
			return acc;
		}
		
		public void onAccuracyChanged(Sensor s, int a) {		
		}

		public void onSensorChanged(SensorEvent e) {			
			float[] val = e.values;
			acc.x = acc.x * 0.9 + val[0] * 0.1;
			acc.y = acc.y * 0.9 + val[1] * 0.1;
			acc.z = acc.z * 0.9 + val[2] * 0.1;
		}		
	}	
	AccListener accListener = new AccListener();	
	void registerAccListener() {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		Sensor acc = sensorList.get(0);        
        sensorManager.registerListener(accListener, 
        	acc, SensorManager.SENSOR_DELAY_FASTEST);	
	}	
	void unregisterAccListener() {
		SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(accListener);
	}
	
	private void determineScreenOrientation() {
        Display d = this.getWindowManager().getDefaultDisplay();
        int rotation = d.getOrientation();
		try {
			//rotation = (Integer) d.getClass().getMethod("getRotation").invoke(d);
		} catch (Exception e) {			
		}
        switch(rotation) {
        	case Surface.ROTATION_90:
        	case Surface.ROTATION_270:
        		swapAccXY = true;
        		break;
        	default:
        		swapAccXY = false;
        }	
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT <= 10) {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
        	requestWindowFeature(0x00000008);//Action bar
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        determineScreenOrientation();
        
        view = new LifeView(this);        
        this.setContentView(view);               
        
    	view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {				
				for (int idx = 0; idx < event.getPointerCount(); ++idx) {					
					float ox = event.getHistoricalX(idx, 1);
					float oy = event.getHistoricalY(idx, 1);
					float cx = event.getX(idx);
					float cy = event.getY(idx);
					
					float dx = cx - ox;
					float dy = cy - oy;
					float dd = (dx*dx + dy*dy);
					if (dd < cellSize*5) {
						continue;
					}
					
					int x = (int)(event.getX(idx) / cellSize);
					int y = (int)(event.getY(idx) / cellSize);					
					double r = Math.max(20. / cellSize, 3);
					controller.fillZone(x, y, r, 0.3);
//					int[][] pattern = {{1,1,1,0,1},{1,0,0,0,0},{0,0,0,1,1},{0,1,1,0,1},{1,0,1,0,1}};
//					controller.placePattern(x, y, pattern);
				}
				return true;
			}     		
    	});
        
        
        timer = new Timer();
        scheduleInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void updateSettings() {
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	int cellSize = Integer.valueOf(pref.getString("cellSize", "7"));
    	int updateDelay = Integer.valueOf(pref.getString("updateDelay", "100"));
    	
    	boolean needRestart = false;
    	
    	if (this.cellSize != cellSize) {
    		this.cellSize = cellSize;
    		needRestart = true;
    	}
    	if (this.updateDelay != updateDelay) {
    		this.updateDelay = updateDelay;    		
    	}
    	
    	BorderMode borderMode = this.borderMode;
    	try {
    		borderMode = BorderMode.valueOf(pref.getString("borderFill", "none"));
    	} catch(Exception ex) {
    		pref.edit().putString("borderFill", borderMode.toString());
    	}
    	if (this.borderMode != borderMode) {
    		this.borderMode = borderMode;
    	}    	
    	
    	ColorMap colorMap = this.colorMap;
    	try {
    		colorMap = ColorMap.valueOf(pref.getString("colorMap", "hsv"));
    	} catch(Exception ex) {
    		pref.edit().putString("colorMap", colorMap.toString());
    	}
    	    	
    	if (this.colorMap != colorMap) {
    		this.colorMap = colorMap;
    	}
    	
    	if (needRestart) {
    		controller = null;
    	}    	
    }
    
    private boolean paused = false;
    private void pauseResume() {    	
    	paused = !paused;
    }
    
    private static final int CAMERA_REQUEST = 1000;
    private void takePhoto() {
    	if (controller == null) {
    		return;
    	}
    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
        startActivityForResult(cameraIntent, CAMERA_REQUEST);     	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == CAMERA_REQUEST) {
    		Bitmap photo = (Bitmap)data.getExtras().get("data");    
    		int nx = controller.getWidth();
    		int ny = controller.getHeight();
    		Bitmap img = Bitmap.createScaledBitmap(photo, nx, ny, false);
    		int[][] pattern = new int[nx][ny];
    		double intensityThreshold = 0.3;
    		double positiveProbability = 0.4;
    		
    		for (int x = 0; x < nx; ++x) {
    			for (int y = 0; y < ny; ++y) {
    				int pixel = img.getPixel(x, y);
    				int red = (pixel >> 16) & 0xFF;
    				int green = (pixel >> 8) & 0xFF;
    				int blue = pixel & 0xFF;
    				double intensity = (red + green + blue) / 768.;
    				if (intensity > intensityThreshold) {
    					intensity = (intensity - intensityThreshold) 
    						/ (1. - intensityThreshold);
    					if (Math.random() < positiveProbability) {
    						pattern[x][y] = (int)((1. - intensity)* 100. + 1);
    					}
    				}    				
    			}
    		}
    		if (!paused) {
    			pauseResume();
    		}
    		controller.clear();
    		controller.placePattern(0, 0, pattern);
    	}    
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem item = menu.findItem(R.id.menu_pause_resume);
		item.setTitle(paused?R.string.menu_resume:R.string.menu_pause);         	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_settings:
    	    	Intent si = new Intent(this, SettingsActivity.class);
    	    	this.startActivity(si);    	    	
    			break;
    		case R.id.menu_clear:
    			controller.clear();
    			break;
    		case R.id.menu_restart:
    			scheduleInit();
    			break;
    		case R.id.menu_pause_resume:
    			pauseResume();    			
        		break;
    		case R.id.menu_photo:    			
    			takePhoto();
    			break;
    		case R.id.menu_exit:
    			this.finish();
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    			
    
    @Override
    protected void onResume() {
    	updateSettings();
    	if (controller == null) {
    		scheduleInit();    		
    	} else {    		
    		scheduleUpdate();
    	}
    	registerAccListener();
    	super.onResume();
    }
    
    
	@Override
	protected void onPause() {
		unregisterAccListener();
		clearTimers();
		super.onPause();
	}	
	
}
