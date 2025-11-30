package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.utils.AuthInputValidator;
import com.example.auranews.utils.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private android.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!NetworkUtil.isConnected(this)) {
            Intent intent = new Intent(LoginActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        if (!AuthInputValidator.validateLogin(edtEmail, edtPassword)) {
            return;
        }

        String email = edtEmail.getText().toString().trim();
        String pass  = edtPassword.getText().toString().trim();

        showLoading();

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        checkUserInFirestore(uid, email);
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Không tìm thấy user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_LONG).show();
                });
    }

    private void checkUserInFirestore(String uid, String email) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    hideLoading();
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if (role == null) role = "user";
                        goToMain(role);
                    } else {
                        createNewUser(uid, email);
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                });
    }

    // Tạo user lần đầu đăng nhập
    private void createNewUser(String uid, String email) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("role", "user");
        data.put("email", email);
        data.put("total_read_articles", 0);
        data.put("total_read_time", 0);

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(unused -> {
                    hideLoading();
                    goToMain("user");
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi tạo user!", Toast.LENGTH_SHORT).show();
                });

    }

    private void goToMain(String role) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private void showLoading() {
        if (progressDialog == null) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setCancelable(false);

            android.view.View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
            builder.setView(view);

            progressDialog = builder.create();
        }
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
