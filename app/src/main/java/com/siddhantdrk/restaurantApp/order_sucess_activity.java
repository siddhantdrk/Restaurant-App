package com.siddhantdrk.restaurantApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class order_sucess_activity extends AppCompatActivity {

    private static int TIME_OUT = 2000; //Time to launch the another activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_success);
        Intent intent = getIntent();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(order_sucess_activity.this,order_details.class);
                i.putExtra("id",intent.getStringExtra("id"));
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }
}