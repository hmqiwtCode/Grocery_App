package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.grocery.Constants;
import com.quy.grocery.R;
import com.quy.grocery.adapters.AdapterProductUser;
import com.quy.grocery.models.ModelProduct;
import com.quy.grocery.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {


    private ImageView ivShop;
    private TextView tvShopName,tvPhone,tvEmail,tvOpen,tvDeliveryFree
            ,tvAddress,tvShowAll;
    private ImageButton btnCall,btnMap,btnBack,btnCart,btnFilterProduct;
    private EditText edtSearchProduct;
    private RecyclerView rvProductUser;

    private String shopUID;
    private String myLatitude, myLongitude;
    private String shopName, shopPhone, shopEmail, shopAddress ,shopLatitude, shopLongitude;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        ivShop = findViewById(R.id.ivShop);
        tvShopName = findViewById(R.id.tvShopName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvOpen = findViewById(R.id.tvOpen);
        tvDeliveryFree = findViewById(R.id.tvDeliveryFree);
        tvAddress = findViewById(R.id.tvAddress);
        tvShowAll = findViewById(R.id.tvShowAll);
        btnCall = findViewById(R.id.btnCall);
        btnMap = findViewById(R.id.btnMap);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        btnFilterProduct = findViewById(R.id.btnFilterProduct);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        rvProductUser = findViewById(R.id.rvProductUser);


        Intent intent = getIntent();
        shopUID = intent.getStringExtra("shopUID");
        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductUser.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnFilterProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ShopDetailsActivity.this)
                        .setTitle("Choose Category")
                        .setItems(Constants.options1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.options1[i];
                                tvShowAll.setText(selected);
                                if(selected.equals("All")){
                                    loadShopProducts();
                                }else{
                                    loadFilteredProducts(selected);
                                }

                            }
                        }).show();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+shopPhone));
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "http://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
                System.out.println(uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);


            }
        });


    }

    private void loadMyInfo(){
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
                    myLatitude = ds.child("latitude").getValue()+"";
                    myLongitude = ds.child("longitude").getValue()+"";


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void loadShopDetails(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(shopUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelShop modelShop = snapshot.getValue(ModelShop.class);
                tvShopName.setText(modelShop.getShop());
                tvPhone.setText(modelShop.getPhone());
                tvEmail.setText(modelShop.getEmail());
                tvAddress.setText(modelShop.getAddress());
                shopPhone = modelShop.getPhone();
                shopLatitude = modelShop.getLatitude();
                shopLongitude = modelShop.getLongitude();
                if(modelShop.getShopOpen().equalsIgnoreCase("true")){
                    tvOpen.setText("Open");
                }else{
                    tvOpen.setText("Closed");
                }

                try{
                    Picasso.get().load(modelShop.getProfileImage()).placeholder(R.drawable.ic_store_white).into(ivShop);
                }catch(Exception e){
                    ivShop.setImageResource(R.drawable.ic_store_white);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUID).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (selected.equalsIgnoreCase(ds.child("productCategory").getValue()+"")){
                                ModelProduct md = ds.getValue(ModelProduct.class);
                                productList.add(md);
                            }
                        }
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                        rvProductUser.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopProducts() {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Log.e("ERROR",shopUID);
        ref.child(shopUID).child("Product")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct md = ds.getValue(ModelProduct.class);
                            Log.e("ERROR",md.getProductId());
                            productList.add(md);
                        }
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                        rvProductUser.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}