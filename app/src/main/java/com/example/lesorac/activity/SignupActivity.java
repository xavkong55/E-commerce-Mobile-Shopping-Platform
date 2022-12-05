package com.example.lesorac.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lesorac.R;
import com.example.lesorac.model.User;
import com.example.lesorac.util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private TextView tv_login;
    private EditText etEmail, etPassword, etCfmPassword, etName;
    private Button btnSignup;

    private FirebaseAuth mFireAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tv_login = findViewById(R.id.signup_activity_return_login);
        etEmail = findViewById(R.id.signup_activity_et_email);
        etPassword = findViewById(R.id.signup_activity_et_password);
        etCfmPassword = findViewById(R.id.signup_activity_et_cfm_password);
        etName = findViewById(R.id.signup_activity_et_name);
        btnSignup = findViewById(R.id.signup_activity_btn_signup);

        mFireAuth = FirebaseUtil.getAuth();
        mFirestore = FirebaseUtil.getFirestore();

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                String cfmPassword = etCfmPassword.getText().toString();
                String name = etName.getText().toString();

                if (isValid(name,email, password, cfmPassword)) {
                    mFireAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mFireAuth.getCurrentUser();
                                String uid = user.getUid().toString();
                                User newUser = new User(
                                        uid,
                                        name,
                                        0,
                                        0,
                                        ""
                                );
                                mFirestore.collection("users").document(uid).set(newUser);

                                etName.setText("");
                                etEmail.setText("");
                                etPassword.setText("");
                                etCfmPassword.setText("");

                                Toast.makeText(SignupActivity.this, "Account created",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignupActivity.this, "Sign up failed.This E-mail has been used",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }

    private boolean isValid(String name, String email, String password, String cfmPassword) {

        if (password.isEmpty() || email.isEmpty() || name.isEmpty() || cfmPassword.isEmpty()) {
            // Remind the user to key in the data
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            builder.setMessage("Please do not leave any field empty").setTitle("Warning").setPositiveButton("OK", null);

            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        else if (!password.equals(cfmPassword) ){
            etPassword.setError("Those passwords didn’t match. Try again.");
            etCfmPassword.setError("Those passwords didn’t match. Try again.");
            return false;
        }
        return true;
    }
}