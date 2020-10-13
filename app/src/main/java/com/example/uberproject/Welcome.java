package com.example.uberproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void turnDriverActivity(View view) {
        startActivity(new Intent(Welcome.this,LoginRegisterDriver.class));
    }

    public void turnCustomerActivity(View view) {
        startActivity(new Intent(Welcome.this,LoginRegisterCustomer.class));

    }
}