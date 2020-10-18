package com.quy.grocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.grocery.R;
import com.quy.grocery.activities.EditProductActivity;
import com.quy.grocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    Context context;
    ArrayList<ModelProduct> arrModelProduct, filterList;
    FilterProduct filter;
    public AdapterProductSeller(Context context, ArrayList<ModelProduct> arrModelProduct){
        this.context = context;
        this.arrModelProduct = arrModelProduct;
        this.filterList = arrModelProduct;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        final ModelProduct modelProduct = arrModelProduct.get(position);
        try{
            Picasso.get().load(modelProduct.getProductIcon()).placeholder(R.drawable.ic_shopping_primary).into(holder.ivproductIcon);
        }
        catch (Exception e){
            holder.ivproductIcon.setImageResource(R.drawable.ic_shopping_primary);
        }

        holder.tvOriginal.setText(modelProduct.getOriginalPrice());
        holder.tvPercentOff.setText(modelProduct.getDiscountNote());
        holder.tvTitle.setText(modelProduct.getProductTitle());
        holder.tvQuantity.setText(modelProduct.getProductQuantity());
        holder.tvPriceDiscounted.setText(modelProduct.getDiscountPrice());

        if(modelProduct.getDiscountAvailable().equals("true")){
            holder.tvPercentOff.setVisibility(View.VISIBLE);
            holder.tvPriceDiscounted.setVisibility(View.VISIBLE);
            holder.tvOriginal.setPaintFlags(holder.tvOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            holder.tvPercentOff.setVisibility(View.GONE);
            holder.tvPriceDiscounted.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Handle item click
                detaisBottomSheet(modelProduct);

            }
        });

    }

    private void detaisBottomSheet(final ModelProduct modelProduct){
        Log.e("ERROR",modelProduct.getProductId());
        final BottomSheetDialog sheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        sheetDialog.setContentView(view);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnDelete = view.findViewById(R.id.btnDelete);
        ImageButton btnEdit = view.findViewById(R.id.btnEdit);
        ImageView ivProductIcon = view.findViewById(R.id.ivProductIcon);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDiscountNote = view.findViewById(R.id.tvDiscountNote);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvCategory = view.findViewById(R.id.tvCategory);
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        TextView tvDiscountPrice = view.findViewById(R.id.tvDiscountPrice);
        TextView tvPrice = view.findViewById(R.id.tvPrice);

        tvTitle.setText(modelProduct.getProductTitle());
        tvQuantity.setText(modelProduct.getProductQuantity());
        tvPrice.setText(modelProduct.getOriginalPrice());
        tvDiscountNote.setText(modelProduct.getDiscountNote());
        tvCategory.setText(modelProduct.getProductCategory());
        tvDiscountPrice.setText(modelProduct.getDiscountPrice());

        if(modelProduct.getDiscountAvailable().equals("true")){
            tvDiscountPrice.setVisibility(View.VISIBLE);
            tvDiscountNote.setVisibility(View.VISIBLE);
            tvPrice.setPaintFlags(tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            tvDiscountPrice.setVisibility(View.GONE);
            tvDiscountNote.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(modelProduct.getProductIcon()).placeholder(R.drawable.ic_shopping_primary).into(ivProductIcon);
        }
        catch (Exception e){
            ivProductIcon.setImageResource(R.drawable.ic_shopping_primary);
        }

        sheetDialog.show();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetDialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show confirm dialog;
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete product " + modelProduct.getProductTitle() + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteProduct(modelProduct.getProductId());
                                sheetDialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sheetDialog.dismiss();
                            }
                        }).show();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", modelProduct.getProductId());
                context.startActivity(intent);
            }
        });
    }

    private void deleteProduct(String productId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(productId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            });
    }

    @Override
    public int getItemCount() {
        return arrModelProduct.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProduct(this,arrModelProduct);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        private ImageView ivproductIcon;
        private TextView tvPercentOff,tvTitle,tvQuantity,tvPriceDiscounted,tvOriginal;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            ivproductIcon = itemView.findViewById(R.id.ivproductIcon);
            tvPercentOff = itemView.findViewById(R.id.tvPercentOff);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPriceDiscounted = itemView.findViewById(R.id.tvPriceDiscounted);
            tvOriginal = itemView.findViewById(R.id.tvOriginal);
;
        }

    }


}
