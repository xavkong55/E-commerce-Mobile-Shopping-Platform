package com.example.lesorac.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.lesorac.R;
import com.example.lesorac.activity.ShowAllActivity;
import com.example.lesorac.adapter.ProductRecyclerAdapter;
import com.example.lesorac.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomepageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomepageFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private DocumentReference mUserRef;
    private LinearLayout category_1,category_2,category_3,category_4,category_5,category_6;
    private SwipeRefreshLayout swipeRefreshlayout;
    private TextView product_seeAll;
    private boolean onFirstRun;

    private RecyclerView mRecyclerView;
    private ProductRecyclerAdapter mProductRecyclerAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomepageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomepageFragment newInstance(String param1, String param2) {
        HomepageFragment fragment = new HomepageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(onFirstRun)
            mProductRecyclerAdapter.refreshData();
        else onFirstRun = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshlayout = view.findViewById(R.id.home_fragment_swipe_refresh_layout);
        category_1 = view.findViewById(R.id.home_fragment_category_1);
        category_2 = view.findViewById(R.id.home_fragment_category_2);
        category_3 = view.findViewById(R.id.home_fragment_category_3);
        category_4 = view.findViewById(R.id.home_fragment_category_4);
        category_5 = view.findViewById(R.id.home_fragment_category_5);
        category_6 = view.findViewById(R.id.home_fragment_category_6);
        product_seeAll = view.findViewById(R.id.home_fragment_product_see_all);

        category_1.setOnClickListener(this);
        category_2.setOnClickListener(this);
        category_3.setOnClickListener(this);
        category_4.setOnClickListener(this);
        category_5.setOnClickListener(this);
        category_6.setOnClickListener(this);
        product_seeAll.setOnClickListener(this);

        ImageSlider imgSlider = view.findViewById(R.id.homepage_fragment_image_slider);
        List<SlideModel> slideModelList = new ArrayList<>();

        slideModelList.add(new SlideModel(R.drawable.banner1, "70% OFF", ScaleTypes.CENTER_INSIDE));
        slideModelList.add(new SlideModel(R.drawable.banner2, "RM18 OFF For all Categories", ScaleTypes.CENTER_INSIDE));
        slideModelList.add(new SlideModel(R.drawable.banner3, "Happy New Year", ScaleTypes.CENTER_CROP));

        imgSlider.setImageList((slideModelList));


        mRecyclerView = view.findViewById(R.id.home_fragment_popular_product_recylcer_view);

        mFirestore = FirebaseUtil.getFirestore();

//        String userId = getActivity().getIntent().getExtras().getString("User_Id");

        Query query = mFirestore.collection("products").whereEqualTo("product_status","Selling");

        mProductRecyclerAdapter = new ProductRecyclerAdapter(query, "homepage");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mProductRecyclerAdapter);

        swipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mProductRecyclerAdapter.refreshData();
                swipeRefreshlayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getContext(),ShowAllActivity.class);
        switch (view.getId()) {
            case R.id.home_fragment_category_1:
                intent.putExtra("Category","Electronic");
                startActivity(intent);
                break;
            case R.id.home_fragment_category_2:
                intent.putExtra("Category","Food & Beverage");
                startActivity(intent);
                break;
            case R.id.home_fragment_category_3:
                intent.putExtra("Category","Clothes");
                startActivity(intent);
                break;
            case R.id.home_fragment_category_4:
                intent.putExtra("Category","Pet");
                startActivity(intent);
                break;
            case R.id.home_fragment_category_5:
                intent.putExtra("Category","Kitchen & Appliances");
                startActivity(intent);
                break;
            case R.id.home_fragment_category_6:
                intent.putExtra("Category","Other");
                startActivity(intent);
                break;
            case R.id.home_fragment_product_see_all:
                startActivity(intent);
                break;
        }
    }
}