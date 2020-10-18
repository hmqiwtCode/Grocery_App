package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.grocery.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail,edtPassword;
    private TextView tvFortgot,tvNoAccount;
    private Button btnLogin;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.emailEt);
        edtPassword = findViewById(R.id.passwordEt);
        tvFortgot = findViewById(R.id.forgotTv);
        tvNoAccount = findViewById(R.id.noAccountTv);
        btnLogin = findViewById(R.id.loginBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);



        tvFortgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        tvNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

    }
    private String strEmail,strPassword;
    private void loginUser(){
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(strPassword)){
            Toast.makeText(this, "Password isn't empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(strEmail,strPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                makeOnline();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 progressDialog.dismiss();
                 Toast.makeText(LoginActivity.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
              }
         });
    }

    private void makeOnline(){
        progressDialog.setMessage("Checking user...");
        progressDialog.show();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        //update value db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                checkUserType();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkUserType() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String accountType = ds.child("accountType").getValue() + "";
                    if (accountType.equals("Seller")){
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this,MainSellerActivy.class));
                        finish();
                    }else{
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this,MainUserActivy.class));
                        finish();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}