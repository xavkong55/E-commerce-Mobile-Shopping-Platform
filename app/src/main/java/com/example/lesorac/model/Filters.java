package com.example.lesorac.model;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class Filters {
    private String condition = null;
    private ArrayList<String> dealMethod = new ArrayList<>();
    private double minPrice = -1;
    private double maxPrice = -1;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {
    }

    public static Filters getDefault(){
        Filters filters = new Filters();
        return filters;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public ArrayList<String> getDealMethod() {
        return dealMethod;
    }

    public void setDealMethod(ArrayList<String> dealMethod) {
        this.dealMethod = dealMethod;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean hasCondition(){return !(TextUtils.isEmpty(condition));}
    public boolean hasMinPrice(){return minPrice > 0;}
    public boolean hasMaxPrice(){return maxPrice > 0;}
    public boolean hasDealMethod(){return dealMethod.size() > 0;}

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        /*
        if (category == null && city == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_restaurants));
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && city != null) {
            desc.append(" in ");
        }

        if (city != null) {
            desc.append("<b>");
            desc.append(city);
            desc.append("</b>");
        }

        if (price > 0) {
            desc.append(" for ");
            desc.append("<b>");
            desc.append(RestaurantUtil.getPriceString(price));
            desc.append("</b>");
        }

         */

        return desc.toString();
    }
}
