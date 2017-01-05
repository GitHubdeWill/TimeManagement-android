package ml.myll.mengyinnotifier;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MovementCheckService extends Service implements SensorEventListener {
    final static String TAG = "MovNotification";
    final static int OTHER_LIMIT = 15*60*1000;

    final static int SLEEP_LIMIT = 60*60*1000;

    static boolean passed = false;
    static long startingTime;

    private SensorManager sensorManager;

    float xCoor; // declare X axis object
    float yCoor; // declare Y axis object
    float zCoor; // declare Z axis object

    int count;

    public MovementCheckService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        Thread.setDefaultUncaughtExceptionHandler(new MExceptionHandler(
                CommonUtils.local_file));
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        boolean success = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(TAG, sensorManager.toString()+" "+success);
        startingTime = System.currentTimeMillis();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Switched to Background", Toast.LENGTH_SHORT).show();
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        boolean success = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(TAG, sensorManager.toString()+" "+success);
        startingTime = System.currentTimeMillis();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        // check sensor type
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

            // assign directions
            float x=event.values[0];
            float y=event.values[1];
            float z=event.values[2];

            boolean moving = (
                xCoor - x > .3 ||
                yCoor - y > .3 ||
                zCoor - z > .3);

            xCoor = x;
            yCoor = y;
            zCoor = z;

            if (moving) {
                startingTime = System.currentTimeMillis();
                count = 0;
                passed = false;
            } else count++;

            long timePassed = System.currentTimeMillis() - startingTime;

            if (!passed && timePassed > OTHER_LIMIT){
                passed = true;
                if (CommonUtils.getCurrEventFromExternal() != 0) {
                    CommonUtils.newEvent(5);
                    sendNotification(5);
                    Toast.makeText(this, "No motion detected, switched to other.", Toast.LENGTH_SHORT).show();
                }
            }
            if (timePassed > SLEEP_LIMIT){
                passed = false;
                CommonUtils.newEvent(0);
                sendNotification(0);
                onDestroy();
            }

            Log.i(TAG, "Time in Seconds:"+timePassed/1000+" Current x:"+x+" y:"+y+" z:"+z);

            if (isForeground()) onDestroy();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        sensorManager.unregisterListener(this);
    }

    public boolean isForeground() {
        return MainActivity.isRunning;
    }

    private void sendNotification (int event){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.scaledicon)
                        .setContentTitle("Event changed due to inactivity")
                        .setContentText("Event changed to "+CommonUtils.getNamesFromItems()[event])
                        .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, AboutUsActivity.class), 0));
        int mNotificationId = 8699000+event;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
