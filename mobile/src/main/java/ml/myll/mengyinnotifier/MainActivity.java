package ml.myll.mengyinnotifier;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
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

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.github.clans.fab.FloatingActionMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.mikephil.charting.animation.Easing;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ObservableScrollViewCallbacks,
        OnChartValueSelectedListener{


    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String TAG = "Main";
    private final static int REQUEST_CODE = 8699;

    public static int notificationId = 86998699;
    public static boolean isRunning = false;


    //Views
    private Toolbar toolbar;
    //Scroll View related views
    private ImageView mImageView;
    private View mOverlayView;
    private ObservableScrollView mScrollView;
    private TextView mTitleView;
    //Floating Action Buttons
    private View yFab;
    private FloatingActionMenu bFab;
    //Navigation Drawer
    private DrawerLayout drawer;
    private NavigationView navigationView;
    //Information Views
    private ProgressBar progressBar;
    private TextView time_text;
    private PieChart pChart;

    //Basic values
    private int mActionBarSize;
    private int mFlexibleSpaceShowyFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int yFabMargin;
    private boolean yFabIsShown;
    private Point screenSize;
    //The time that have left
    private float timePercent;

    //Date picker opened
    public boolean opened = false;

    //Share
    ShareActionProvider mShareActionProvider;

    //Handler for date picker
    private final Handler handler = new Handler();
    private Runnable yFabLongPressed = new Runnable() {
        public void run() {
            Log.i(TAG, "yFab Long press");
            expandyFab(1);
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
        Log.v(TAG, "onScrollChanged called");

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
        int maxyFabTranslationY = mFlexibleSpaceImageHeight - yFab.getHeight() / 2;
        float yFabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - yFab.getHeight() / 2,
                mActionBarSize - yFab.getHeight() / 2,
                maxyFabTranslationY);
        yFab.setTranslationX(mOverlayView.getWidth() - yFabMargin - yFab.getWidth());
        yFab.setTranslationY(yFabTranslationY);

        // Show/hide FAB
        if (yFabTranslationY < mFlexibleSpaceShowyFabOffset) {
            hideyFab();
        } else {
            showyFab();
        }
    }
    @Override
    public void onDownMotionEvent() {
        Log.i(TAG, "onDownMotionEvent called");
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        Log.i(TAG, "onUCMotionEvent called");
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

    //Show and hide yFab
    private void showyFab() {
        Log.v(TAG, "yFab showing Fab");
        if (!yFabIsShown) {
            ViewPropertyAnimator.animate(yFab).cancel();
            ViewPropertyAnimator.animate(yFab).scaleX(1).scaleY(1).setDuration(200).start();
            yFab.setClickable(true);
            yFab.setActivated(true);
            yFab.setEnabled(true);
            yFabIsShown = true;
        }
    }

    private void hideyFab() {
        Log.v(TAG, "yFab hiding Fab");
        if (yFabIsShown) {
            ViewPropertyAnimator.animate(yFab).cancel();
            ViewPropertyAnimator.animate(yFab).scaleX(0).scaleY(0).setDuration(200).start();
            yFab.setClickable(false);
            yFab.setActivated(false);
            yFab.setEnabled(false);
            yFabIsShown = false;
        }
    }

    private void expandyFab(int mode) {
        if (mode == 1) {
            ValueAnimator animator = ValueAnimator.ofFloat(yFab.getScaleX(), 35).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    yFab.setScaleX(translationY);
                    yFab.setScaleY(translationY);
                }
            });
            animator.start();
            Log.i(TAG, "yFab expanding Fab");
        } else if (mode == 0) {
            ValueAnimator animator = ValueAnimator.ofFloat(yFab.getScaleX(), 1).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    yFab.setScaleX(translationY);
                    yFab.setScaleY(translationY);
                }
            });
            animator.start();
            Log.i(TAG, "yFab shrinking Fab");
        }
    }

    //spin bFab
    private void spinbFab () {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 360).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                bFab.getMenuIconView().setRotation(angle);
            }
        });
        animator.start();
    }

    //Update TimeCount ProgressBar
    public void updateProgress () {
        Log.i(TAG, "progressBar Updating Progress...");
        SharedPreferences settings = getSharedPreferences(SettingActivity.PREFS_NAME, 0);
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
        ValueAnimator animator = ValueAnimator.ofFloat(100, timePercent).setDuration(3000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                progressBar.setProgress(100- (int) percent);
                String minus = percent<0?"-":"";
                String text = getString(R.string.progress);
                time_text.setText(text
                        .replace("DD", minus+(Math.abs((int)(percent%(1.0F/12.0F)*365)) < 10 ? "0" : "")
                                + Math.abs((int)(percent%(1.0F/12.0F)*365)))
                        .replace("MM", minus+(Math.abs((int)(percent%1*12)) < 10 ? "0" : "") + Math.abs((int)(percent%1*12)))
                        .replace("YYYY", minus+(Math.abs((int)(percent/1)) < 10 ? "0" : "")
                                +(Math.abs((int)(percent/1)) < 100 ? "0" : "") + Math.abs((int)(percent)))
                );

                (findViewById(R.id.back_layout)).setBackgroundColor(
                        Color.rgb(236,240,241));
                if (percent<0){
                    progressBar.getProgressDrawable().setColorFilter(
                            Color.rgb(255,0,0), PorterDuff.Mode.SRC_IN);
//                    (findViewById(R.id.back_layout)).setBackgroundColor(
//                            Color.rgb(255,0,0));
                } else if (percent>100){
                    progressBar.getProgressDrawable().setColorFilter(
                            Color.rgb(0,255,0), PorterDuff.Mode.SRC_IN);
//                    (findViewById(R.id.back_layout)).setBackgroundColor(
//                            Color.rgb(0,255,0));
                } else {
                    progressBar.getProgressDrawable().setColorFilter(
                            Color.rgb(percent <= 50 ? 255 : (int) (255 * (1 - (percent - 50) / 50.0)),
                                    percent >= 50 ? 255 : (int) (255 * (percent / 50.0)),
                                    0), PorterDuff.Mode.SRC_IN);
//                    (findViewById(R.id.back_layout)).setBackgroundColor(
//                            Color.rgb(percent <= 50 ? 255 : (int) (255 * (1 - (percent - 50) / 50.0)),
//                            percent >= 50 ? 255 : (int) (255 * (percent / 50.0)),
//                            0));
                }
            }
        });
        animator.start();
    }

    //ToolBar Translation
    private boolean toolbarIsShown() {
        if (toolbar.getTranslationY() == 0) Log.i(TAG, "toolBar is completely shown");
        return toolbar.getTranslationY() == 0;
    }

    private boolean toolbarIsHidden() {
        if (toolbar.getTranslationY() == -toolbar.getHeight()) Log.i(TAG, "toolBar is completely hidden");
        return toolbar.getTranslationY() == -toolbar.getHeight();
    }

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-toolbar.getHeight());
    }

    private void moveToolbar(float toTranslationY) {
        Log.i(TAG, "toolBar Moving toolBar");
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

    //Refresh Views
    private void refresh () {
        if (Build.VERSION.SDK_INT >= 21 && CommonUtils.colorDynamic) {
            getWindow().setStatusBarColor(CommonUtils.getColorsFromItems()[CommonUtils.currEvent]);
            getWindow().setNavigationBarColor(CommonUtils.getColorsFromItems()[CommonUtils.currEvent]);
        }
        initViews(1);
        spinbFab();
        updateNotification();
        Toast.makeText(this, "Refreshed!", Toast.LENGTH_LONG).show();
    }

    //Scroll back to the top ScrollView
    private void scrollBack() {
        Log.i(TAG, "scrollView scrolling back...");
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

    //Pie Chart Overrides
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i(TAG, "pieChart Value selected");
        if (e == null)
            return;
        Log.i(TAG,
                "pieChart Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("index", (int)h.getX());
        startActivity(intent);
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "pieChart Nothing");
    }

    //Navigation Drawer Overrides
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
        if (id == R.id.nav_share) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.isChecked()) return true;
        int id = item.getItemId();

        if(R.id.nav_setting == id) {
            if (Build.VERSION.SDK_INT >= 21) {
//                Intent intent = new Intent(this, SettingActivity.class);
//                startActivity(intent);
                int[] colorChoices = null;
//                try {
//                    Field[] fields = Class.forName(getPackageName() + ".R$color").getDeclaredFields();
//                    colorChoices = new int[fields.length];
//                    int i = 0;
//                    for (Field field : fields) {
//                        String colorName = field.getName();
//                        int colorId = field.getInt(null);
//                        int color = getResources().getColor(colorId);
//                        colorChoices[i++] = color;
//                        Log.i(TAG, "Added " + colorName + " => " + colorId + " => " + color);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                if (colorChoices == null) colorChoices = CommonUtils.getColorsFromItems();
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.color_picker, colorChoices, colorChoices[0], 3, colorChoices.length);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            getWindow().setStatusBarColor(color);
                            getWindow().setNavigationBarColor(color);
                        }
                    }
                });
                colorPickerDialog.show(getFragmentManager(), TAG);
                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            } else Toast.makeText(this, "4.4 or lower Not Supported", Toast.LENGTH_SHORT).show();
            return true;
        }
        if(R.id.add_item == id) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }

        for (int i = 0; i < CommonUtils.drawerItemsIds.size(); i++) {
            if (id == CommonUtils.drawerItemsIds.get(i)) {
                CommonUtils.newEvent(i);
                renewCurEvent(i);
                Log.i(TAG, "Event changed to " + i);
                item.setChecked(true);
                if (Build.VERSION.SDK_INT >= 21 && CommonUtils.colorDynamic) {
                    getWindow().setStatusBarColor(CommonUtils.getColorsFromItems()[CommonUtils.currEvent]);
                    getWindow().setNavigationBarColor(CommonUtils.getColorsFromItems()[CommonUtils.currEvent]);
                }
                refresh();
                return true;
            }
        }
        return false;
    }

    //Activity overRides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new MExceptionHandler(
                CommonUtils.local_file));
        setContentView(R.layout.activity_main);

        CommonUtils.initItems();

        firstCheck();

        //Set Toolbar as Action Bar
        this.setSupportActionBar(toolbar);

        Log.i(TAG, "onCreate Getting Attributes...");
        setAttributes();

        Log.i(TAG, "onCreate Finding Views...");
        findViews();

        Log.i(TAG, "onCreate Initializing Views...");
        initViews(0);

        Log.i(TAG, "onCreate doing Intro View...");
        introView();

        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, false, false);
            }
        });

        updateNotification();

        Log.i(TAG, "onCreate All Done!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
        Log.i(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called, Current Event is:" + CommonUtils.items.get(CommonUtils.currEvent).getName());
        refresh();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "OnStop Called, Launching Service");
        super.onStop();
        isRunning = false;
        Intent intent = new Intent(this, MovementCheckService.class);
        startService(intent);
    }

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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult called");
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
        }
    }


    //Helper methods
    //Called when onCreate to set all attributes
    private void setAttributes() {
        Log.i(TAG, "setAttribute called");
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowyFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);

        //Get Action Bar Size...
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.i(TAG, "attributes ABSize:" + mActionBarSize);

        //Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getRealSize(screenSize);
        Log.i(TAG, "attributes ScreenSize:" + screenSize.toString());

        SharedPreferences settings = getSharedPreferences(SettingActivity.PREFS_NAME, 0);
        int years = settings.getInt("year", 1900);
        timePercent = (100 + years - Calendar.getInstance().get(Calendar.YEAR));
        Log.i(TAG, "attributes getting SHARED_PREF: years "+years);
    }

    //Called when onCreate find all views corresponding to variables
    private void findViews () {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mImageView = (ImageView) findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        yFab = findViewById(R.id.fab);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mTitleView = (TextView) findViewById(R.id.title);
        bFab = (FloatingActionMenu) findViewById(R.id.fab2);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        progressBar = (ProgressBar) findViewById(R.id.timeBar);
        time_text = (TextView) findViewById(R.id.intro_time);
    }

    //0 when onCreate; 1 when refresh.
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
                Log.i(TAG, "initView height of StatusBar+Nav:" + result);
                must.setMinimumHeight(screenSize.y);
                must.getLayoutParams().height = screenSize.y - result > inner.getLayoutParams().height ? screenSize.y - result : inner.getLayoutParams().height;
                must.requestLayout();

                mImageView.setImageBitmap(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.bg));
                mScrollView.setScrollViewCallbacks(this);
                mTitleView.setText(getTitle());
                setTitle(null);

                yFab.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (opened) return false;
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            handler.postDelayed(yFabLongPressed, 600);
                            expandyFab(1);
                            return true;
                        }
                        if ((event.getAction() == MotionEvent.ACTION_UP)) {
                            handler.removeCallbacks(yFabLongPressed);
                            expandyFab(0);
                            return true;
                        }
                        return false;
                    }
                });
                yFab.setClickable(false);
                yFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
                yFab.setScaleX(0);
                yFab.setScaleY(0);

                (findViewById(R.id.manage)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), WeekViewActivity.class);
                        Log.i(TAG, "weekViewActivity starting...");
                        startActivity(intent);
                        Log.i(TAG, "calling onPause");
                        onPause();
                    }
                });
                (findViewById(R.id.refresh)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i(TAG, "calling refresh...");
                        refresh();
                    }
                });
                (findViewById(R.id.scroll_back)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), WeekViewActivity.class);
                        Log.i(TAG, "Scrolling back...");
                        scrollBack();
                    }
                });

                pChart = new PieChart(this);
                inner.addView(pChart);
                pChart.getLayoutParams().height = screenSize.y * 3 / 4;
                pChart.requestLayout();

                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();

                navigationView.setNavigationItemSelectedListener(this);
                navigationView.inflateMenu(R.menu.activity_main_drawer);
            case 1:
                updateNotification();

                navigationView.getMenu().getItem(CommonUtils.currEvent).setChecked(true);
                updateProgress();

                if(pChart != null)initChart(pChart);
        }
    }

    //Show intro view
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
                .setTarget(yFab)
                .setUsageId("yFab1")
                .show();
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText(getString(R.string.introfab2))
                .setTarget(bFab)
                .setUsageId("bFab1")
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
        mChart.setHoleColor(CommonUtils.getColorsFromItems()[CommonUtils.currEvent]);

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

        setData(mChart);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutBounce);

//        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(12f);
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString(">" + CommonUtils.getNamesFromItems()[CommonUtils.currEvent]+"中" +"\n时间管理分析系统");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 4, 0);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 4, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 4, s.length() - 4, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 4, s.length() - 4, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 4, s.length() - 4, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 4, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 4, s.length(), 0);
        return s;
    }

    private void setData(PieChart mChart) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        String[] items = CommonUtils.getNamesFromItems();
        long[] times = CommonUtils.getTotalTime();

//        Log.e(TAG, "sizes: "+CommonUtils.items.size()+" "+items.length+" "+times.length);
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < items.length ; i++) {
            PieEntry ent = new PieEntry((float)times[i], items[i]);
            entries.add(ent);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextColor(Color.WHITE);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : CommonUtils.getColorsFromItems())
            colors.add(c);

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

    //Date Picker inner class
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            MainActivity a = (MainActivity)getActivity();
            a.expandyFab(1);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, 2000, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            SharedPreferences preferences = getActivity().getSharedPreferences(SettingActivity.PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("year", year);
            editor.putInt("month", month);
            editor.putInt("day", day);
            editor.apply();
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.expandyFab(0);
            mainActivity.opened=false;
            mainActivity.updateProgress();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.opened=false;
            mainActivity.expandyFab(0);
        }
    }

    //Update the "curEvent" shared pref to new int
    private void updateEventSharedPref (int curEvent) {
        SharedPreferences preferences = getSharedPreferences(SettingActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("curEvent", curEvent);
        editor.apply();
    }

    //Check internal FILE and renew CommonUtil.curEvent to that value. Default 5
    private void firstCheck () {
        Log.i(TAG, "onCreate Read External:");
        int ev = CommonUtils.getCurrEventFromExternal();
        if (ev == -1) CommonUtils.createRecordIfNotCreated();
        CommonUtils.currEvent = CommonUtils.getCurrEventFromExternal();
        Log.i(TAG, "onCreate Prev event is: "+CommonUtils.currEvent);
    }

    //Update internal FILE
    private void renewCurEvent (int cur){
        updateNotification();
    }

    //Setting share Intent for sharing
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void addNewCategory (String name, int color) {
        //Write new Category to Record

        //Renew Information in CommonUtils

        //Update local views
        addItemToDrawer(name);
        refresh();
    }

    private void addItemToDrawer (String name) {
        final Menu menu = navigationView.getMenu();
        menu.add(name);
    }

    public void updateNotification () {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// Sets an ID for the notification, so it can be updated
        int notifyID = MainActivity.notificationId;
        Intent window = new Intent(getApplicationContext(), FloatingActivity.class);
        window.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("MY Time")
                .setContentText("Expand to check Time")
                .setSmallIcon(R.drawable.scaledicon)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, window, 0));
// Start of a loop that processes data and then notifies the user
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = CommonUtils.getNamesFromItems();
// Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Time spending details:");
// Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {
            PendingIntent pendingIntent;
            Intent intent = new Intent();
            intent.setClass(this, NotificationReceiver.class);
            intent.putExtra("event", i);
            intent.setAction("action1");
            intent.addCategory("category1");
            pendingIntent =  PendingIntent.getBroadcast(this, i, intent, 0);
            if (Arrays.asList(CommonUtils.shortcuts).contains(i)) {
                NotificationCompat.Action action = new NotificationCompat.Action(R.color.lime, events[i], pendingIntent);
                mBuilder.addAction(action);
            }
            inboxStyle.addLine(events[i]+": "+(CommonUtils.getTotalTime()[i]/1000/3600+" Hours"));
        }
// Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);
        mBuilder.setOngoing(CommonUtils.stickyNotification);
        mNotificationManager.notify(
                notifyID,
                mBuilder.build());
    }
}
