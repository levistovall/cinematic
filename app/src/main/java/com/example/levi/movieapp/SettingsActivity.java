package com.example.levi.movieapp;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public boolean onSupportNavigateUp(){
        super.onSupportNavigateUp();
        Log.v("NAV", "up navigation attempt detected");
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        setContentView(R.layout.activity_settings);


        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sortmode)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_genre)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_cast)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_narrow_by_genre)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_narrow_by_year)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_year_range)));

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.

        // handle different types of preferences differently
        if((preference instanceof MultiSelectListPreference) ){

            // have an empty set to serve as default for multiselect pref
            Set dummySet = new AbstractSet() {
                @Override
                public Iterator iterator() {
                    return null;
                }

                @Override
                public int size() {
                    return 0;
                }
            };

            onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getStringSet(preference.getKey(), dummySet));
            return;
        }

        try{onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
        }catch(ClassCastException e){
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        Log.v("STRING VALUE", stringValue);
        Log.v("PREF TITLE", preference.getTitle().toString());

        if(preference.getTitle().toString().equals(getResources().getString(R.string.pref_label_narrow_by_genre))
                && stringValue.equals("false")){
            Log.v("NARROW", "inside if clause");

            // if the user updates the preference for whether to consider genre to false,
            // the genre preference will be updated to an empty set
            AbstractSet dummySet = new AbstractSet() {
                @Override
                public Iterator iterator() {
                    return new Iterator() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Object next() {
                            return null;
                        }

                        @Override
                        public void remove() {

                        }
                    };
                }

                @Override
                public int size() {
                    return 0;
                }
            };

            //update the genre preference
            PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .edit().putStringSet(getResources().getString(R.string.pref_key_genre), dummySet).commit();

            // update the genre preference summary
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_genre)));
            Log.v("PREF CHANGED", PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getStringSet(preference.getDependency(), dummySet).toString());
        } else if(preference.getTitle().toString().equals(getResources().getString(R.string.pref_label_narrow_by_year))
                && stringValue.equals("false")){
            Log.v("NARROW", "inside if clause");

            // similarly to above, update the year range preference
            PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .edit().putString(getResources().getString(R.string.pref_key_year_range), "").apply();

            // update the year range preference summary
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_year_range)));
        }

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if(preference instanceof MultiSelectListPreference){
            MultiSelectListPreference multiPref = (MultiSelectListPreference) preference;

            // if preference is a multi select list, i assume it is the genre preference, and
            // if the set is empty the preference summary should say All Genres
            if(stringValue.equals("[]")){
                preference.setSummary("All Genres");

            // otherwise it should list the selected genres
            }else{
                String[] valueArray = stringValue.substring(1, stringValue.length()-1).split(", ");
                int[] indexArray = new int[valueArray.length];
                for(int i = 0; i<indexArray.length; i++){
                    Log.v(valueArray[i], Integer.toString(multiPref.findIndexOfValue(valueArray[i])));
                    indexArray[i] = multiPref.findIndexOfValue(valueArray[i]);
                }
                String summaryString = multiPref.getEntries()[indexArray[indexArray.length-1]].toString();
                for(int i = 0; i<indexArray.length-1; i++){
                    summaryString = multiPref.getEntries()[indexArray[i]] + ", " + summaryString;
                }
                preference.setSummary(summaryString);
            }

        // if preference is a switch just summarize with on or off
        } else if(preference instanceof SwitchPreference){
            if((Boolean) value){
                preference.setSummary("On");
            }else{
                preference.setSummary("Off");
            }

        // the custom year range preference stores a string value of form
        // "[start_year]-[end_year]", so use it as summary unless the span of years
        // is one or no range is selected
        } else if(preference instanceof YearRangePreference){
            if((stringValue.split("-").length > 1) && (stringValue.split("-")[0].equals(stringValue.split("-")[1]))) {
                preference.setSummary(stringValue.split("-")[0]);
            }else if(stringValue.equals("")){
                preference.setSummary("All Release Years");
            } else{
                preference.setSummary(stringValue);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}
