package com.parksjg.its.gpslocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by josephparks on 3/7/16.
 */
public class Zero extends Activity {

    Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero);

        setupButton();

    }

    private void setupButton() {
        continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Zero.this, First.class);
                    startActivity(intent);


                } catch (Exception e) {
                    Log.d("TAG", "+++++++++++++++++++++++++ continue button error: " + e);
                }
            }
        });
    }
}
