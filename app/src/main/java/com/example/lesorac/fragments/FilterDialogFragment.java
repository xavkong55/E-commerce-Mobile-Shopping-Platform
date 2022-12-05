package com.example.lesorac.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.lesorac.R;
import com.example.lesorac.model.Filters;

import java.util.ArrayList;

public class FilterDialogFragment extends DialogFragment implements View.OnClickListener{

    public interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    private Spinner mConditionSpinner;
    private CheckBox mCheckboxMeet, mCheckboxDelivery;
    private EditText mPriceMin, mPriceMax;

    private FilterListener mFilterListener;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_filter_dialog, container, false);
        mConditionSpinner = mRootView.findViewById(R.id.spinner_show_all_condition);
        mCheckboxMeet = mRootView.findViewById(R.id.cb_show_all_meet);
        mCheckboxDelivery = mRootView.findViewById(R.id.cb_show_all_delivery);
        mPriceMin = mRootView.findViewById(R.id.et_show_all_price_minimum);
        mPriceMax = mRootView.findViewById(R.id.et_show_all_price_maximum);

        mRootView.findViewById(R.id.btn_apply_show_all_activity).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_cancel_show_all_activity).setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_apply_show_all_activity:
                onSearchClicked();
                break;
            case R.id.btn_cancel_show_all_activity:
                onCancelClicked();
                break;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof FilterListener){
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }
        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @NonNull
    private String getSelectedCondition(){
        String selected = mConditionSpinner.getSelectedItem().toString();
        return selected;
    }

    @NonNull
    private ArrayList<String> getSelectedDealMethod(){
        ArrayList<String> dealMethod = new ArrayList<>();
        if(mCheckboxDelivery.isChecked())  dealMethod.add(mCheckboxDelivery.getText().toString());
        if(mCheckboxMeet.isChecked()) dealMethod.add(mCheckboxMeet.getText().toString());

        return dealMethod;
    }

    private double getSelectedMinPrice(){
        String minPrice = mPriceMin.getText().toString();
        if(minPrice.equals(""))
            return -1;
        else return Double.parseDouble(minPrice);
    }

    private double getSelectedMaxPrice(){
        String maxPrice = mPriceMax.getText().toString();
        if(maxPrice.equals(""))
            return -1;
        else return Double.parseDouble(maxPrice);
    }

    public Filters getFilters(){
        Filters filters = new Filters();
        if(mRootView != null){
            filters.setCondition(getSelectedCondition());
            filters.setMinPrice(getSelectedMinPrice());
            filters.setMaxPrice(getSelectedMaxPrice());
            filters.setDealMethod(getSelectedDealMethod());
        }
        return filters;
    }


    public void resetFilters(){
        if(mRootView != null){
            mConditionSpinner.setSelection(0);
            mPriceMin.setText("");
            mPriceMax.setText("");
            mCheckboxMeet.setChecked(false);
            mCheckboxDelivery.setChecked(false);
        }
    }
}