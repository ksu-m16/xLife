package xu.life;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Menu;

public class SettingsActivity extends PreferenceActivity {
		

	OnPreferenceChangeListener preferenceListener = new OnPreferenceChangeListener() {        			
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference)preference;
			updateListPreference(lp, (String)newValue);
			return true;
		}
	};
	
	private void updateListPreference(ListPreference lp, String value) {
		int idx = lp.findIndexOfValue(value);
		if (idx < 0) {
			idx = 0;
		}
		lp.setSummary(lp.getEntries()[idx]);	
	}
	
	private void prepareListPreference(String key) {
		ListPreference lp = (ListPreference)findPreference(key);
		updateListPreference(lp, (String)lp.getValue());
		lp.setOnPreferenceChangeListener(preferenceListener);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);        
        prepareListPreference("cellSize");
        prepareListPreference("updateDelay");
        prepareListPreference("borderFill");
        prepareListPreference("colorMap");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }

    
}
