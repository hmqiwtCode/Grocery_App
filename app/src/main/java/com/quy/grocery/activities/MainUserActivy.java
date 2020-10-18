package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.grocery.R;
import com.quy.grocery.adapters.AdapterShopUser;
import com.quy.grocery.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivy extends AppCompatActivity {

    TextView tvNameUser, tvEmail, tvPhone, tvTabShops, tvTabOrders;
    ImageButton btnOut,btnEdtProfile;
    private ImageView tvProfile;
    private RelativeLayout rlShops, rlOrders;
    private RecyclerView rvShops;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> arrModelShop;
    private AdapterShopUser adapterShopUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_activy);
        tvNameUser = findViewById(R.id.tvNameUser);
        btnOut = findViewById(R.id.btnSignOut);
        btnEdtProfile = findViewById(R.id.btnEdtProfile);
        tvProfile = findViewById(R.id.tvProfile);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvTabShops = findViewById(R.id.tvTabShops);
        tvTabOrders = findViewById(R.id.tvTabOrders);
        rlShops = findViewById(R.id.rlShops);
        rlOrders = findViewById(R.id.rlOrders);
        rvShops = findViewById(R.id.rvShops);

        arrModelShop = new ArrayList<>();



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        showShopsUI();

        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOffline();
            }
        });
        btnEdtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivy.this,ProfileEditUserActivity.class));
            }
        });

        tvTabShops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShopsUI();
            }
        });
        tvTabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });
    }

    private void showShopsUI() {
        rlShops.setVisibility(View.VISIBLE);
        rlOrders.setVisibility(View.GONE);

        tvTabShops.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabShops.setBackgroundResource(R.drawable.shape_rect04);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        rlShops.setVisibility(View.GONE);
        rlOrders.setVisibility(View.VISIBLE);

        tvTabShops.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabShops.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorBlack) );
        tvTabOrders.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    public void loadMyInfo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String name = ds.child("name").getValue()+"";
                    String accountType = ds.child("accountType").getValue()+"";
                    String email = ds.child("email").getValue()+"";
                    String phone = ds.child("phone").getValue()+"";
                    String profileImage = ds.child("profileImage").getValue()+"";
                    String city = ds.child("city").getValue()+"";
                    tvNameUser.setText(name);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);
                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(tvProfile);
                    }catch(Exception e){
                        tvProfile.setImageResource(R.drawable.ic_person_gray);
                    }
                    loadShops(city);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String city) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrModelShop.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Log.e("ERROR", ds.child("accountType").getValue()+"");
                    ModelShop modelShop = ds.getValue(ModelShop.class);

                    if(modelShop.getCity().equalsIgnoreCase(city)){
                        arrModelShop.add(modelShop);
                    }
                }
                adapterShopUser = new AdapterShopUser(MainUserActivy.this,arrModelShop);
                rvShops.setAdapter(adapterShopUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void makeOffline(){
        progressDialog.setMessage("Logging Out...");
        progressDialog.show();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseAuth.signOut();
                checkUser();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivy.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}