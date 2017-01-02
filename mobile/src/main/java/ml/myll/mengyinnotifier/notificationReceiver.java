package ml.myll.mengyinnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "rec_ver";

    private final static String FILENAME = "file";

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        int i = (int) intent.getExtras().get("event");
        if (CommonUtils.currEvent == i) return;
        CommonUtils.newEvent(i);
        CommonUtils.currEvent = i;
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fos == null) return;
        try {
            fos.write((i + "").getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Event changed to " + i);
        Toast.makeText(context, "Event changed to "+CommonUtils.getNamesFromItems()[i], Toast.LENGTH_SHORT).show();
    }
}
