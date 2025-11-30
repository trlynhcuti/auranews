package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.utils.NetworkUtil;
import com.example.auranews.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import com.example.auranews.utils.RegisterValidator;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (!NetworkUtil.isConnected(this)) {
            Intent intent = new Intent(RegisterActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        if (!RegisterValidator.validateInput(edtEmail, edtPassword, edtConfirmPassword)) {
            return;
        }

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Gửi email xác thực
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {

                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this,
                                                    "Không gửi được email xác thực: " +
                                                            verifyTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }

                                        // Lưu thông tin user vào Firestore
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);
                                        userData.put("role", "user");
                                        userData.put("total_read_articles", 0);
                                        userData.put("total_read_time", 0);

                                        db.collection("users")
                                                .document(uid)
                                                .set(userData)
                                                .addOnSuccessListener(unused -> {
                                                    startActivity(new Intent(
                                                            RegisterActivity.this,
                                                            LoginActivity.class
                                                    ));
                                                    finish();
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this,
                                                                "Lỗi lưu Firestore: " + e.getMessage(),
                                                                Toast.LENGTH_LONG).show()
                                                );
                                    });
                        }
                    } else {
                        Toast.makeText(this,
                                "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
