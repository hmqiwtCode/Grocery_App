package com.quy.grocery.adapters;

import android.util.Log;
import android.widget.Filter;

import com.quy.grocery.adapters.AdapterProductSeller;
import com.quy.grocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProductSeller adapterProductSeller;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductSeller adapterProductSeller, ArrayList<ModelProduct> modelProducts){
        this.adapterProductSeller = adapterProductSeller;
        filterList = modelProducts;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        Log.d("WHEN","charSequence");
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase(); // change to upper case, to make case insensitive
            ArrayList<ModelProduct> modelProducts = new ArrayList<>();
            for (int i = 0; i < filterList.size();i++){
                if (filterList.get(i).getProductTitle().toUpperCase().contains(charSequence) || filterList.get(i).getProductCategory().toUpperCase().contains(charSequence)){
                    modelProducts.add(filterList.get(i));
                }
            }
            results.count = modelProducts.size();
            results.values = modelProducts;

        }else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterProductSeller.arrModelProduct = (ArrayList<ModelProduct>) filterResults.values;
        adapterProductSeller.notifyDataSetChanged();

    }
}
