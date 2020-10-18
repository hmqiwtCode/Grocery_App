package com.quy.grocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quy.grocery.R;
import com.quy.grocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> arrModelProducts, filterList;
    private FilterProductUser filterProductUser;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> arrModelProducts) {
        this.context = context;
        this.arrModelProducts = arrModelProducts;
        filterList = arrModelProducts;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        final ModelProduct product = arrModelProducts.get(position);
        holder.tvTitle.setText(product.getProductTitle());
        holder.tvDescription.setText(product.getProductDescription());
        holder.tvPercentOff.setText(product.getDiscountNote());
        holder.tvPriceDiscounted.setText(product.getDiscountPrice());
        holder.tvOriginal.setText(product.getOriginalPrice());


        if(product.getDiscountAvailable().equals("true")){
            holder.tvPriceDiscounted.setVisibility(View.VISIBLE);
            holder.tvPercentOff.setVisibility(View.VISIBLE);
            holder.tvOriginal.setPaintFlags(holder.tvOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            holder.tvPriceDiscounted.setVisibility(View.GONE);
            holder.tvPercentOff.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(product.getProductIcon()).placeholder(R.drawable.ic_shopping_primary).into(holder.ivproductIcon);
        }
        catch (Exception e){
            holder.ivproductIcon.setImageResource(R.drawable.ic_shopping_primary);
        }

        holder.tvAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(product);
            }
        });

    }

    private int quantity = 0;
    private int minQuantity = 1;
    private int maxQuantity = 0;
    private void showQuantityDialog(final ModelProduct product) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        ImageView tvIconProduct = view.findViewById(R.id.tvIconProduct);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        final TextView tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        final TextView tvQuantityReal = view.findViewById(R.id.tvQuantityReal);
        ImageView ivDecre = view.findViewById(R.id.ivDecre);
        ImageView ivIncrease = view.findViewById(R.id.ivIncrease);
        Button btnAddCart = view.findViewById(R.id.btnAddCart);

        tvTitle.setText(product.getProductTitle());
        tvDescription.setText(product.getProductDescription());
        tvQuantity.setText(product.getProductQuantity());
        tvTotalPrice.setText(product.getOriginalPrice());


        try{
            Picasso.get().load(product.getProductIcon()).placeholder(R.drawable.ic_shopping_primary).into(tvIconProduct);
        }
        catch (Exception e){
            tvIconProduct.setImageResource(R.drawable.ic_shopping_primary);
        }
        quantity = 1;
        maxQuantity = Integer.valueOf(product.getProductQuantity());
        tvQuantityReal.setText(quantity+"");
        builder.show();

        ivDecre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity--;
                if(quantity <= 1)
                    quantity = minQuantity;

                tvQuantityReal.setText(quantity+"");
                tvTotalPrice.setText(Double.valueOf(product.getOriginalPrice())*quantity+"");

            }
        });

        ivIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity++;
                if (quantity > maxQuantity)
                    quantity = maxQuantity;
                tvQuantityReal.setText(quantity+"");
                tvTotalPrice.setText(Double.valueOf(product.getOriginalPrice())*quantity+"");
            }
        });

        btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart(product.getProductId(),product.getProductTitle(),product.getOriginalPrice(),tvTotalPrice.getText().toString(),quantity);
            }
        });



    }

    private int itemSTT = 0;
    private void addToCart(String productId, String productTitle, String text, String originalPrice, int quantity) {
        itemSTT++;
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB") // "TEST" is the name of the DATABASE
                .setTableName("ITEMS")  // You can ignore this line if you want
                .addColumn(new Column("Item_ID", "text", "unique")) // Contrains like "text", "unique", "not null" are not case sensitive
                .addColumn(new Column("Item_PID", "text", "not null"))
                .addColumn(new Column("Item_Name", "text"))
                .addColumn(new Column("Item_Price_Each", "text"))
                .addColumn(new Column("Item_Price", "text"))
                .addColumn(new Column("Item_Quantity", "text"))
                .doneTableColumn();

        boolean done = easyDB.addData("Item_ID", itemSTT)
                .addData("Item_PID", productId)
                .addData("Item_Name", productTitle)
                .addData("Item_Price_Each", text)
                .addData("Item_Price", originalPrice)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();
        if (done){
            Toast.makeText(context,"Ok",Toast.LENGTH_LONG).show();
        }



    }

    @Override
    public int getItemCount() {
        return arrModelProducts.size();
    }

    @Override
    public Filter getFilter() {
        if(filterProductUser == null){
            filterProductUser = new FilterProductUser(this,filterList);
        }
        return filterProductUser;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {

        private ImageView ivproductIcon;
        private TextView tvPercentOff,tvTitle,
                tvPriceDiscounted,tvOriginal,tvDescription,tvAddToCart;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            ivproductIcon = itemView.findViewById(R.id.ivproductIcon);
            tvPercentOff = itemView.findViewById(R.id.tvPercentOff);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPriceDiscounted = itemView.findViewById(R.id.tvPriceDiscounted);
            tvOriginal = itemView.findViewById(R.id.tvOriginal);
            tvAddToCart = itemView.findViewById(R.id.tvAddToCart);

        }
    }
}
