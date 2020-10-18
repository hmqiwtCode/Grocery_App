package com.quy.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quy.grocery.Constants;
import com.quy.grocery.R;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgProfile;
    private EditText edtTitle, edtDescription, edtQuantity,edtPrice,edtDiscountPrice, edtDiscountNote;
    private TextView tvCategory;
    private SwitchCompat switchDiscount;
    private Button btnAddProduct;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.tvProfile);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtDiscountPrice = findViewById(R.id.edtDiscountPrice);
        edtPrice = findViewById(R.id.edtPrice);
        edtDiscountNote = findViewById(R.id.edtDiscountNote);

        tvCategory = findViewById(R.id.tvCategory);

        switchDiscount = findViewById(R.id.switchDiscount);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        cameraPermission = new String[]{Manifest.permission.CAMERA};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        edtDiscountPrice.setVisibility(View.GONE);
        edtDiscountNote.setVisibility(View.GONE);

        switchDiscount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    edtDiscountPrice.setVisibility(View.VISIBLE);
                    edtDiscountNote.setVisibility(View.VISIBLE);
                }else{
                    edtDiscountPrice.setVisibility(View.GONE);
                    edtDiscountNote.setVisibility(View.GONE);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AddProductActivity.this).setTitle("Choose ...")
                        .setItems(Constants.options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tvCategory.setText(Constants.options[i]);
                            }
                        }).show();
            }
        });




    }


    private String strTitle, strDes, strCategories, strQuantity, strPrice, strPercentDiscount, strNoteDiscount;
    private boolean discountAvailable = false;

    private void inputData() {
        strTitle = edtTitle.getText().toString();
        strDes = edtDescription.getText().toString();
        strCategories = tvCategory.getText().toString();
        strQuantity = edtQuantity.getText().toString();
        strPrice = edtPrice.getText().toString();
        discountAvailable = switchDiscount.isChecked();


        if (TextUtils.isEmpty(strTitle)) {
            Toast.makeText(this, "Please enter Title...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strDes)) {
            Toast.makeText(this, "Please enter Description...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strCategories)) {
            Toast.makeText(this, "Please enter Category...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strQuantity) || Integer.valueOf(strQuantity) <= 0) {
            Toast.makeText(this, "Please enter Quantity...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(strPrice)) {
            Toast.makeText(this, "Please enter Price...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountAvailable){
            strPercentDiscount = edtDiscountPrice.getText().toString();
            strNoteDiscount = edtDiscountNote.getText().toString();
            if (TextUtils.isEmpty(strPercentDiscount)) {
                Toast.makeText(this, "Please enter Discount price...", Toast.LENGTH_SHORT).show();
                return;
            }
        }else {
            strPercentDiscount = "0";
            strNoteDiscount = "";
        }

        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Adding product...");
        progressDialog.show();
        saverFirebaseData();
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving product...");
        final String timestamp = System.currentTimeMillis() + "";
        if (uri == null) {
            // save without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", timestamp);
            hashMap.put("productTitle", strTitle);
            hashMap.put("productDescription", strDes);
            hashMap.put("productCategory", strCategories);
            hashMap.put("productQuantity", strQuantity);
            hashMap.put("productIcon", "");
            hashMap.put("originalPrice", strPrice);
            hashMap.put("discountPrice", strPercentDiscount);
            hashMap.put("discountNote", strNoteDiscount);
            hashMap.put("discountAvailable", discountAvailable+"");
            hashMap.put("timestamp", timestamp);
            hashMap.put("uid", firebaseAuth.getUid());

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Product").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    clearData();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



        } else {
            String filePathAndName = "profile_images/" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url image upload;
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId", timestamp);
                                hashMap.put("productTitle", strTitle);
                                hashMap.put("productDescription", strDes);
                                hashMap.put("productCategory", strCategories);
                                hashMap.put("productQuantity", strQuantity);
                                hashMap.put("productIcon", ""+downloadImageUri);
                                hashMap.put("originalPrice", strPrice);
                                hashMap.put("discountPrice", strPercentDiscount);
                                hashMap.put("discountNote", strNoteDiscount);
                                hashMap.put("discountAvailable", discountAvailable+"");
                                hashMap.put("timestamp", timestamp);
                                hashMap.put("uid", firebaseAuth.getUid());
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                databaseReference.child(firebaseAuth.getUid()).child("Product").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        clearData();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void clearData(){
        edtTitle.setText("");
        uri = null;
        edtDescription.setText("");
        tvCategory.setText("");
        edtQuantity.setText("");
        edtPrice.setText("");
        edtDiscountNote.setText("");
        edtDiscountPrice.setText("");
        switchDiscount.setChecked(false);
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this).setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            //Camera Pick
                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            //Gallery pick
                            if (checkStoragePermission()) {
                                pickFromGallery();
                            } else {
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

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            uri = data.getData();
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgProfile.setImageBitmap(imageBitmap);

            } else if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                System.out.println("Vo day chua ta_1");
                Log.i("LINK", uri.toString());
                imgProfile.setImageURI(uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}