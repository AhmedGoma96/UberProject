package com.example.uberproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginRegisterDriver extends AppCompatActivity
{
    TextView driver_status,driver_link;
    Button driver_login,driver_register;
    EditText email_driver,password_driver;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference onLineDriver;
    String driverId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_driver);
        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        driver_status=findViewById(R.id.driver_status);
        driver_link=findViewById(R.id.dont_have_account_driver);
        driver_login=findViewById(R.id.login_driver);
        driver_register=findViewById(R.id.register_driver);
        password_driver=findViewById(R.id.password_driver);
        email_driver=findViewById(R.id.email_driver);
        driver_register.setVisibility(View.INVISIBLE);
        driver_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driver_status.setText("Driver Register");
                driver_login.setVisibility(View.INVISIBLE);
                driver_link.setVisibility(View.INVISIBLE);
                driver_register.setVisibility(View.VISIBLE);

            }
        });
         driver_register.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String email=email_driver.getText().toString();
                 String password=password_driver.getText().toString();
                 regeristation(email,password);
             }
         });
           driver_login.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   String email=email_driver.getText().toString();
                   String password=password_driver.getText().toString();
                   signInDriver(email,password);
               }
           });


    }

    private void signInDriver(String email, String password) {
        if(email.trim().isEmpty()){
            Toast.makeText(LoginRegisterDriver.this,"Please enter email..",Toast.LENGTH_LONG).show();
        }
        if(password.trim().isEmpty()){
            Toast.makeText(LoginRegisterDriver.this,"Please enter password..",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Driver Login");
            progressDialog.setMessage("please wait , check your Data");
            progressDialog.show();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        driverId=auth.getCurrentUser().getUid();

                        onLineDriver= Dao.saveDriver().child(driverId);
                        onLineDriver.setValue(true);
                        Toast.makeText(LoginRegisterDriver.this,"Complete Login",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginRegisterDriver.this,DriversMapsActivity.class));
                    }
                    else{
                        Toast.makeText(LoginRegisterDriver.this,"error happen during Login",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void regeristation(String email, String password) {
        if(email.trim().isEmpty()){
            Toast.makeText(LoginRegisterDriver.this,"Please enter email..",Toast.LENGTH_LONG).show();
        }
        if(password.trim().isEmpty()){
            Toast.makeText(LoginRegisterDriver.this,"Please enter password..",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Driver Regeristation");
            progressDialog.setMessage("please wait , Register your Data");
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                       driverId=auth.getCurrentUser().getUid();

                        onLineDriver= Dao.saveDriver().child(driverId);
                       onLineDriver.setValue(true);
                        Toast.makeText(LoginRegisterDriver.this,"Complete Regeristation",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginRegisterDriver.this,DriversMapsActivity.class));

                    }
                    else{
                        Toast.makeText(LoginRegisterDriver.this,"error happen during regeristation",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
}