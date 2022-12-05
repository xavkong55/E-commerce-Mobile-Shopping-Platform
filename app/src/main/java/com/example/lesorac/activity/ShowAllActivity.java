package com.example.lesorac.activity;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.lesorac.R;
import com.example.lesorac.adapter.ProductRecyclerAdapter;
import com.example.lesorac.fragments.FilterDialogFragment;
import com.example.lesorac.model.Filters;
import com.example.lesorac.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShowAllActivity extends BaseActivity implements FilterDialogFragment.FilterListener {

    private FirebaseAuth mFireAuth;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private CardView mFilterBar;
    private ImageView btn_clear_filter;
    private RecyclerView mRecyclerView;
    private ProductRecyclerAdapter mProductRecyclerAdapter;
    private String category = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all);

        mRecyclerView = findViewById(R.id.show_all_activity_recycler_view);
        mFilterBar = findViewById(R.id.filter_bar);
        mFilterDialog = new FilterDialogFragment();
        btn_clear_filter = findViewById(R.id.btn_clear_filter_show_all_activity);

        mFireAuth = FirebaseUtil.getAuth();
        mFirestore = FirebaseUtil.getFirestore();


        if(getIntent().getStringExtra("Category") != null){
            category = getIntent().getStringExtra("Category");
            mQuery = mFirestore.collection("products")
                    .whereEqualTo("product_category",category)
                    .whereEqualTo("product_status","Selling");
        }else{
            mQuery = mFirestore.collection("products")
                    .whereEqualTo("product_status","Selling");
        }


        mFilterBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterDialog.show(getSupportFragmentManager(), "FilterDialog");
            }
        });

        btn_clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterDialog.resetFilters();
                onFilter(Filters.getDefault());
            }
        });

        initRecyclerView();
    }

    private void initRecyclerView(){

        mProductRecyclerAdapter = new ProductRecyclerAdapter(mQuery, "homepage");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(ShowAllActivity.this, 2,
                GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mProductRecyclerAdapter);
    }

    @Override
    public void onFilter(Filters filters) {
        Query query = mFirestore.collection("products").whereEqualTo("product_status","Selling");

        if(filters.hasCondition()){
            query = query.whereEqualTo("product_condition",filters.getCondition());
        }

        if(filters.hasMinPrice()){
            query = query.whereGreaterThanOrEqualTo("product_price",filters.getMinPrice());
        }

        if(filters.hasMaxPrice()){
            query = query.whereLessThanOrEqualTo("product_price",filters.getMaxPrice());
        }

        if(filters.hasDealMethod()){
            query = query.whereArrayContainsAny("product_deal_method",filters.getDealMethod());
        }

        if(category != null){
            query = query.whereEqualTo("product_category",category);
        }

        mQuery = query;
        mProductRecyclerAdapter.setQuery(mQuery);
    }

}