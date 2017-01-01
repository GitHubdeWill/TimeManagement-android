package ml.myll.mengyinnotifier;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by William on 2016/11/26.
 */

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "Setting";
    public static final String PREFS_NAME = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
    }

}
