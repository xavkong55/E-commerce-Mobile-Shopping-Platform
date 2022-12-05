package com.example.lesorac.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.adapter.TabPagerAdapter;
import com.example.lesorac.fragments.Products;
import com.example.lesorac.fragments.Reviews;
import com.example.lesorac.util.Constants;
import com.example.lesorac.util.FirebaseUtil;
import com.example.lesorac.util.PreferenceManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends FragmentActivity {

    TabLayout tabLayout;
    LinearLayout linlay_show_name, linlay_edit_name, linlay_detail;
    ViewPager2 viewPager;
    TabPagerAdapter pagerAdapter;
    TextView tv_user_name, tv_avg_rating, tv_product_num;
    EditText et_name;
    CircleImageView image_edit_icon, profile_image;
    ImageView name_edit_icon, confirm_edit_icon, cancel_edit_icon, back_icon;

    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private String uid, name;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private DocumentReference UserRef;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        uid = getIntent().getStringExtra("User_Id");

        mFirestore = FirebaseUtil.getFirestore();
        mStorage = FirebaseUtil.getStorage();
        mAuth = FirebaseUtil.getAuth();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tv_user_name = findViewById(R.id.profile_activity_tv_name);
        tv_product_num = findViewById(R.id.profile_activity_tv_products_num);
        tv_avg_rating = findViewById(R.id.profile_activity_tv_avg_rating);
        image_edit_icon = findViewById(R.id.profile_image_edit_icon);
        profile_image = findViewById(R.id.profile_activity_profile_image);
        name_edit_icon = findViewById(R.id.profile_name_edit_icon);
        confirm_edit_icon = findViewById(R.id.profile_activity_icon_cfm);
        cancel_edit_icon = findViewById(R.id.profile_activity_icon_cancel);
        back_icon = findViewById(R.id.imageBack);
        et_name = findViewById(R.id.profile_activity_et_name);

        linlay_show_name = findViewById(R.id.linlay_show_name);
        linlay_edit_name = findViewById(R.id.activity_profile_linlay_edit_name);
        linlay_detail = findViewById(R.id.linlay1);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (mAuth.getCurrentUser().getUid().equals(uid)) {
            image_edit_icon.setVisibility(View.VISIBLE);
            name_edit_icon.setVisibility(View.VISIBLE);
            storageRef = mStorage.getReference(uid).child("profile_image").child("profile_image");

        } else {
            image_edit_icon.setVisibility(View.GONE);
            name_edit_icon.setVisibility(View.GONE);
        }

        initInfo();

        viewPager.setUserInputEnabled(false);

        pagerAdapter = new TabPagerAdapter(this);

        pagerAdapter.AddFragment(new Products(), "Products");
        pagerAdapter.AddFragment(new Reviews(), "Reviews");

        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getFragmentName(position))
        ).attach();

        back_icon.setOnClickListener(v -> onBackPressed());

        image_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoPickerLauncher.launch("image/*");
            }
        });


        name_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linlay_detail.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.activity_profile_linlay_edit_name);

                linlay_detail.setLayoutParams(params);

                linlay_edit_name.setVisibility(View.VISIBLE);
                linlay_show_name.setVisibility(View.GONE);

                et_name.setText(name);
                et_name.requestFocus();
            }
        });


        cancel_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeEdit();
            }
        });

        confirm_edit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserRef = mFirestore.collection("users").document(uid);
                String updatedName = et_name.getText().toString();

                UserRef.update("name", updatedName)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                tv_user_name.setText(updatedName);
                                name = updatedName;
                                Toast.makeText(ProfileActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();
                                closeEdit();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                updateConversationInfo("senderName",updatedName);

            }
        });

    }

    ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        try {
                            InputStream stream = getContentResolver().openInputStream(result);
                            uploadTask = storageRef.putStream(stream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                return storageRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    updateProfileImage(downloadUri.toString());
                                    updateConversationInfo("senderImage",downloadUri.toString());
                                    Toast.makeText(ProfileActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();
                                } else {
                                }
                            }
                        });

                    }
                }
            }
    );

    private void initInfo() {
        UserRef = mFirestore.collection("users").document(uid);
        UserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getData().get("name").toString();
                        tv_user_name.setText(name);
                        tv_avg_rating.setText(document.getData().get("avgRating").toString());
                        String imgURL = document.getData().get("photo").toString();

                        if (!imgURL.equals("")) {
                            setImage(imgURL);
                        }
                    }
                }
                ;
            }
        });

        mFirestore.collection("products")
                .whereEqualTo("user_id", uid)
                .whereEqualTo("product_status", "Selling")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    tv_product_num.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }

    private void updateConversationInfo(String updateField, String updateInfo) {

        mFirestore.collection("conversations")
                .whereEqualTo("senderId", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch writeBatch = mFirestore.batch();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef =
                                        mFirestore.collection("conversations").document(document.getId());

                                writeBatch.update(docRef, updateField, updateInfo);
                            }
                            writeBatch.commit();
                        }
                    }
                });

        mFirestore.collection("conversations")
                .whereEqualTo("receiverId", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch writeBatch = mFirestore.batch();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef =
                                        mFirestore.collection("conversations").document(document.getId());

                                writeBatch.update(docRef, "receiverName", updateInfo);
                            }
                            writeBatch.commit();
                        }
                    }
                });
    }

    private void updateProfileImage(String url) {
        DocumentReference UserRef = mFirestore.collection("users").document(uid);

        UserRef.update("photo", url)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setImage(url);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void setImage(String imgURL) {

        Glide
                .with(ProfileActivity.this)
                .load(imgURL)
                .placeholder(R.color.white)
                .into(profile_image);
    }

    private void closeEdit() {

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) linlay_show_name.getLayoutParams();
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) linlay_detail.getLayoutParams();
        params1.addRule(RelativeLayout.BELOW, R.id.imgUser);
        params2.addRule(RelativeLayout.BELOW, R.id.linlay_show_name);
        linlay_show_name.setLayoutParams(params1);

        linlay_show_name.setVisibility(View.VISIBLE);
        linlay_edit_name.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserRef = mFirestore.collection("users").document(preferenceManager.getString(Constants.KEY_UID));
        UserRef.update(Constants.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserRef = mFirestore.collection("users").document(preferenceManager.getString(Constants.KEY_UID));
        UserRef.update(Constants.KEY_AVAILABILITY, 1);
    }
}