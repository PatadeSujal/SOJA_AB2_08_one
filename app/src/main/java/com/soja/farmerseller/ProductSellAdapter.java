package com.soja.farmerseller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductSellAdapter extends RecyclerView.Adapter<ProductSellAdapter.SellProductsViewHolder>{
    public ArrayList<ProductSellsManager> productSellsManagerArrayList;
    Context context;
    public ProductSellAdapter(ArrayList<ProductSellsManager> productSellsManagerArrayList,Context context){
        this.productSellsManagerArrayList = productSellsManagerArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SellProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_portfolio,parent,false);
        return new SellProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellProductsViewHolder holder, int position) {
        ProductSellsManager productSellsManager = productSellsManagerArrayList.get(position);
        holder.productName.setText(productSellsManager.getProductName());
        holder.productSells.setText("Total Sells : " + productSellsManager.getProductSells());
        Picasso.get().load(productSellsManager.getImageUrl()).into(holder.productImage);

        holder.productEarnings.setText("Total Earnings :" +productSellsManager.getProductEarnings());


    }

    @Override
    public int getItemCount() {
        return productSellsManagerArrayList.size();
    }

    public static class SellProductsViewHolder extends RecyclerView.ViewHolder {
        TextView productName,productSells,productEarnings;
        ImageView productImage;
        public SellProductsViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productSells = itemView.findViewById(R.id.product_sells);
            productEarnings = itemView.findViewById(R.id.product_earnings);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }
}
