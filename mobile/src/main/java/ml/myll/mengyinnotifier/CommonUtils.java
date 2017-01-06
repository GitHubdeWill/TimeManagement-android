package ml.myll.mengyinnotifier;

import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;

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
 * Created by William on 2016/11/026.
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

    //Notification
    public static boolean stickyNotification = true;
    public static boolean colorDynamic = true;
    public static int days = 7;

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
                R.color.md_deep_purple_300, R.color.md_red_300, R.color.md_orange_500,
                R.color.md_yellow_600, R.color.md_light_green_300, R.color.md_light_blue_200
        };
        for(int i = 0; i < evs.length; i++) {
            items.add(new MEvent(evs[i], MyllApplication.getAppContext().getResources().getColor(colors[i])));
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
            String sCurrentLine, last = null;

            BufferedReader br = new BufferedReader(new FileReader(new File(new File(local_file).getAbsolutePath(), eventRecordFile)));

            while ((sCurrentLine = br.readLine()) != null) {
                last = sCurrentLine;
            }
            br.close();
            if (last == null) {
                createRecordIfNotCreated();
                return 5;
            }
            return Integer.parseInt(last.split(" ")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currEvent;
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
        try
        {
            PrintWriter ou = new PrintWriter(new BufferedWriter(new FileWriter(f0, true)));
            ou.print(getEventString(5));
            ou.close();
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
        try  {             BufferedReader br = new BufferedReader(new FileReader(f0));
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
        if (currEvent == event) return;
        Log.e(TAG, "Creating new event "+event);
        File f = new File(local_file);

        Log.e(TAG, "Start writing File");
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        if(f0.exists()) {
            Log.i(TAG, "record.txt existed, starting...");
            try
            {
                PrintWriter ou = new PrintWriter(new BufferedWriter(new FileWriter(f0, true)));
                ou.println("#"+System.currentTimeMillis());
                ou.print(getEventString(event));
                ou.close();
                Log.i(TAG, "written " + getEventString(event));
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
        try  {             BufferedReader br = new BufferedReader(new FileReader(f0));
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

    public static long[] getTimeDays (int days) {
        long[] record = {0,0,0,0,0,0};
        long daysInMillis = days*24*3600*1000;
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        try  {             BufferedReader br = new BufferedReader(new FileReader(f0));
            String line;
            while ((line = br.readLine()) != null) {
                if (Integer.parseInt(line.charAt(0)+"") < 6 && line.split("#").length==2 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[1]) < daysInMillis)
                    record[Integer.parseInt(line.charAt(0)+"")] +=
                            System.currentTimeMillis() -
                                    Long.parseLong(line.split("#")[1]);
                if (Integer.parseInt(line.charAt(0)+"") < 6 && line.split("#").length==3 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[2]) < daysInMillis)
                    record[Integer.parseInt(line.charAt(0)+"")] +=
                            Long.parseLong(line.split("#")[2]) -
                                    Long.parseLong(line.split("#")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public static long[] getEventTimeDays (int event, int days) {
        long[] record = new long[days+1];
        long daysInMillis = 86400000L*days;
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        try  {
            BufferedReader br = new BufferedReader(new FileReader(f0));
            String line;
            while ((line = br.readLine()) != null) {
                if (Integer.parseInt(line.charAt(0)+"") == event && line.split("#").length==2 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[1]) < daysInMillis)
                    record[days-(int)((System.currentTimeMillis() - Long.parseLong(line.split("#")[1])) /
                            (3600*1000*24))] +=
                            System.currentTimeMillis() -
                                    Long.parseLong(line.split("#")[1]);
                if (Integer.parseInt(line.charAt(0)+"") == event && line.split("#").length==3 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[2]) < daysInMillis)
                    record[days-(int)((System.currentTimeMillis() - Long.parseLong(line.split("#")[2])) /
                            (3600*1000*24))] +=
                            Long.parseLong(line.split("#")[2]) -
                                    Long.parseLong(line.split("#")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public static ArrayList<Point> getTimeNSize (int event, int day) {
        if (day <= 0) return new ArrayList<>();
        ArrayList<Point> list = new ArrayList<>();
        long sDaysInMillis = 86400000L*day;
        long eDaysInMillis = 86400000L*(day-1);
        File f = new File(local_file);
        File f0 = new File(f.getAbsolutePath(), eventRecordFile);
        try  {             BufferedReader br = new BufferedReader(new FileReader(f0));
            String line;
            while ((line = br.readLine()) != null) {
                if (Integer.parseInt(line.charAt(0)+"") == event && line.split("#").length==2 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[1]) > eDaysInMillis &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[1]) < sDaysInMillis)
                    list.add(new Point((int)((System.currentTimeMillis()%(3600000*24)/3600000 +
                            Long.parseLong(line.split("#")[1])%(3600000*24)/3600000)/2),

                            (int)((System.currentTimeMillis() - Long.parseLong(line.split("#")[1]))
                                    /3600000F)));
                if (Integer.parseInt(line.charAt(0)+"") == event && line.split("#").length==3 &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[2]) > eDaysInMillis &&
                        System.currentTimeMillis() - Long.parseLong(line.split("#")[1]) < sDaysInMillis)
                    list.add(new Point((int)((Long.parseLong(line.split("#")[2])%(3600000*24)/3600000 +
                            Long.parseLong(line.split("#")[1])%(3600000*24)/3600000)/2),
                            (int)((Long.parseLong(line.split("#")[2]) - Long.parseLong(line.split("#")[1]))/3600000F)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
