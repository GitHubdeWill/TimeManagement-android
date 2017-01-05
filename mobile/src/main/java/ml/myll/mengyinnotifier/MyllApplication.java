package ml.myll.mengyinnotifier;

import android.app.Application;
import android.content.Context;

/**
 * Created by will on 1/4/2017.
 */

public class MyllApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyllApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyllApplication.context;
    }
}
