package com.example.lesorac.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.lesorac.R;
import com.example.lesorac.model.Product;
import com.example.lesorac.model.User;
import com.example.lesorac.util.Constants;
import com.example.lesorac.util.FirebaseUtil;
import com.example.lesorac.util.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductDetailActivity extends BaseActivity implements View.OnClickListener, OnMapReadyCallback {

    private Product product;
    private TextView tv_product_title, tv_product_price, tv_product_condition, tv_seller_name,
            tv_product_description, tv_product_dealMethod,tv_product_type;

    private LinearLayout llMap, llDealMethod;

    private Button btn_viewProfile, btn_chat, btn_buy, btn_edit, btn_delete;
    private CircleImageView profile_image;
    private CardView btm_cardView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String sellerName, imgURL, fcmToken, uid;
    private User user;
    private PreferenceManager preferenceManager;
    private Intent intent;
    private String buyerName;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tv_product_title = findViewById(R.id.product_detail_activity_product_title);
        tv_product_price = findViewById(R.id.product_detail_activity_product_price);
        tv_product_condition = findViewById(R.id.product_detail_activity_product_condition);
        tv_product_description = findViewById(R.id.product_detail_activity_product_description);
        tv_product_dealMethod = findViewById(R.id.product_detail_activity_product_dealMethod);
        tv_seller_name = findViewById(R.id.product_detail_activity_seller_name);
        tv_product_type = findViewById(R.id.product_detail_activity_product_cat);

        llMap = findViewById(R.id.product_detail_activity_map_layout);
        llDealMethod = findViewById(R.id.product_detail_activity_dealMethod_layout);

        profile_image = findViewById(R.id.product_detail_activity_seller_profile_img);
        btn_viewProfile = findViewById(R.id.product_detail_activity_btn_view_profile);
        btn_chat = findViewById(R.id.product_detail_activity_btn_chat);
        btn_buy = findViewById(R.id.product_detail_activity_btn_buy);
        btn_edit = findViewById(R.id.product_detail_activity_btn_edit);
        btn_delete = findViewById(R.id.product_detail_activity_btn_delete);
        btm_cardView = findViewById(R.id.product_detail_activity_btm_cardView);

        preferenceManager = new PreferenceManager(getApplicationContext());

        mFirestore = FirebaseUtil.getFirestore();
        mAuth = FirebaseUtil.getAuth();

        if (getIntent().getExtras() != null) {
            product = (Product) getIntent().getParcelableExtra("Product");
        }

        uid = product.getUser_id();
        if(uid.equals(mAuth.getCurrentUser().getUid())){
            btn_viewProfile.setVisibility(View.GONE);
        }

        if (!product.getProduct_deal_method().contains("Delivery")) {
            llDealMethod.setVisibility(View.GONE);
        }

        if (product.getDeal_location().getLatitude() != 0 && product.getDeal_location().getLongitude() != 0) {
            llMap.setVisibility(View.VISIBLE);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        ImageSlider imgSlider = findViewById(R.id.product_detail_activity_image_slider);
        List<SlideModel> slideModelList = new ArrayList<>();

        for (String imgURL : product.getProduct_img_url()) {
            slideModelList.add(new SlideModel(imgURL, "", ScaleTypes.CENTER_INSIDE));
        }
        imgSlider.setImageList((slideModelList));
        initInfo();

        btn_viewProfile.setOnClickListener(this);
        btn_chat.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        btn_edit.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }

    private void initInfo() {
        tv_product_title.setText(product.getProduct_name());
        tv_product_price.setText("RM " + Double.toString(product.getProduct_price()));
        tv_product_condition.setText(product.getProduct_condition());
        tv_product_description.setText(product.getProduct_description());
        tv_product_type.setText(product.getProduct_category());

        if (product.getProduct_status().equals("Sold"))
            btm_cardView.setVisibility(View.GONE);


        if (!preferenceManager.getString(Constants.KEY_UID).equals(uid)) {
            btn_chat.setVisibility(View.VISIBLE);
            btn_buy.setVisibility(View.VISIBLE);
        }else if(getIntent().getStringExtra("from") != null){
            btn_edit.setVisibility(View.VISIBLE);
            btn_delete.setVisibility(View.VISIBLE);
        }

        DocumentReference docRef = mFirestore.collection("users").document(product.getUser_id());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        sellerName = document.getData().get("name").toString();
                        tv_seller_name.setText(sellerName);
                        imgURL = document.getData().get("photo").toString();
                        fcmToken = document.getData().get("fcmToken").toString();
                        if (!imgURL.equals("")) {
                            Glide
                                    .with(ProductDetailActivity.this)
                                    .load(imgURL)
                                    .placeholder(R.color.white)
                                    .into(profile_image);
                        }
                        user = new User();
                        user.setName(sellerName);
                        user.setPhoto(imgURL);
                        user.setfcmToken(fcmToken);
                        user.setUid(product.getUser_id());
                    }
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        switch (view.getId()) {
            case R.id.product_detail_activity_btn_view_profile:
                intent = new Intent(ProductDetailActivity.this, ProfileActivity.class);
                intent.putExtra("User_Id", product.getUser_id());
                startActivity(intent);
                break;
            case R.id.product_detail_activity_btn_chat:
                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("User_Id", preferenceManager.getString(Constants.KEY_UID));
                intent.putExtra(
                        "message",
                        "I would like to get more details about \nProduct Name: " + product.getProduct_name() + "\nid: " + product.getProduct_id() + "\nPrice: RM" + product.getProduct_price());
                startActivity(intent);
                break;
            case R.id.product_detail_activity_btn_buy:
                alert.setTitle("Confirmation Box");
                alert.setMessage("Are you sure you want to buy this product?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DocumentReference docRef = mFirestore.collection("products").document(product.getProduct_id());
                        docRef
                                .update("product_status", "Sold")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                        Toast.makeText(ProductDetailActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override//
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.create().show();
                break;
            case R.id.product_detail_activity_btn_delete:
                alert.setTitle("Confirmation Box");
                alert.setMessage("Are you sure you want to delete this product?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DocumentReference docRef = mFirestore.collection("products").document(product.getProduct_id());
                        docRef
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                        Toast.makeText(ProductDetailActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override//
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.create().show();
                break;
            case R.id.product_detail_activity_btn_edit:
                intent = new Intent(ProductDetailActivity.this, SellActivity.class);
                intent.putExtra("productID",product.getProduct_id());
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (product.getDeal_location() != null) {
            LatLng meetupLocation = new LatLng(product.getDeal_location().getLatitude(), product.getDeal_location().getLongitude());
            map.addMarker(new MarkerOptions().position(meetupLocation).title("Meet Up Location"));
            map.moveCamera(CameraUpdateFactory.newLatLng(meetupLocation));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
        //if(product.getDeal_location()!=null){
        //Log.d("location",Double.toString(product.getDeal_location().getLatitude())) ;
        //Log.d("location",Double.toString(product.getDeal_location().getLongitude())) ;
        //}


    }
}