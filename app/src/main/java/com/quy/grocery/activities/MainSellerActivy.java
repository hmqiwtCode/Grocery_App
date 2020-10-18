package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.quy.grocery.adapters.AdapterProductSeller;
import com.quy.grocery.Constants;
import com.quy.grocery.models.ModelProduct;
import com.quy.grocery.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivy extends AppCompatActivity {
    private TextView tvNameSeller, tvShopNameSeller, tvEmailSeller, tvTabProducts, tvTabOrders, tvShowAll;
    private ImageButton btnOut, btnEdtProfile, btnAddProduct;
    private EditText edtSearchProduct;
    private ImageButton btnFilterProduct;
    private RecyclerView rvProductSeller;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageView tvProfile;
    private RelativeLayout rlProducts,rlOrders;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller_activy);
        tvNameSeller = findViewById(R.id.tvNameSeller);
        btnOut = findViewById(R.id.btnSignOut);
        btnEdtProfile = findViewById(R.id.btnEdtProfile);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnFilterProduct = findViewById(R.id.btnFilterProduct);

        tvShopNameSeller = findViewById(R.id.tvShopNameSeller);
        tvEmailSeller = findViewById(R.id.tvEmailSeller);
        tvProfile = findViewById(R.id.tvProfile);
        tvTabProducts = findViewById(R.id.tvTabProducts);
        tvTabOrders = findViewById(R.id.tvTabOrders);
        tvShowAll = findViewById(R.id.tvShowAll);

        rlProducts = findViewById(R.id.rlProducts);
        rlOrders = findViewById(R.id.rlOrders);

        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        rvProductSeller = findViewById(R.id.rvProductSeller);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();

        showProductsUI();

        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOffline();
            }
        });
        btnEdtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivy.this,ProfileEditSellerActivity.class));
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivy.this,AddProductActivity.class));
            }
        });

        tvTabProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductsUI();
            }
        });

        tvTabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });

        btnFilterProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainSellerActivy.this)
                        .setTitle("Choose Category")
                        .setItems(Constants.options1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.options1[i];
                                tvShowAll.setText(selected);
                                if(selected.equals("All")){
                                    loadAllProducts();
                                }else{
                                    loadFilteredProducts(selected);
                                }

                            }
                        }).show();
            }
        });

        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductSeller.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (selected.equals(ds.child("productCategory").getValue()+"")){
                                ModelProduct md = ds.getValue(ModelProduct.class);
                                productList.add(md);
                            }
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivy.this,productList);
                        rvProductSeller.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct md = ds.getValue(ModelProduct.class);
                            productList.add(md);
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivy.this,productList);
                        rvProductSeller.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        rlProducts.setVisibility(View.VISIBLE);
        rlOrders.setVisibility(View.GONE);

        tvTabProducts.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabProducts.setBackgroundResource(R.drawable.shape_rect04);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        rlProducts.setVisibility(View.GONE);
        rlOrders.setVisibility(View.VISIBLE);

        tvTabProducts.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabProducts.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                    String shopName = ds.child("shop").getValue()+"";
                    String url = ds.child("profileImage").getValue()+"";
                    tvNameSeller.setText(name);
                    tvEmailSeller.setText(email);
                    tvShopNameSeller.setText(shopName);

                    try {
                        Picasso.get().load(url).placeholder(R.drawable.ic_store_white).into(tvProfile);
                    }catch (Exception e){
                        tvProfile.setImageResource(R.drawable.ic_store_white);
                    }
                }
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
                        Toast.makeText(MainSellerActivy.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}