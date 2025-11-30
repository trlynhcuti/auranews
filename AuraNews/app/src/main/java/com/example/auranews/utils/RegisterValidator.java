package com.example.auranews.utils;

import android.util.Patterns;
import android.widget.EditText;

public class RegisterValidator {

    public static boolean validateInput(EditText edtEmail,
                                        EditText edtPassword,
                                        EditText edtConfirmPassword) {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPwd = edtConfirmPassword.getText().toString().trim();

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
        if (password.isEmpty()) {
            edtPassword.setError("Mật khẩu không được để trống");
            edtPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPwd)) {
            edtConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            edtConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }
}
