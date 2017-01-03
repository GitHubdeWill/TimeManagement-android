package ml.myll.mengyinnotifier;

import android.os.Environment;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by William on 2016/11/26.
 */

public class CommonUtils {

    private final static String TAG = "COM_UTIL";
    public static boolean hasPermission = false;

    //Store events, Ids
    public static List<MEvent> items = new ArrayList<>();
    public static List<Integer> drawerItemsIds = new ArrayList<>();
    //Notification shortcuts
    public static Integer[] shortcuts= {0,1,3};

    //curEvent
    public static int currEvent = 5;

    //Files
    public static String local_file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MYLLTIME";
    public static String eventRecordFile = "/record.txt";

    //Set default color and events and drawerItems
    public static void initItems(){
        //Clear All Items
        items = new ArrayList<>();
        drawerItemsIds = new ArrayList<>();
        //Basic
        String[] evs= {"睡觉", "工作", "学习", "娱乐", "生活", "其他"};
        int[] colors = {
                ColorTemplate.VORDIPLOM_COLORS[0],
                ColorTemplate.VORDIPLOM_COLORS[1],
                ColorTemplate.VORDIPLOM_COLORS[2],
                ColorTemplate.VORDIPLOM_COLORS[3],
                ColorTemplate.VORDIPLOM_COLORS[4],
                ColorTemplate.getHoloBlue()};
        for(int i = 0; i < evs.length; i++) {
            items.add(new MEvent(evs[i], colors[i]));
        }
        drawerItemsIds.add(R.id.sleep);
        drawerItemsIds.add(R.id.work);
        drawerItemsIds.add(R.id.study);
        drawerItemsIds.add(R.id.recreation);
        drawerItemsIds.add(R.id.sustain);
        drawerItemsIds.add(R.id.other);

        //Customized

        //Set currEvent
        currEvent = getCurrEventFromExternal();
        Log.i(TAG, "Current event: "+currEvent);
    }

    public static int getCurrEventFromExternal() {
        try {
            String sCurrentLine, last = "";

            BufferedReader br = new BufferedReader(new FileReader(new File(new File(local_file).getAbsolutePath(), eventRecordFile)));

            while ((sCurrentLine = br.readLine()) != null) {
                last = sCurrentLine;
            }
            br.close();
            return Integer.parseInt(last.split(" ")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //return array of colors
    public static int[] getColorsFromItems(){
        int[] c = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            c[i] = items.get(i).getColor();
        }
        return c;
    }

    //return array of event names
    public static String[] getNamesFromItems(){
        String[] c = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            c[i] = items.get(i).getName();
        }
        return c;
    }

    //Check, create records.txt and <initFile>
    public static void createRecordIfNotCreated() {
        Log.e(TAG, "Start creating store file Dir");
        File f = new File(local_file);
        if(!f.exists()||!f.isDirectory()){
            f.mkdirs();
        }

        Log.e(TAG, "Start creating File");
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        if(f0.exists()) {
            Log.i(TAG, "record.txt Already existed");
            try {
                BufferedReader br = new BufferedReader(new FileReader(f0));
                if (br.readLine() == null) {
                    initFile(f0);
                    Log.i(TAG, "record.txt Empty");
                }
                else return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Log.e(TAG, f0.getPath());
                if (!f0.createNewFile()) {
                    System.out.println("File already exists");
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

    /**
     *
     * @param f0 external file
     *      check if empty
     *      write event 5 to file
     */
    private static void initFile (File f0) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f0));
            if (br.readLine() == null) {
                System.out.println("No errors, and file empty");
            }
            else return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter fw = new FileWriter(f0, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.print(getEventString(5));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //return storing standard string of the event. w/o finish last event
    public static String getEventString (int event) {
        return event + " " + Calendar.getInstance().get(Calendar.YEAR)+ " "
                + Calendar.getInstance().get(Calendar.MONTH)+ " "
                + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+ " "
                + Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+ " "
                + Calendar.getInstance().get(Calendar.MINUTE) + " #"
                + System.currentTimeMillis();
    }

    //return array of total time in items
    public static long[] getTotalTime (){
        long[] record = {0,0,0,0,0,0};
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
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

    //Create a new event, write to external file and update <currEvent>
    public static void newEvent (int event) {
        Log.e(TAG, "Start checking file Dir");
        File f = new File(local_file);

        Log.e(TAG, "Start writing File");
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        if(f0.exists()) {
            Log.i(TAG, "record.txt existed, starting...");
            try(FileWriter fw = new FileWriter(f0, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("#"+System.currentTimeMillis());
                out.print(getEventString(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "File not existed");
        }
        currEvent = event;
    }

    //get all events in the year and month specified
    public static ArrayList<WeekViewEvent> getEvents (int year, int month) {
        ArrayList<WeekViewEvent> ret = new ArrayList<>();
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        try (BufferedReader br = new BufferedReader(new FileReader(f0))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.split(" ").length==7 && line.split("#").length>=2) {
//                    Log.d(TAG, "cur YYYY MM: " + Integer.parseInt(line.split(" ")[1]) + Integer.parseInt(line.split(" ")[2]));
                    if (Integer.parseInt(line.split(" ")[1]) != year || Integer.parseInt(line.split(" ")[2]) != month-1) continue;
                }
                if (line.split(" ").length==7 && line.split("#").length==3) {
                    String[] lineComp = line.split(" ");
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(Long.parseLong(line.split("#")[1]));
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(Long.parseLong(line.split("#")[2]));
                    WeekViewEvent weekViewEvent =
                            new WeekViewEvent(Integer.parseInt(lineComp[0]),
                                    items.get(Integer.parseInt(lineComp[0])).getName(), start, end);
                    weekViewEvent.setColor(getColorsFromItems()[Integer.parseInt(lineComp[0])]);
                    ret.add(weekViewEvent);
                } else if (line.split(" ").length==7 && line.split("#").length==2) {
                    String[] lineComp = line.split(" ");
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(Long.parseLong(line.split("#")[1]));
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(System.currentTimeMillis());
                    WeekViewEvent weekViewEvent =
                            new WeekViewEvent(Integer.parseInt(lineComp[0]),
                                    items.get(Integer.parseInt(lineComp[0])).getName(), start, end);
                    weekViewEvent.setColor(getColorsFromItems()[Integer.parseInt(lineComp[0])]);
                    ret.add(weekViewEvent);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return ret;
    }


}
