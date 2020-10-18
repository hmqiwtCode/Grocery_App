package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quy.grocery.R;

import java.util.HashMap;
import java.util.List;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton btnBack, btnGPS;
    private ImageView tvProfile;
    private EditText edtName, edtPhone, edtCountry, edtState, edtCity, edtAddress, edtEmail, edtPassword, edtCpassword, edtShopName, edtDeliveryFee;
    private Button btnRegister;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri uri;

    private LocationManager locationManager;
    private double latitude = 0.0, longtitude = 0.0;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        btnBack = findViewById(R.id.btnBack);
        btnGPS = findViewById(R.id.btnGps);
        btnRegister = findViewById(R.id.btnRegister);
        tvProfile = findViewById(R.id.tvProfile);
        edtName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtCountry = findViewById(R.id.edtCountry);
        edtState = findViewById(R.id.edtState);
        edtCity = findViewById(R.id.edtCity);
        edtAddress = findViewById(R.id.edtAddress);
        edtEmail = findViewById(R.id.emailEt);
        edtPassword = findViewById(R.id.passwordEt);
        edtCpassword = findViewById(R.id.cPasswordEt);
        edtShopName = findViewById(R.id.edtShopName);
        edtDeliveryFee = findViewById(R.id.edtDelivery);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);



        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    Log.d("Crash at here", "Not ok");
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });
        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });


    }

    private String strFullName, strShopName, strPhoneNumber, strDeliveryFee, strCountry, strState, strCity, strAddress, strEmail, strPassword, strCPassword;
    private void inputData() {
        strFullName = edtName.getText().toString();
        strShopName = edtShopName.getText().toString();
        strPhoneNumber = edtPhone.getText().toString();
        strDeliveryFee = edtDeliveryFee.getText().toString();
        strCountry = edtCountry.getText().toString();
        strState = edtState.getText().toString();
        strCity = edtCity.getText().toString();
        strAddress = edtAddress.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strCPassword = edtCpassword.getText().toString();

        if(TextUtils.isEmpty(strFullName)){
            Toast.makeText(this, "Please enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(strShopName)){
            Toast.makeText(this, "Please enter Shop Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(strPhoneNumber)){
            Toast.makeText(this, "Please enter Phone...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(strDeliveryFee)){
            Toast.makeText(this, "Please enter Delivery...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(longtitude == 0 || latitude == 0){
            Toast.makeText(this, "Please click GPS button...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(strPassword.length() < 6){
            Toast.makeText(this, "Length > 6", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!strCPassword.equals(strPassword)){
            Toast.makeText(this, "Not match..", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount(){
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(strEmail,strPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                saverFirebaseData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterSellerActivity.this, "Can't save" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");
        final String timestamp = System.currentTimeMillis()+"";
        if(uri == null){
            // save without image
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uid",firebaseAuth.getUid());
            hashMap.put("email",strEmail);
            hashMap.put("name",strFullName);
            hashMap.put("shop",strShopName);
            hashMap.put("phone",strPhoneNumber);
            hashMap.put("deliveryFee",strDeliveryFee);
            hashMap.put("country",strCountry);
            hashMap.put("state",strState);
            hashMap.put("city",strCity);
            hashMap.put("address",strAddress);
            hashMap.put("latitude",latitude+"");
            hashMap.put("longitude",longtitude+"");
            hashMap.put("timestamp",timestamp);
            hashMap.put("accountType","Seller");
            hashMap.put("online","true");
            hashMap.put("shopOpen","true");
            hashMap.put("profileImage","");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)   //ref.child(firebaseAuth.getUid()) Get a reference to location relative to this one ->Return  A new DatabaseReference to the given path
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivy.class));
                        finish();
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivy.class));
                            finish();
                        }
                    });
        }
        else{
            String filePathAndName = "profile_images/" + firebaseAuth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                storageReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url image upload;
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("uid",firebaseAuth.getUid());
                                hashMap.put("email",strEmail);
                                hashMap.put("name",strFullName);
                                hashMap.put("shop",strShopName);
                                hashMap.put("phone",strPhoneNumber);
                                hashMap.put("deliveryFee",strDeliveryFee);
                                hashMap.put("country",strCountry);
                                hashMap.put("state",strState);
                                hashMap.put("city",strCity);
                                hashMap.put("address",strAddress);
                                hashMap.put("latitude",latitude+"");
                                hashMap.put("longitude",longtitude+"");
                                hashMap.put("timestamp",timestamp);
                                hashMap.put("accountType","Seller");
                                hashMap.put("online","true");
                                hashMap.put("shopOpen","true");
                                hashMap.put("profileImage",downloadImageUri+"");
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)   //ref.child(firebaseAuth.getUid()) Get a reference to location relative to this one ->Return  A new DatabaseReference to the given path
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivy.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivy.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void findAddress() {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;
        try {
            listAddress = geocoder.getFromLocation(latitude, longtitude, 1);

            String address = listAddress.get(0).getAddressLine(0); // complete address
            String city = listAddress.get(0).getLocality();
            String state = listAddress.get(0).getAdminArea();
            String country = listAddress.get(0).getCountryName();

            edtAddress.setText(address);
            edtCity.setText(city);
            edtState.setText(state);
            edtCountry.setText(country);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this).setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            //Camera Pick
                            if(checkCameraPermission()){
                                pickFromCamera();
                            }else {
                                requestCameraPermission();
                            }
                        } else {
                            //Gallery pick
                            if(checkStoragePermission()){
                                pickFromGallery();
                            }else{
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        detectLocation();
                    } else {

                    }
                }
            }
            break;

            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {

                    }
                }
            }
            break;


            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {

                    }
                }
            }
            break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longtitude = location.getLongitude();
        findAddress();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Please enable location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        Log.d("Loi gi day","HUHU");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            uri = data.getData();
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                tvProfile.setImageBitmap(imageBitmap);

            }
            else if (requestCode == IMAGE_PICK_GALLERY_CODE){
                System.out.println("Vo day chua ta_1");
                Log.i("LINK",uri.toString());
                tvProfile.setImageURI(uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}