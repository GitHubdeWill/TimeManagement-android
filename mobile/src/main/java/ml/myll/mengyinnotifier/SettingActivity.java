package ml.myll.mengyinnotifier;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by William on 2016/11/26.
 */

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "Setting";
    public static final String PREFS_NAME = "Settings";

//
//    public static class DatePickerFragment extends DialogFragment
//            implements DatePickerDialog.OnDateSetListener {
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Use the current date as the default date in the picker
//            final Calendar c = Calendar.getInstance();
//            int year = c.get(Calendar.YEAR);
//            int month = c.get(Calendar.MONTH);
//            int day = c.get(Calendar.DAY_OF_MONTH);
//
//            // Create a new instance of DatePickerDialog and return it
//            return new DatePickerDialog(getActivity(), this, year, month, day);
//        }
//
//        public void onDateSet(DatePicker view, int year, int month, int day) {
//            SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putInt("year", year);
//            editor.putInt("month", month);
//            editor.putInt("day", day);
//            editor.commit();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
    }

}
