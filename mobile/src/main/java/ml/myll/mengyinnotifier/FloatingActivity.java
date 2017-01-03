package ml.myll.mengyinnotifier;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class FloatingActivity extends FloatingBaseActivity {
    /**
     * This method overrides the MainActivity method to set up the actual window for the popup.
     * This is really the only method needed to turn the app into popup form. Any other methods would change the behavior of the UI.
     * Call this method at the beginning of the main activity.
     * You can't call setContentView(...) before calling the window service because it will throw an error every time.
     */
    @Override
    public void setUpWindow() {

        // Creates the layout for the window and the look of it
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = .9f;    // lower than one makes it more transparent
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
            getWindow().setLayout((int) (width * .6), (int) (height * .7));
        } else {
            getWindow().setLayout((int) (width * .6), (int) (height * .6));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
