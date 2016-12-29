package ml.myll.mengyinnotifier;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by William on 2016/11/26.
 */

public class CommonUtils {

    final static String TAG = "COM_UTIL";
    public static boolean hasPermission = false;
    public static final String[] ITEMS = {"睡觉", "工作", "学习", "娱乐", "生活", "其他"};
    private static final int[] colors = {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2],
            ColorTemplate.VORDIPLOM_COLORS[3],
            ColorTemplate.VORDIPLOM_COLORS[4],
            ColorTemplate.getHoloBlue()};

    public static String local_file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MYLLTIME";

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void createRecordIfNotCreated() {
        Log.e(TAG, "Start creating store file Dir");
        File f = new File(local_file);
        if(!f.exists()||!f.isDirectory()){
            f.mkdirs();
        }

        Log.e(TAG, "Start creating File");
        File f0 = new File(f.getAbsolutePath(), "/record.txt");
        if(f0.exists()) {
            Log.i(TAG, "record.txt Already existed, Skipping");
        } else {
            try {
                Log.e(TAG, f0.getPath());
                if (!f0.createNewFile()) {
                    System.out.println("File already exists");
                    initFile(f0);
                } else {
                    initFile(f0);
                    System.out.println("File created");
                }
            } catch (IOException ex) {
                Log.e(TAG, "create Error");
                ex.printStackTrace();
            }
        }
        hasPermission = true;
    }

    public static void recreateRecord () {
        Log.e(TAG, "Start checking store file Dir");
        File f = new File(local_file);

        Log.e(TAG, "Start creating File");
        File f0 = new File(f.getAbsolutePath(), "/record.txt");
        if(f0.exists()) {
            Log.i(TAG, "record.txt existed, Deleting");
            f0.delete();
        }
        createRecordIfNotCreated();
    }

    private static void initFile (File f0) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f0));
            if (br.readLine() == null)
                System.out.println("No errors, and file empty");
            else return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter fw = new FileWriter(f0, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.print(eventRecord(5));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String eventRecord (int event) {
        return event + " " + Calendar.getInstance().get(Calendar.YEAR)+ " "
                + Calendar.getInstance().get(Calendar.MONTH)+ " "
                + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+ " "
                + Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+ " "
                + Calendar.getInstance().get(Calendar.MINUTE) + " #"
                + System.currentTimeMillis();
    }

    public static long[] getTotalTime (){
        long[] record = {0,0,0,0,0,0};
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), "/record.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(f0))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (Integer.parseInt(line.charAt(0)+"") < 6 && line.split("#").length==2)
                    record[Integer.parseInt(line.charAt(0)+"")] +=
                            System.currentTimeMillis() -
                                    Long.parseLong(line.split("#")[1]);
                if (Integer.parseInt(line.charAt(0)+"") < 6 && line.split("#").length==3)
                    record[Integer.parseInt(line.charAt(0)+"")] +=
                            Long.parseLong(line.split("#")[2]) -
                                    Long.parseLong(line.split("#")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public static void newEvent (int event) {
        Log.e(TAG, "Start checking file Dir");
        File f = new File(local_file);

        Log.e(TAG, "Start writing File");
        File f0 = new File(f.getAbsolutePath(), "/record.txt");
        if(f0.exists()) {
            Log.i(TAG, "record.txt existed, starting...");
            try(FileWriter fw = new FileWriter(f0, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("#"+System.currentTimeMillis());
                out.print(CommonUtils.eventRecord(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "File not existed");
        }
    }

    public static ArrayList<WeekViewEvent> getEvents () {
        ArrayList<WeekViewEvent> ret = new ArrayList<>();
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), "/record.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(f0))) {
            String line;
            while ((line = br.readLine()) != null) {
                Log.i(TAG, "Reading "+line);
                if (line.split(" ").length==7 && line.split("#").length==3) {
                    String[] lineComp = line.split(" ");
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(Long.parseLong(line.split("#")[1]));
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(Long.parseLong(line.split("#")[2]));
                    WeekViewEvent weekViewEvent =
                            new WeekViewEvent(Integer.parseInt(lineComp[0]),
                                    ITEMS[Integer.parseInt(lineComp[0])], start, end);
                    weekViewEvent.setColor(colors[Integer.parseInt(lineComp[0])]);
                    ret.add(weekViewEvent);
                } else if (line.split(" ").length==7 && line.split("#").length==2) {
                    String[] lineComp = line.split(" ");
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(Long.parseLong(line.split("#")[1]));
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(System.currentTimeMillis());
                    WeekViewEvent weekViewEvent =
                            new WeekViewEvent(Integer.parseInt(lineComp[0]),
                                    ITEMS[Integer.parseInt(lineComp[0])], start, end);
                    weekViewEvent.setColor(colors[Integer.parseInt(lineComp[0])]);
                    ret.add(weekViewEvent);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return ret;
    }
}
