package ml.myll.mengyinnotifier;

import android.*;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ObservableScrollViewCallbacks, OnChartValueSelectedListener {


    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String TAG = "Main";
    public static final String PREFS_NAME = "Settings";
    final static int REQUEST_CODE = 8699;
    final static String FILENAME = "file";

    //Tool Bar
    Toolbar toolbar;

    //Views
    private ImageView mImageView;
    private View mOverlayView;
    private ObservableScrollView mScrollView;
    private TextView mTitleView;
    private View mFab;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ProgressBar progressBar;
    private TextView time_text;

    //Basic values
    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private boolean mFabIsShown;
    private Point screenSize;
    private float timePercent;
    public boolean opened = false;
    private int currEvent;

    //Share
    ShareActionProvider mShareActionProvider;

    //Handler for date picker
    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            Log.i("", "Long press!");
            expandFAB(1);
            Toast.makeText(MainActivity.this, "Please set your Birthday", Toast.LENGTH_LONG).show();
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
            opened=true;
        }
    };

    //ScrollView
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll,
                                boolean dragging) {

        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        mOverlayView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        mImageView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        mOverlayView.setAlpha(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        mTitleView.setPivotX(0);
        mTitleView.setPivotY(0);
        mTitleView.setScaleX(scale);
        mTitleView.setScaleY(scale);

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        mTitleView.setTranslationY(titleTranslationY);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - mFab.getHeight() / 2,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);
        mFab.setTranslationX(mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
        mFab.setTranslationY(fabTranslationY);

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }
    }
    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }


    //Show and hide Fab
    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFab.setClickable(true);
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFab.setClickable(false);
            mFabIsShown = false;
        }
    }

    public void expandFAB(int mode) {
        if (mode == 1) {
            ValueAnimator animator = ValueAnimator.ofFloat(mFab.getScaleX(), 30).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    mFab.setScaleX(translationY);
                    mFab.setScaleY(translationY);
                }
            });
            animator.start();
        } else if (mode == 0) {
            ValueAnimator animator = ValueAnimator.ofFloat(mFab.getScaleX(), 1).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    mFab.setScaleX(translationY);
                    mFab.setScaleY(translationY);
                }
            });
            animator.start();
        }
    }

    //Update TimeCount ProgressBar
    public void updateProgress () {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int year = settings.getInt("year", 2000);
        int month = settings.getInt("month", 1);
        int day = settings.getInt("day",1);
        timePercent =(year
                + (float)month/12.0F
                + (float)day/365.0F + 100)
                - Calendar.getInstance().get(Calendar.YEAR)
                - Calendar.getInstance().get(Calendar.MONTH)/12.0F
                - Calendar.getInstance().get(Calendar.DAY_OF_MONTH)/365.0F;
        progressBar.setScaleY(15f);
        ValueAnimator animator = ValueAnimator.ofFloat(100.0F, timePercent).setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                progressBar.setProgress(100- (int) percent);
                String minus = percent<0?"-":"";
                String text = getString(R.string.progress);
                time_text.setText(text
                        .replace("0000", minus+(Math.abs((int)(percent/1)) < 10 ? "0" : "")
                                +(Math.abs((int)(percent/1)) < 100 ? "0" : "") + Math.abs((int)(percent)))
                        .replace("000", minus+((int)(percent%1*12) < 10 ? "0" : "") + Math.abs((int)(percent%1*12)))
                        .replace("00", minus+((int)(percent%(1.0F/12.0F)*365) < 10 ? "0" : "")
                                + Math.abs((int)(percent%(1.0F/12.0F)*365)))
                );
                if (percent < 20) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 40) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 127, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 60) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 80) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(127, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent <= 100) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(0, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
        animator.start();
    }

    //Show and hide Toolbar
    private boolean toolbarIsShown() {
        return toolbar.getTranslationY() == 0;
    }

    private boolean toolbarIsHidden() {
        return toolbar.getTranslationY() == -toolbar.getHeight();
    }

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-toolbar.getHeight());
    }

    private void moveToolbar(float toTranslationY) {
        ValueAnimator animator = ValueAnimator.ofFloat(toolbar.getTranslationY(), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                toolbar.setTranslationY(translationY);
            }
        });
        animator.start();
    }

    //Scroll back to the top
    private void scrollBack() {
        onUpOrCancelMotionEvent(ScrollState.DOWN);
        ValueAnimator animator = ValueAnimator.ofFloat(mScrollView.getCurrentScrollY(), 0).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                mScrollView.scrollTo(0,(int)translationY);
            }
        });
        animator.start();
    }

    //Pie Chart
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());

        Intent intent = new Intent(this, WeekViewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "Nothing");
    }

    //Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            FileInputStream fis = openFileInput(FILENAME);
            Scanner scanner = new Scanner(fis);
            int c = scanner.nextInt();
            Log.i(TAG, "Read "+c);
            if (c<6) currEvent = c;
            Log.i(TAG, "Prev event is "+currEvent);
            scanner.close();
        } catch (Exception e) {
            try {
                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write((0+"").getBytes());
                fos.flush();fos.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }

        //Set Toolbar as Action Bar
        this.setSupportActionBar(toolbar);

        Log.i(TAG, "Getting Attributes...");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermission();
        setAttributes();

        Log.i(TAG, "Finding Views...");
        findViews();

        Log.i(TAG, "Initializing Views...");
        initViews(0);
        introView();

        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, false, false);
            }
        });

        Log.i(TAG, "OnCreate All Done!");
    }

    private void requestPermission() {
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "We need to store the data in your phone.", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            CommonUtils.createRecordIfNotCreated();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Got it!", Toast.LENGTH_LONG).show();
                    CommonUtils.createRecordIfNotCreated();
                } else {
                    Toast.makeText(this, "The App is not gonna work for now", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setAttributes() {
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);

        //Get Action Bar Size...
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.i(TAG, "ABSize:" + mActionBarSize);

        //Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getRealSize(screenSize);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int years = settings.getInt("year", 1900);
        timePercent = (100 + years - Calendar.getInstance().get(Calendar.YEAR));

    }

    private void findViews () {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mImageView = (ImageView) findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mTitleView = (TextView) findViewById(R.id.title);
        mFab = findViewById(R.id.fab);
        fab = (FloatingActionButton) findViewById(R.id.fab2);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        progressBar = (ProgressBar) findViewById(R.id.timeBar);
        time_text = (TextView) findViewById(R.id.intro_time);
    }

    private void initViews (int mode) {
        RelativeLayout must = (RelativeLayout) findViewById(R.id.statusView);
        LinearLayout inner = (LinearLayout) findViewById(R.id.mustL);
        switch (mode) {
            case 0:
            this.setSupportActionBar(toolbar);

            int result = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
            resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result += getResources().getDimensionPixelSize(resourceId);
            }
            Log.i(TAG, "Status bar+Nav:" + result + "");
            must.setMinimumHeight(screenSize.y);
            must.getLayoutParams().height = screenSize.y - result > inner.getLayoutParams().height ? screenSize.y - result : inner.getLayoutParams().height;
            must.requestLayout();

            mImageView.setImageBitmap(
                    CommonUtils.decodeSampledBitmapFromResource(getResources(),
                            R.drawable.bg, screenSize.x, R.dimen.flexible_space_image_height));
            mScrollView.setScrollViewCallbacks(this);
            mTitleView.setText(getTitle());
            setTitle(null);

            mFab.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (opened) return false;
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        handler.postDelayed(mLongPressed, 600);
                        expandFAB(1);
                        return true;
                    }
                    if ((event.getAction() == MotionEvent.ACTION_UP)) {
                        handler.removeCallbacks(mLongPressed);
                        expandFAB(0);
                        return true;
                    }
                    return false;
                }
            });
            mFab.setClickable(false);
            mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
            mFab.setScaleX(0);
            mFab.setScaleY(0);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refresh();
                }
            });

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().getItem(currEvent).setChecked(true);
            case 1:
            updateProgress();

            PieChart chart = new PieChart(this);

            initChart(chart);
            inner.addView(chart);
            chart.getLayoutParams().height = screenSize.y * 3 / 4;
            chart.requestLayout();
        }
    }

    private void refresh () {
        initViews(1);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 360).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                fab.setRotation(angle);
            }
        });
        animator.start();
        scrollBack();
        Toast.makeText(this, "Refreshed!", Toast.LENGTH_LONG).show();
    }

    private void introView() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText(getString(R.string.introfab))
                .setTarget(mFab)
                .setUsageId("mFab1")
                .show();
    }

    //Setup Chart
    private void initChart(PieChart mChart){
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(R.color.primary);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(100, mChart);

//        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(12f);
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("萌音嘹亮\n时间管理分析系统");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 4, 0);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 4, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 4, s.length() - 4, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 4, s.length() - 4, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 4, s.length() - 4, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 4, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 4, s.length(), 0);
        return s;
    }

    private void setData(float range, PieChart mChart) {
        int count = 6;
        float mult = range;

        ArrayList<PieEntry> entries = new ArrayList<>();

        String[] items = {"睡觉", "工作", "学习", "娱乐", "生活", "其他"};

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < count ; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5), items[i]));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextColor(Color.WHITE);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    //Date Picker
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            MainActivity a = (MainActivity)getActivity();
            a.expandFAB(1);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, 2000, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("year", year);
            editor.putInt("month", month);
            editor.putInt("day", day);
            editor.apply();
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.expandFAB(0);
            mainActivity.opened=false;
            mainActivity.updateProgress();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.opened=false;
            mainActivity.expandFAB(0);
        }
    }

    /**
     * Override Back Pressed to close the drawer if opened
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        scrollBack();

        MenuItem shareItem = menu.findItem(R.id.nav_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "我在使用萌音时间管理系统耶~你也来试试吧！");
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);
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
        } if (id == R.id.nav_share) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fos == null) return false;
        switch (id) {
        case R.id.sleep:
            try{
                fos.write((0+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 0");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.work:
            try{
                fos.write((1+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 1");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.study:
            try{
                fos.write((2+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 2");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.sustain:
            try{
                fos.write((3+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 3");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.recreation:
            try{
                fos.write((4+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.other:
            try{
                fos.write((5+"").getBytes());
                fos.flush();fos.close();
                Log.i(TAG, "Event changed to 5");
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.setChecked(true);
            break;
        case R.id.nav_setting:
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            return true;
        default:
            break;
        }
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}
