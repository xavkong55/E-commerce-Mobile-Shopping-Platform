package com.example.lesorac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.model.ProductImage;

import java.util.ArrayList;


public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    private ArrayList<ProductImage> mImage;

    public ProductImageAdapter(ArrayList<ProductImage> imageList) {
        this.mImage = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View product_img = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_image, null);
        return new ViewHolder(product_img);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductImageAdapter.ViewHolder holder, int position) {
        holder.bind(mImage.get(position));
    }

    @Override
    public int getItemCount() {
        return mImage.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView productImage;
        private ImageButton btn_remove;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.sell_activity_product_image);
            btn_remove = itemView.findViewById(R.id.sell_activity_btn_remove);
            btn_remove.setOnClickListener(this);
        }

        public void bind(ProductImage image) {
            if (image.getUri() == null) {
                Glide
                    .with(productImage.getContext())
                    .load(image.getURL())
                    .into(productImage);
            }else{
                Glide.with(productImage.getContext())
                    .load(image.getUri())
                    .into(productImage);

            }
        }

        @Override
        public void onClick(View view) {
            mImage.remove(getAdapterPosition());
            notifyDataSetChanged();
        }
    }
}
