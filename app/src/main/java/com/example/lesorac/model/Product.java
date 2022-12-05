package com.example.lesorac.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Product implements Parcelable{
    private String product_name;
    private double product_price;
    private String product_description;
    private String product_category;
    private String product_condition;
    private String product_status;
    private ArrayList<String> product_deal_method;
    private GeoPoint deal_location ;
    private ArrayList<String> product_img_url;
    private String user_id;
    private String product_id;

    public Product(String product_name,
                   double product_price,
                   String product_description,
                   String product_category,
                   String product_condition,
                   String product_status,
                   ArrayList<String> product_deal_method,
                   GeoPoint deal_location,
                   ArrayList<String> product_img_url,
                   String user_id) {
        this.product_name = product_name;
        this.product_price = product_price;
        this.product_description = product_description;
        this.product_category = product_category;
        this.product_condition = product_condition;
        this.product_status = product_status;
        this.product_deal_method = product_deal_method;
        this.deal_location = deal_location;
        this.product_img_url = product_img_url;
        this.user_id = user_id;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Product(){}

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(double product_price) {
        this.product_price = product_price;
    }

    public String getProduct_description() {
        return product_description;
    }

    public void setProduct_description(String product_description) {
        this.product_description = product_description;
    }

    public String getProduct_category() {
        return product_category;
    }

    public void setProduct_category(String product_category) {
        this.product_category = product_category;
    }

    public String getProduct_condition() {
        return product_condition;
    }

    public void setProduct_condition(String product_condition) {
        this.product_condition = product_condition;
    }

    public String getProduct_status() {
        return product_status;
    }

    public void setProduct_status(String product_status) {
        this.product_status = product_status;
    }

    public ArrayList<String> getProduct_deal_method() {
        return product_deal_method;
    }

    public void setProduct_deal_method(ArrayList<String> product_deal_method) {
        this.product_deal_method = product_deal_method;
    }

    public GeoPoint getDeal_location() {
        return deal_location;
    }

    public void setDeal_location(GeoPoint deal_location) {
        this.deal_location = deal_location;
    }

    public ArrayList<String> getProduct_img_url() {
        return product_img_url;
    }

    public void setProduct_img_url(ArrayList<String> product_img_url) {
        this.product_img_url = product_img_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.product_category);
        parcel.writeString(this.product_condition);
        parcel.writeString(this.product_description);
        parcel.writeString(this.product_name);
        parcel.writeString(this.product_status);
        parcel.writeDouble(this.product_price);
        parcel.writeString(this.product_id);
        parcel.writeString(this.user_id);
        parcel.writeList(this.product_deal_method);
        parcel.writeList(this.product_img_url);
        if(deal_location != null){
            parcel.writeDouble(deal_location.getLatitude());
            parcel.writeDouble(deal_location.getLongitude());
        }
    }

    protected Product(Parcel in) {
        this.product_category = in.readString();
        this.product_condition = in.readString();
        this.product_description = in.readString();
        this.product_name = in.readString();
        this.product_status = in.readString();
        this.product_price = in.readDouble();
        this.product_id = in.readString();
        this.user_id = in.readString();
        this.product_deal_method = in.readArrayList(String.class.getClassLoader());
        this.product_img_url = in.readArrayList(String.class.getClassLoader());
        this.deal_location = new GeoPoint(in.readDouble(),in.readDouble());
    }
}
