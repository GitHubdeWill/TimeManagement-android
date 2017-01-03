package ml.myll.mengyinnotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by will on 1/3/2017.
 */
public class FloatingBaseActivity extends Activity {

    public Context context;

    /**
     * Called when the activity is first created to set up all of the features.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CommonUtils.getNamesFromItems().length<6)CommonUtils.initItems();

        context = this;

        // If the user is in the PopupMainActivity function, the setUpWindow function would be called from that class
        // otherwise it would call the function from this class that has no implementation.
        setUpWindow();

        // Make sure to set your content view AFTER you have set up the window or it will crash.
        setContentView(R.layout.activity_floating);
        ListView listView = (ListView)findViewById(R.id.event_list);

        setTitle("Now "+CommonUtils.getNamesFromItems()[CommonUtils.currEvent]);

        String[] values = CommonUtils.getNamesFromItems();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CommonUtils.newEvent(position);
                Toast.makeText(getApplicationContext(), "Event Changed to "+CommonUtils.getNamesFromItems()[position], Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
        // Again, this will call either the function from this class or the PopupMainActivity one,
        // depending on where the user is
        setUpButton();
    }

    public void setUpWindow() {
        // Nothing here because we don't need to set up anything extra for the full app.
    }

    public void setUpButton() {

    }
}