package ml.myll.mengyinnotifier;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.yalantis.starwars.TilesFrameLayout;
import com.yalantis.starwars.interfaces.TilesFrameLayoutListener;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity  implements TilesFrameLayoutListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static int index = 0;

    private TilesFrameLayout mTilesFrameLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        index = getIntent().getIntExtra("index", 0);

        super.onCreate(savedInstanceState);
        setUpWindow();
        setContentView(R.layout.activity_detail);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTilesFrameLayout = (TilesFrameLayout) findViewById(R.id.tiles_frame_layout1);
        mTilesFrameLayout.setOnAnimationFinishedListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fabf);
        fab.setBackgroundColor(CommonUtils.getColorsFromItems()[index]);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setAdapter(mSectionsPagerAdapter);
                ValueAnimator animator = ValueAnimator.ofFloat(0, 360).setDuration(500);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float angle = (float) animation.getAnimatedValue();
                        fab.setRotation(angle);
                    }
                });
                animator.start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mTilesFrameLayout.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTilesFrameLayout.onPause();
    }


    public void setUpWindow() {

        // Creates the layout for the window and the look of it
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = .95f;    // lower than one makes it more transparent
        params.dimAmount = .5f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "table_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            int type = getArguments().getInt(ARG_SECTION_NUMBER);
            switch (type){
                case 0:
                    LineChart lc = getLineChart();
                    ((LinearLayout)rootView.findViewById(R.id.chartContainer)).addView(lc);
                    lc.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    break;
                case 1:
                    BubbleChart bc = getBubbleChart();
                    ((LinearLayout)rootView.findViewById(R.id.chartContainer)).addView(bc);
                    bc.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    break;
                case 2:
                    RadarChart rc = getRadarChart();
                    ((LinearLayout)rootView.findViewById(R.id.chartContainer)).addView(rc);
                    rc.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    break;
                default:
                    break;
            }
            return rootView;
        }

        private static RadarChart getRadarChart () {
            RadarChart mChart = new RadarChart(MyllApplication.getAppContext());
            mChart.setBackgroundColor(Color.rgb(60, 65, 82));

            mChart.getDescription().setEnabled(false);

            mChart.setWebLineWidth(1f);
            mChart.setWebColor(Color.LTGRAY);
            mChart.setWebLineWidthInner(1f);
            mChart.setWebColorInner(Color.LTGRAY);
            mChart.setWebAlpha(100);

            // create a custom MarkerView (extend MarkerView) and specify the layout
            // to use for it
//            MarkerView mv = new RadarMarkerView(this, R.layout.radar_markerview);
//            mv.setChartView(mChart); // For bounds control
//            mChart.setMarker(mv); // Set the marker to the chart

            setRadarData(mChart);

            mChart.animateXY(
                    1400, 1400,
                    Easing.EasingOption.EaseInOutQuad,
                    Easing.EasingOption.EaseInOutQuad);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setTextSize(9f);
            xAxis.setYOffset(0f);
            xAxis.setXOffset(0f);
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                private String[] mActivities = CommonUtils.getNamesFromItems();

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return mActivities[(int) value % mActivities.length];
                }
            });
            xAxis.setTextColor(Color.WHITE);

            YAxis yAxis = mChart.getYAxis();
            yAxis.setLabelCount(5, false);
            yAxis.setTextSize(9f);
            yAxis.setAxisMinimum(0f);
            long[] t = CommonUtils.getTotalTime();
            long limit = 0;
            for (long l : t) if(l>limit) limit = l;
            yAxis.setAxisMaximum((float)limit);
            yAxis.setDrawLabels(false);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(5f);
            l.setTextColor(Color.WHITE);

            return mChart;
        }

        private static void setRadarData(RadarChart mChart){
            int cnt = 6;

            ArrayList<RadarEntry> entries1 = new ArrayList<>();
            ArrayList<RadarEntry> entries2 = new ArrayList<>();
            ArrayList<RadarEntry> entries3 = new ArrayList<>();

            // NOTE: The order of the entries when being added to the entries array determines their position around the center of
            // the chart.
            long[] time = CommonUtils.getTotalTime();
            long[] weekTime = CommonUtils.getTimeDays(7);
            long[] dayTime = CommonUtils.getTimeDays(1);
            long maxT = 1;
            long maxW = 1;
            long maxD = 1;
            for (int i = 0; i < cnt; i++) {
                if (maxT < time[i]) maxT = time[i];
                if (maxW < weekTime[i]) maxW = weekTime[i];
                if (maxD < dayTime[i]) maxD = dayTime[i];
            }
            for (int i = 0; i < cnt; i++) {
                float val1 = (float)time[i]*((float)maxW/(float)maxT);
                entries1.add(new RadarEntry(val1));

                float val2 = (float)weekTime[i];
                entries2.add(new RadarEntry(val2));

                float val3 = (float) dayTime[i]* ((float)maxW/(float)maxD);
                entries3.add(new RadarEntry(val3));
            }

            RadarDataSet set1 = new RadarDataSet(entries1, "Total (Scaled)");
            set1.setColor(Color.rgb(121, 162, 175));
            set1.setFillColor(Color.rgb(121, 162, 175));
            set1.setDrawFilled(true);
            set1.setFillAlpha(180);
            set1.setLineWidth(2f);
            set1.setDrawHighlightCircleEnabled(true);
            set1.setDrawHighlightIndicators(false);

            RadarDataSet set2 = new RadarDataSet(entries2, "Week (Scaled)");
            set2.setColor(Color.rgb(103, 110, 129));
            set2.setFillColor(Color.rgb(103, 110, 129));
            set2.setDrawFilled(true);
            set2.setFillAlpha(180);
            set2.setLineWidth(2f);
            set2.setDrawHighlightCircleEnabled(true);
            set2.setDrawHighlightIndicators(false);

            RadarDataSet set3 = new RadarDataSet(entries3, "Day(Scaled)");
            set3.setColor(Color.rgb(80, 90, 100));
            set3.setFillColor(Color.rgb(80, 90, 100));
            set3.setDrawFilled(true);
            set3.setFillAlpha(180);
            set3.setLineWidth(2f);
            set3.setDrawHighlightCircleEnabled(true);
            set3.setDrawHighlightIndicators(false);

            ArrayList<IRadarDataSet> sets = new ArrayList<>();
            sets.add(set1);
            sets.add(set2);
            sets.add(set3);

            RadarData data = new RadarData(sets);
            data.setValueTextSize(8f);
            data.setDrawValues(false);
            data.setValueTextColor(Color.WHITE);

            mChart.setData(data);
            mChart.invalidate();
        }

        private static BubbleChart getBubbleChart () {
            BubbleChart mChart = new BubbleChart(MyllApplication.getAppContext());
            mChart.getDescription().setEnabled(false);

            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {

                }

                @Override
                public void onNothingSelected() {

                }
            });

            mChart.setDrawGridBackground(false);

            mChart.setTouchEnabled(true);

            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);

            mChart.setMaxVisibleValueCount(200);
            mChart.setPinchZoom(true);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);

            YAxis yl = mChart.getAxisLeft();
            yl.setSpaceTop(10f);
            yl.setSpaceBottom(10f);
            yl.setDrawZeroLine(false);

            mChart.getAxisRight().setEnabled(false);

            XAxis xl = mChart.getXAxis();
            xl.setPosition(XAxis.XAxisPosition.BOTTOM);

            setBubbleData(mChart);

            return mChart;
        }

        private static void setBubbleData (BubbleChart mChart) {

            ArrayList<ArrayList<BubbleEntry>> yVals = new ArrayList<>(CommonUtils.getNamesFromItems().length);
            for (int k = 0; k < CommonUtils.getNamesFromItems().length; k++) {
                yVals.add(new ArrayList<BubbleEntry>());
                for (int i = 1; i <= CommonUtils.days; i++) {
                    ArrayList<Point> list = CommonUtils.getTimeNSize(k, i);
                    if (list.isEmpty()) continue;
                    for (int j = 0; j < list.size(); j++) {
                        float val = (float) (list.get(j).x);
                        float size = (float) (list.get(j).y);

                        yVals.get(k).add(new BubbleEntry(i, val, size));
                    }
                }
            }

            Log.i("DA", "Here");
            // create a dataset and give it a type
            ArrayList<IBubbleDataSet> dataSets = new ArrayList<>();
            for (int i = 0; i <CommonUtils.getNamesFromItems().length; i++) {
                if(yVals.size()-1 < i || yVals.get(i) == null || yVals.get(i).isEmpty())continue;
                BubbleDataSet set = new BubbleDataSet(yVals.get(i), CommonUtils.getNamesFromItems()[i]);
                set.setColor(CommonUtils.getColorsFromItems()[i], 130);
                set.setDrawValues(true);
                dataSets.add(set);
            }

            Log.i("DA", "Here");
            // create a data object with the datasets
            BubbleData data = new BubbleData(dataSets);
            data.setDrawValues(false);
            data.setValueTextSize(8f);
            data.setValueTextColor(Color.BLACK);
            data.setHighlightCircleWidth(1.5f);

            mChart.setData(data);
            mChart.invalidate();
        }

        private static LineChart getLineChart(){
            LineChart mChart = new LineChart(MyllApplication.getAppContext());
            mChart.setViewPortOffsets(0, 0, 0, 0);
            mChart.setBackgroundColor(Color.rgb(240, 235, 245));

            // no description text
            mChart.getDescription().setText(CommonUtils.getNamesFromItems()[index] + " Time in this week in Hours");
            mChart.getDescription().setTextSize(9f);

            // enable touch gestures
            mChart.setTouchEnabled(true);

            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);

            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);

            mChart.setDrawGridBackground(false);
            mChart.setMaxHighlightDistance(300);

            XAxis x = mChart.getXAxis();
            x.setEnabled(true);
            x.setLabelCount(CommonUtils.days, false);
            x.setTextColor(Color.BLACK);

            YAxis y = mChart.getAxisLeft();
            y.setLabelCount(7, false);
            y.setTextColor(Color.BLACK);
            y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.WHITE);

            // add data
            setLineData(mChart);

            mChart.getLegend().setEnabled(false);

            mChart.animateXY(2000, 2000);

            // dont forget to refresh the drawing
            mChart.invalidate();
            return mChart;

        }

        private static void setLineData(LineChart mChart) {

            int count = CommonUtils.days;

            ArrayList<Entry> yVals = new ArrayList<>();
            long[] times = CommonUtils.getEventTimeDays(index, CommonUtils.days);

            for (int i = 0; i < count; i++) {
                yVals.add(new Entry(i, times[i]/1000/3600F));
            }
            yVals.add(new Entry(count, times[count-1]/1000/3600F));

            LineDataSet set1;

            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
                set1.setValues(yVals);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(yVals, "Time in Mins");

                set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                set1.setCubicIntensity(0.2f);
                set1.setDrawFilled(true);
                set1.setDrawCircles(false);
                set1.setLineWidth(1.8f);
                set1.setCircleRadius(4f);
                set1.setCircleColor(Color.WHITE);
                set1.setHighLightColor(Color.rgb(244, 117, 117));
                set1.setColor(Color.WHITE);
                set1.setFillColor(CommonUtils.getColorsFromItems()[DetailActivity.index]);
                set1.setFillAlpha(100);
                set1.setDrawHorizontalHighlightIndicator(false);
                set1.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return -10;
                    }
                });

                // create a data object with the datasets
                LineData data = new LineData(set1);
                data.setValueTextSize(9f);
                data.setDrawValues(false);

                // set data
                mChart.setData(data);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        mTilesFrameLayout.startAnimation();
    }

    @Override
    public void onAnimationFinished() {
        finish();
    }
}
