package com.example.lesorac.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.fragments.HomepageFragment;
import com.example.lesorac.util.Constants;
import com.example.lesorac.util.FirebaseUtil;
import com.example.lesorac.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomepageActivity extends BaseActivity{

    private FirebaseAuth mFireAuth;
    private FirebaseFirestore mFirestore;
    private PreferenceManager preferenceManager;
    private DocumentReference docRef;
    private FirebaseUser user;

    Fragment homeFragment;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView tv_logout, tv_name;
    String name;
    View parentView;
    CircleImageView profile_image;
    Button btn_sell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        homeFragment = new HomepageFragment();
        loadFragment(homeFragment);

        mFireAuth = FirebaseUtil.getAuth();
        mFirestore = FirebaseUtil.getFirestore();

        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        navigationView = findViewById(R.id.nav_view);
        parentView = navigationView.getHeaderView(0);

        tv_logout = findViewById(R.id.homepage_activity_logout);
        btn_sell = findViewById(R.id.toolbar_btn_sell);

        tv_name = parentView.findViewById(R.id.nav_header_name);
        profile_image = parentView.findViewById(R.id.nav_header_profile_image);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        preferenceManager = new PreferenceManager(getApplicationContext());
        user = mFireAuth.getCurrentUser();
        docRef = mFirestore.collection("users").document(user.getUid().toString());
        getToken();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent;
                switch (id) {
                    case R.id.profile:
//                        drawerLayout.closeDrawers();
                        intent = new Intent(HomepageActivity.this, ProfileActivity.class);
                        intent.putExtra("User_Id", mFireAuth.getCurrentUser().getUid());
                        startActivity(intent);
                        break;
                    case R.id.inbox:
                        intent = new Intent(HomepageActivity.this, InboxActivity.class);
                        intent.putExtra("User_Id", mFireAuth.getCurrentUser().getUid());
                        intent.putExtra("Username", name);
                        startActivity(intent);
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_logout.setBackgroundResource(R.drawable.menu_back);
                mFireAuth.signOut();
                Intent intent = new Intent(HomepageActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomepageActivity.this,SellActivity.class);
                startActivity(intent);
//                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        initInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawerLayout.closeDrawers();
    }

    private void initInfo() {
        String provider = user.getProviderData().get(0).toString();
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getData().get("name").toString();
                        tv_name.setText(name);
                        String imgURL = document.getData().get("photo").toString();
//
                        if (!imgURL.equals("")) {
                            Glide
                                .with(HomepageActivity.this)
                                .load(imgURL)
                                .placeholder(R.color.white)
                                .into(profile_image);
                        }

                    } else {
                        name = preferenceManager.getString(provider);
                        tv_name.setText(name);
                    }
                } else {
                }
            }
        });
    }

    private void loadFragment(Fragment homeFragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container,homeFragment);
        transaction.commit();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        docRef.update(Constants.KEY_FCM_TOKEN,token)
                //.addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"Unable to update Token",Toast.LENGTH_SHORT).show());

    }
}