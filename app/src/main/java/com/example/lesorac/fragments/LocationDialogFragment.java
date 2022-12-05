package com.example.lesorac.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lesorac.R;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationDialogFragment extends DialogFragment implements View.OnClickListener{


    public interface onInputListener{
        void sendInput(GeoPoint LatLng );
    }

    private onInputListener mInputListener;
    private EditText et_location;
    private CheckBox checkbox_meet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_address, container, false);

        et_location = view.findViewById(R.id.meetup_location_et_text);
        checkbox_meet = getActivity().findViewById(R.id.sell_activity_checkbox_meet);
        view.findViewById(R.id.meetup_form_btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.meetup_form_btn_cfm).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof onInputListener){
            mInputListener = (onInputListener) context;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.meetup_form_btn_cfm:
                if(TextUtils.isEmpty(et_location.getText())){
                    Toast.makeText(getActivity(), "Invalid Address", Toast.LENGTH_SHORT).show();
                    checkbox_meet.setChecked(false);
                    mInputListener.sendInput(new GeoPoint(0,0));
                }
                else{
                    mInputListener.sendInput(getLatLangFromAddress(et_location.getText().toString()));
                }
                dismiss();
                break;
            case R.id.meetup_form_btn_cancel:
                checkbox_meet.setChecked(false);
                dismiss();
                break;
        }
    }

    private GeoPoint getLatLangFromAddress(String addr){
        Geocoder coder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> address;
        try {
            address = coder.getFromLocationName(addr,5);
            if (address == null || address.isEmpty()) {
                Toast.makeText(getActivity(), "Invalid Address", Toast.LENGTH_SHORT).show();
                checkbox_meet.setChecked(false);
                return new GeoPoint(0, 0);
            }
            Address location = address.get(0);
            return new GeoPoint(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            return new GeoPoint(0, 0);
        }
    }
}

