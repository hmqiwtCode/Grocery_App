package com.quy.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quy.grocery.R;
import com.quy.grocery.activities.ShopDetailsActivity;
import com.quy.grocery.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdapterShopUser extends RecyclerView.Adapter<AdapterShopUser.HolderShop> {

    Context context;
    ArrayList<ModelShop> arrModelShop;

    public AdapterShopUser(Context context, ArrayList<ModelShop> arrModelShop) {
        this.context = context;
        this.arrModelShop = arrModelShop;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        ModelShop modelShop = arrModelShop.get(position);
        final String uid = modelShop.getUid();
        String email = modelShop.getEmail();
        String name = modelShop.getName();
        String shop = modelShop.getShop();
        String phone = modelShop.getPhone();
        String address = modelShop.getAddress();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String shopOpen = modelShop.getShopOpen();
        String urlProfile = modelShop.getProfileImage();
        holder.tvShopName.setText(shop);
        holder.tvPhone.setText(phone);
        holder.tvAddress.setText(address);
        if(shopOpen.equals("true")){
            holder.tvOpen.setVisibility(View.GONE);
        }else{
            holder.tvOpen.setVisibility(View.VISIBLE);
        }

        if(online.equals("true")){
            holder.ivOnline.setVisibility(View.VISIBLE);
        }else{
            holder.ivOnline.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(urlProfile).placeholder(R.drawable.ic_store_white).into(holder.tvProfile);
        }catch(Exception e){
            holder.tvProfile.setImageResource(R.drawable.ic_store_white);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUID",uid);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrModelShop.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{

        private ImageView tvProfile, ivOnline,ivNext;
        private TextView tvOpen, tvShopName, tvPhone, tvAddress;
        private RatingBar ratingBar;


        public HolderShop(@NonNull View itemView) {
            super(itemView);
            tvProfile = itemView.findViewById(R.id.tvProfile);
            ivOnline = itemView.findViewById(R.id.ivOnline);
            ivNext = itemView.findViewById(R.id.ivNext);
            tvOpen = itemView.findViewById(R.id.tvOpen);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }


    }
}
