package com.example.uberproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginRegisterCustomer extends AppCompatActivity {
    TextView customer_status,customer_link;
    Button customer_login,customer_register;
    EditText email_customer,password_customer;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference onLineCustomer;
    String customerId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_customer);
        auth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        password_customer=findViewById(R.id.password_customer);
        email_customer=findViewById(R.id.email_customer);
        customer_status=findViewById(R.id.customer_status);
        customer_link=findViewById(R.id.dont_have_account_customer);
        customer_login=findViewById(R.id.login_customer);
        customer_register=findViewById(R.id.register_customer);
        customer_register.setVisibility(View.INVISIBLE);
        customer_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customer_status.setText("Customer Register");
                customer_login.setVisibility(View.INVISIBLE);
                customer_link.setVisibility(View.INVISIBLE);
                customer_register.setVisibility(View.VISIBLE);

            }
        });
        customer_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=email_customer.getText().toString();
                String password=password_customer.getText().toString();
                regeristation(email,password);
            }
        });
        customer_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=email_customer.getText().toString();
                String password=password_customer.getText().toString();
                signInCustomer(email,password);
            }
        });
    }

    private void signInCustomer(String email, String password) {
        if(email.trim().isEmpty()){
            Toast.makeText(LoginRegisterCustomer.this,"Please enter email..",Toast.LENGTH_LONG).show();
        }
        if(password.trim().isEmpty()){
            Toast.makeText(LoginRegisterCustomer.this,"Please enter password..",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Customer Login");
            progressDialog.setMessage("please wait , check your Data");
            progressDialog.show();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        customerId=auth.getCurrentUser().getUid();

                        onLineCustomer=Dao.saveCustomer().child(customerId);
                        onLineCustomer.setValue(true);

                        Toast.makeText(LoginRegisterCustomer.this,"Complete Login",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginRegisterCustomer.this,CustomerMapsActivity.class));

                    }
                    else{
                        Toast.makeText(LoginRegisterCustomer.this,"error happen during Login",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void regeristation(String email, String password) {
        if(email.trim().isEmpty()){
            Toast.makeText(LoginRegisterCustomer.this,"Please enter email..",Toast.LENGTH_LONG).show();
        }
        if(password.trim().isEmpty()){
            Toast.makeText(LoginRegisterCustomer.this,"Please enter password..",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Customer Regeristation");
            progressDialog.setMessage("please wait , Register your Data");
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        customerId=auth.getCurrentUser().getUid();

                       onLineCustomer=Dao.saveCustomer().child(customerId);
                        onLineCustomer.setValue(true);
                        Toast.makeText(LoginRegisterCustomer.this,"Complete Regeristation",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginRegisterCustomer.this,CustomerMapsActivity.class));

                    }
                    else{
                        Toast.makeText(LoginRegisterCustomer.this,"error happen during regeristation",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
}