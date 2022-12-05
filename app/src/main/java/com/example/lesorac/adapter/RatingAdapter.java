package com.example.lesorac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lesorac.R;
import com.example.lesorac.model.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private ArrayList<Rating> mRatingList = new ArrayList<>();
    private Query mQuery;

    public RatingAdapter(Query query) {
        mQuery = query;
        readData(query,new FirebaseCallback() {
            @Override
            public void onResponse(QueryDocumentSnapshot value) {
                mRatingList.add(value.toObject(Rating.class));
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mRatingList.get(position));
    }

    @Override
    public int getItemCount() {
        return mRatingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        MaterialRatingBar ratingBar;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.rating_item_name);
            ratingBar = itemView.findViewById(R.id.rating_item_rating);
            textView = itemView.findViewById(R.id.rating_item_text);
        }

        public void bind(Rating rating) {
            nameView.setText(rating.getUserName());
            ratingBar.setRating((float) rating.getRating());
            textView.setText(rating.getText());
        }
    }


    private void readData(Query query, FirebaseCallback callback) {
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

    public void updateData(){
        mRatingList.clear();
        readData(mQuery,new FirebaseCallback() {
            @Override
            public void onResponse(QueryDocumentSnapshot value) {
                mRatingList.add(value.toObject(Rating.class));
            }
        });
        notifyDataSetChanged();
    }

    private interface FirebaseCallback {
        void onResponse(QueryDocumentSnapshot snapshots);
    }
}
