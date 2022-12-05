package com.example.lesorac.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.lesorac.R;
import com.example.lesorac.adapter.ProductImageAdapter;
import com.example.lesorac.fragments.LocationDialogFragment;
import com.example.lesorac.model.Product;
import com.example.lesorac.model.ProductImage;
import com.example.lesorac.util.FirebaseUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SellActivity extends BaseActivity implements LocationDialogFragment.onInputListener {
    private Button upload_btn, sell_btn, update_btn;
    private EditText et_name, et_description, et_price;
    private CheckBox checkbox_meet, checkbox_delivery;
    private RadioGroup radioGroup;
    private RadioButton radioButton, radioButton1, radioButton2;
    private RecyclerView product_recycler;
    private ArrayList<ProductImage> productImagesList;
    private ArrayList<String> imageUrl, dealMethod;
    private ProductImageAdapter mAdapter;
    private AutoCompleteTextView autoCompleteTxt;
    private ArrayAdapter<String> dropdownListAdapter;
    private String product_name, product_description, product_category, product_condition, user_id, productID;
    private String categories[];
    private Product product;
    private double product_price;
    private GeoPoint deal_location;
    private FirebaseAuth mFireAuth;
    private FirebaseFirestore mFireStore;
    private FirebaseStorage mStorage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private LocationDialogFragment mLocationDiaglog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        mFireAuth = FirebaseUtil.getAuth();
        mFireStore = FirebaseUtil.getFirestore();
        mStorage = FirebaseUtil.getStorage();

        user_id = mFireAuth.getCurrentUser().getUid();
        storageRef = mStorage.getReference(user_id).child("product_image");

        upload_btn = findViewById(R.id.sell_activity_btn_upload);
        sell_btn = findViewById(R.id.sell_activity_btn_sell);
        update_btn = findViewById(R.id.sell_activity_btn_update);
        et_name = findViewById(R.id.sell_activity_product_et_name);
        et_description = findViewById(R.id.sell_activity_product_et_description);
        et_price = findViewById(R.id.sell_activity_product_et_price);
        checkbox_meet = findViewById(R.id.sell_activity_checkbox_meet);
        checkbox_delivery = findViewById(R.id.sell_activity_checkbox_delivery);
        radioGroup = findViewById(R.id.sell_activity_RadioGroup1);
        radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        radioButton1 = findViewById(R.id.sell_activity_radio_button1);
        radioButton2 = findViewById(R.id.sell_activity_radio_button2);

        product_recycler = findViewById(R.id.sell_activityrecycler_product_image);

        autoCompleteTxt = findViewById(R.id.autoCompleteTextView);
        product_category = autoCompleteTxt.getText().toString();
        categories = getResources().getStringArray(R.array.categories);
        dropdownListAdapter = new ArrayAdapter<String>(this, R.layout.dropdown_item, categories);

        productImagesList = new ArrayList<ProductImage>();
        dealMethod = new ArrayList<String>();
        imageUrl = new ArrayList<String>();

        LinearLayoutManager horizontalLayout = new LinearLayoutManager(SellActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        product_recycler.setLayoutManager(horizontalLayout);
        mLocationDiaglog = new LocationDialogFragment();

        mAdapter = new ProductImageAdapter(productImagesList);
        product_recycler.setAdapter(mAdapter);
        autoCompleteTxt.setAdapter(dropdownListAdapter);

        if (getIntent().getStringExtra("productID") != null) {
            productID = getIntent().getStringExtra("productID");
            sell_btn.setVisibility(View.GONE);
            update_btn.setVisibility(View.VISIBLE);
            initUpdateInfo();
        }

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoPickerLauncher.launch("image/*");
            }
        });

        autoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                product_category = adapterView.getItemAtPosition(position).toString();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                product_condition = radioButton.getText().toString();
            }
        });

        sell_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateImageURLAndSellProduct();
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateImageURLAndSellProduct();
            }
        });

        checkbox_meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox_meet.isChecked()) {
                    mLocationDiaglog.show(getSupportFragmentManager(), "Location");
                } else {
                    checkbox_meet.setChecked(false);
                    deal_location = null;
                }
            }
        });
    }

    ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), new ActivityResultCallback<List<Uri>>() {
                @Override
                public void onActivityResult(List<Uri> result) {
                    for (Uri uri : result) {
                        productImagesList.add(new ProductImage(uri));
                        if (productImagesList.size() == 6) break;
                    }
                    mAdapter.notifyDataSetChanged();

                }
            }
    );

    private void generateImageURLAndSellProduct() {
        Task<Uri> urlTask = null;
        if (infoIsValid()) {
            for (ProductImage image : productImagesList) {
                InputStream stream = null;
                try {
                    stream = getContentResolver().openInputStream(image.getUri());
                    uploadTask = storageRef.child(product_name + image.getUri().getLastPathSegment()).putStream(stream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return storageRef.child(product_name + image.getUri().getLastPathSegment()).getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            imageUrl.add(downloadUri.toString());
                        } else {
                        }
                    }
                });

            }

            urlTask.addOnCompleteListener(SellActivity.this, new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Product product = new Product(product_name, product_price, product_description, product_category, product_condition,
                            "Selling", dealMethod, deal_location, imageUrl, user_id);

                    if (productID == null) {
                        mFireStore.collection("products").document().set(product);
                        Toast.makeText(SellActivity.this, "Add product successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        mFireStore.collection("products")
                                .document(productID)
                                .set(product);

                        Toast.makeText(SellActivity.this, "Update product successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SellActivity.this, ProfileActivity.class);

                        intent.putExtra("User_Id", user_id);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private boolean infoIsValid() {
        if (productImagesList.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SellActivity.this);
            builder.setMessage("Please choose at least one photo").setTitle("Warning").setPositiveButton("OK", null);

            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        LinearLayout linlay_checkbox = findViewById(R.id.sell_activity_linlay_checkbox);
        dealMethod.clear();
        for (int i = 0; i < linlay_checkbox.getChildCount(); i++) {
            View v = linlay_checkbox.getChildAt(i);
            CheckBox checkbox = (CheckBox) v;
            if (checkbox.isChecked()) dealMethod.add(checkbox.getText().toString());
        }
        if (dealMethod.isEmpty()) {
            checkbox_meet.setError("Please choose at least one dealing method");
            checkbox_meet.requestFocus();

            checkbox_delivery.setError("Please choose at least one dealing method");
            checkbox_delivery.requestFocus();
            return false;
        } else {
            checkbox_meet.setError(null);
            checkbox_delivery.setError(null);
        }
        if (TextUtils.isEmpty((et_name.getText().toString()))) {
            et_name.setError("Product name cannot be empty");
            return false;
        } else {
            product_name = et_name.getText().toString();
        }

        if (TextUtils.isEmpty((et_price.getText().toString()))) {
            et_price.setError("Product price cannot be empty");
            return false;
        } else {
            product_price = Double.parseDouble(et_price.getText().toString());
        }

        if (TextUtils.isEmpty((et_description.getText().toString()))) {
            et_description.setError("Product description cannot be empty");
            return false;
        } else {
            product_description = et_description.getText().toString();
        }
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            radioButton.setError("Choose one");
            return false;
        }
        return true;

    }

    public void initUpdateInfo() {
        DocumentReference docRef = mFireStore.collection("products").document(productID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    et_name.setText(document.getData().get("product_name").toString());
                    et_description.setText(document.getData().get("product_description").toString());
                    et_price.setText(document.getData().get("product_price").toString());

                    LinearLayout linlay_checkbox = findViewById(R.id.sell_activity_linlay_checkbox);
                    List<Object> dealMethodList = (List<Object>) document.getData().get("product_deal_method");
                    for (int i = 0; i < linlay_checkbox.getChildCount(); i++) {
                        View v = linlay_checkbox.getChildAt(i);
                        CheckBox checkbox = (CheckBox) v;
                        if (dealMethodList.contains(checkbox.getText().toString()))
                            checkbox.setChecked(true);
                    }

                    String productCondition = document.getData().get("product_condition").toString();

                    if (radioButton1.getText().toString().equals(productCondition)) {
                        radioButton1.setChecked(true);
                    } else {
                        radioButton2.setChecked(true);
                        radioButton1.setChecked(false);
                    }


                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(document.getData().get("product_category"))) {
                            autoCompleteTxt.setText(autoCompleteTxt.getAdapter().getItem(i).toString(), false);
                            product_category = categories[i];
                            break;
                        }
                    }

                    GeoPoint location = (GeoPoint) document.getData().get("deal_location");

                    if (location != null) {
                        deal_location = location;
                    }
                }
            }
        });
    }

    @Override
    public void sendInput(GeoPoint location) {
        Log.d("location", location.toString());

        deal_location = location.getLatitude() == 0 && location.getLatitude() == 0
                ? null
                : location;


    }
}