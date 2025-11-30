package com.example.auranews.utils;

import android.util.Patterns;
import android.widget.EditText;

public class AuthInputValidator {

    public static boolean validateLogin(EditText edtEmail, EditText edtPassword) {
        String email = edtEmail.getText().toString().trim();
        String pass  = edtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }

        if (pass.isEmpty()) {
            edtPassword.setError("Nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }
}
