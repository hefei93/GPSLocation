package com.parksjg.its.gpslocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by josephparks on 2/25/16.
 */
public class Second extends Activity {

    Button newTestButton;
    String startDate;
    TextView fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        setupUI();
        setupButton();

    }

    private void setupUI() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startDate = extras.getString("START_DATE");
        }

        fileName = (TextView) findViewById(R.id.fileName);
        fileName.setText(startDate);

        newTestButton = (Button) findViewById(R.id.newTestButton);
    }

    private void setupButton() {
        newTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), First.class);
                startActivity(intent);

            }
        });

    }

}
