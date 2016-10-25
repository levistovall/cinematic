package com.example.levi.movieapp;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Calendar;

/**
 * Created by Levi on 10/19/2016.
 */

// custom preference which extends dialog preference to provide user with the option
// to select a range of release years by which to restrict the movie search
public class YearRangePreference extends DialogPreference {
    // define attributes including two number pickers for the start year and end year
    private NumberPicker mFirstYearPicker;
    private NumberPicker mLastYearPicker;

    // and a string which will be of form "[start_year]-[end_year]"
    private String mYearRange;

    private static final String ATTR_DEFAULT_VALUE = "defaultValue";

    public YearRangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // upon instantiating YRP, give values to the previously defined number pickers
        mFirstYearPicker = new NumberPicker(context, attrs);
        mLastYearPicker = new NumberPicker(context, attrs);

        // set mYearRange to stored value, or empty string if no value is accessed
        mYearRange = this.getPersistedString("");

        // set the layout resource for the dialog and provide for ok and cancel buttons
        setDialogLayoutResource(R.layout.preference_year_range);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
        mFirstYearPicker.setEnabled(true);
        mLastYearPicker.setEnabled(true);
    }

    @Override
    protected View onCreateDialogView(){
        Calendar c = Calendar.getInstance();

        // Inflate layout
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference_year_range, null);

        // store the first number picker from the layout resource and set its min
        // value to the earliest release year in the api, and its max value to the current year.
        // also set a listener so that the second year picker can never have a value less than the first
        mFirstYearPicker = (NumberPicker) view.findViewById(R.id.start_year);
        mFirstYearPicker.setMinValue(1902);
        mFirstYearPicker.setMaxValue(c.get(Calendar.YEAR));
        mFirstYearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mLastYearPicker.setMinValue(newVal);
            }
        });

        // store the second number picker from the layout resource and set its min
        // value to the earliest release year in the api, and its max value to the current year.
        // also set a listener so that the first year picker can never have a value greater than the second
        mLastYearPicker = (NumberPicker) view.findViewById(R.id.end_year);
        mLastYearPicker.setMinValue(1902);
        mLastYearPicker.setMaxValue(c.get(Calendar.YEAR));
        mLastYearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mFirstYearPicker.setMaxValue(newVal);
            }
        });

        // if no preference is set, set the number picker values to the minumum and maximum
        // years respectively
        if(mYearRange.equals("")){
            mFirstYearPicker.setValue(1902);
            mLastYearPicker.setValue(c.get(Calendar.YEAR));

        // otherwise, set them to the previously stored preference
        }else{
            mFirstYearPicker.setValue(Integer.parseInt(mYearRange.split("-")[0]));
            mLastYearPicker.setValue(Integer.parseInt(mYearRange.split("-")[1]));
        }

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            String value = Integer.toString(mFirstYearPicker.getValue()) + "-" +
                    Integer.toString(mLastYearPicker.getValue());
            if(callChangeListener(value)){
                mYearRange = value;
                persistString(mYearRange);
            }
        }
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mYearRange = this.getPersistedString(ATTR_DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mYearRange = (String) defaultValue;
            persistString(mYearRange);
        }
    }

    private String getRange(){
        return mYearRange;
    }
}
