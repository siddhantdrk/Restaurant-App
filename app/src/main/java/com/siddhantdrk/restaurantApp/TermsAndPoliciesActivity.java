package com.siddhantdrk.restaurantApp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TermsAndPoliciesActivity extends AppCompatActivity {

    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_and_policies);

        backBtn = findViewById(R.id.back_btn_terms);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent=new Intent(TermsAndPoliciesActivity.this, MainActivity.class);
//                startActivity(intent);
                finish();
            }
        });
    }
}