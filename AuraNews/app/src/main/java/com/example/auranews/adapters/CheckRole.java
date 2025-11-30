package com.example.auranews.adapters;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CheckRole {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;

    public CheckRole(Context context){
        this.context = context;
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Interface callback để trả kết quả về
    public interface RoleCallback {
        void onRoleChecked(String role);
    }

    // Hàm kiểm tra role
    public void getUserRole(@NonNull RoleCallback callback){
        FirebaseUser user = auth.getCurrentUser();
        if(user == null){
            callback.onRoleChecked("guest"); // nếu chưa đăng nhập
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        String role = documentSnapshot.getString("role");
                        callback.onRoleChecked(role != null ? role : "user");
                    } else {
                        callback.onRoleChecked("user"); // mặc định user
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CheckRole", "Lỗi lấy role", e);
                    callback.onRoleChecked("user"); // lỗi cũng trả về user
                });

    }

}
