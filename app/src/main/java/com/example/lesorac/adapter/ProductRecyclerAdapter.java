package com.example.lesorac.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.activity.ProductDetailActivity;
import com.example.lesorac.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder> {

    private ArrayList<Product> mProductList;
    String from;
    private Query mQuery;

    public ProductRecyclerAdapter(Query query,String from){
        this.mQuery = query;
        this.from = from;
        mProductList = new ArrayList<>();
        readData(query,new FirebaseCallback() {
            @Override
            public void onResponse(QueryDocumentSnapshot value) {
                Product product = value.toObject(Product.class);
                product.setProduct_id(value.getId());
                mProductList.add(product);
            }
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(mProductList.get(position));
    }

    @Override
    public int getItemCount() {
        if(from.equals("profile"))
            return mProductList.size();
        else
            return mProductList.size() > 14 ? 14 : mProductList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mTextViewName,mTextViewPrice;
        public ImageView mImageView;
        public ImageView mSoldImageView;

        public ProductViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mImageView = itemView.findViewById(R.id.grid_view_image_view);
            mSoldImageView = itemView.findViewById(R.id.grid_view_image_view_sold);
            mTextViewName = itemView.findViewById(R.id.grid_view_product_name);
            mTextViewPrice = itemView.findViewById(R.id.grid_view_product_price);
        }


        public void bind(Product product) {
            Glide
                .with(mImageView.getContext())
                .load(product.getProduct_img_url().get(0))
                .into(mImageView);

            mTextViewName.setText(product.getProduct_name());
            mTextViewPrice.setText("RM" + Double.toString(product.getProduct_price()));

            if(!product.getProduct_status().equals("Selling")) mSoldImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
            intent.putExtra("Product",mProductList.get(getAdapterPosition()));
            if(from.equals("profile")){
                intent.putExtra("from","profile");
            }
            view.getContext().startActivity(intent);
        }

    }

    public void setQuery(Query query){
        mProductList.clear();
        notifyDataSetChanged();

        readData(query,new FirebaseCallback() {
            @Override
            public void onResponse(QueryDocumentSnapshot value) {
                Product product = value.toObject(Product.class);
                product.setProduct_id(value.getId());
                mProductList.add(product);
            }
        });


    }


    private void readData(Query query,FirebaseCallback callback) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        callback.onResponse(document);
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void refreshData(){
        mProductList.clear();
        readData(mQuery,new FirebaseCallback() {
            @Override
            public void onResponse(QueryDocumentSnapshot value) {
                Product product = value.toObject(Product.class);
                product.setProduct_id(value.getId());
                mProductList.add(product);
            }
        });
    }


    private interface FirebaseCallback {
        void onResponse(QueryDocumentSnapshot snapshots);
    }

}

