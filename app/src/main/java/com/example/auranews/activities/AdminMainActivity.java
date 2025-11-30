package com.example.auranews.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.adapters.FunctionAdapter;
import com.example.auranews.models.FunctionItem;
import com.example.auranews.models.Post;
import com.example.auranews.utils.CategoryUtils;
import com.example.auranews.utils.CloudinaryUploader;
import com.example.auranews.utils.NetworkUtil;
import com.example.auranews.utils.WordUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ListView lvFunction;
    private ImageView prvImage;
    private TextView prvContent, tvTitlePreview, tvCategoryPreview;
    private Button btnPost, btnManager;
    private EditText edtTitle;
    private Spinner spinner;
    private ImageView prvImageItem;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> wordPickerLauncher;

    private String uploadedImageUrl = "";
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    private final String[] categories = {
            "Tin tức",
            "Thế giới",
            "Xã hội",
            "Thể thao",
            "Giải trí",
            "Công nghệ",
            "Sức khỏe"
    };
    private String selectedCategory = "";

    private boolean isEditMode = false;
    private String editingPostId = null;

    private CheckRole checkRole;
    private CloudinaryUploader cloudinaryUploader;

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void pickWordFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        wordPickerLauncher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(AdminMainActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        lvFunction = findViewById(R.id.lvFunction);
        prvImage = findViewById(R.id.prvImage);
        prvContent = findViewById(R.id.prvContent);
        btnPost = findViewById(R.id.btnPost);
        btnManager = findViewById(R.id.btnManager);
        edtTitle = findViewById(R.id.editText_title);
        spinner = findViewById(R.id.spinner_categories);

        tvCategoryPreview = findViewById(R.id.textView_category);
        tvTitlePreview = findViewById(R.id.textView_title);
        prvImageItem = findViewById(R.id.prvImage_item);

        auth = FirebaseAuth.getInstance();
        checkRole = new CheckRole(this);
        cloudinaryUploader = new CloudinaryUploader();

        btnPost.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        CategoryUtils.setupCategorySpinner(this, spinner, categories, category -> {
            selectedCategory = category;
            if (tvCategoryPreview != null) {
                tvCategoryPreview.setText("Danh mục: " + selectedCategory);
            }
        });

        edtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvTitlePreview != null) tvTitlePreview.setText(s.toString());
                checkValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        prvImage.setImageURI(uri);
                        if (prvImageItem != null) prvImageItem.setImageURI(uri);

                        progressDialog.setMessage("Đang tải ảnh lên...");
                        progressDialog.show();
                        btnPost.setEnabled(false);

                        cloudinaryUploader.uploadImage(this, uri,
                                new CloudinaryUploader.UploadCallback() {
                                    @Override
                                    public void onSuccess(String url) {
                                        runOnUiThread(() -> {
                                            progressDialog.dismiss();
                                            uploadedImageUrl = url;
                                            checkValidation();
                                            Toast.makeText(AdminMainActivity.this,
                                                    "Upload ảnh thành công!",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        runOnUiThread(() -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(AdminMainActivity.this,
                                                    "Lỗi upload ảnh: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                    }
                }
        );

        wordPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        progressDialog.setMessage("Đang đọc file...");
                        progressDialog.show();

                        WordUtils.readDocx(this, uri, new WordUtils.WordReadCallback() {
                            @Override
                            public void onSuccess(String text) {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    prvContent.setText(text);
                                    TextView tvPreview = findViewById(R.id.textView_preview);
                                    if (tvPreview != null) tvPreview.setText(text);
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AdminMainActivity.this,
                                            "Không đọc được file Word!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }
        );

        List<FunctionItem> list = new ArrayList<>();
        list.add(new FunctionItem(R.drawable.ic_add_photo, "Chọn ảnh bìa"));
        list.add(new FunctionItem(R.drawable.ic_add_file, "Chọn nội dung (.docx)"));
        FunctionAdapter adapter = new FunctionAdapter(this, list);
        lvFunction.setAdapter(adapter);
        lvFunction.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) pickImage();
            else if (position == 1) pickWordFile();
        });

        btnBack.setOnClickListener(v -> finish());

        btnManager.setOnClickListener(v ->
                startActivity(new Intent(AdminMainActivity.this, PostManagerActivity.class)));

        btnPost.setOnClickListener(v -> postArticle());

        checkEditMode();
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("POST_ID")) {
            isEditMode = true;
            editingPostId = intent.getStringExtra("POST_ID");
            String oldTitle = intent.getStringExtra("POST_TITLE");
            String oldContent = intent.getStringExtra("POST_CONTENT");
            String oldCategory = intent.getStringExtra("POST_CATEGORY");
            uploadedImageUrl = intent.getStringExtra("POST_IMAGE");

            edtTitle.setText(oldTitle);
            prvContent.setText(oldContent);
            btnPost.setText("CẬP NHẬT");

            if (oldCategory != null) {
                selectedCategory = oldCategory;
                if (tvCategoryPreview != null) {
                    tvCategoryPreview.setText("Danh mục: " + oldCategory);
                }
            }

            if (oldCategory != null) {
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(oldCategory)) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }

            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                Picasso.get().load(uploadedImageUrl).into(prvImage);
                if (prvImageItem != null) {
                    Picasso.get().load(uploadedImageUrl).into(prvImageItem);
                }
            }

            btnPost.setEnabled(true);
        }
    }

    private void checkValidation() {
        String title = edtTitle.getText().toString().trim();
        btnPost.setEnabled(!title.isEmpty() && !uploadedImageUrl.isEmpty());
    }

    private void postArticle() {
        String title = edtTitle.getText().toString().trim();
        String content = prvContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Thiếu tiêu đề hoặc nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uploadedImageUrl.isEmpty()) {
            Toast.makeText(this, "Chưa có ảnh bìa!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorId = user.getUid();
        String authorEmail = user.getEmail();

        progressDialog.setMessage(isEditMode ? "Đang cập nhật..." : "Đang đăng bài...");
        progressDialog.show();

        Post post = new Post(title, content, selectedCategory,
                uploadedImageUrl, authorId, authorEmail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (isEditMode && editingPostId != null) {
            db.collection("posts").document(editingPostId)
                    .update(post.toUpdateMap())
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("posts")
                    .add(post.toCreateMap())
                    .addOnSuccessListener(doc -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("NEW_POST_CREATED", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        createNotificationsForNewPost(db, doc.getId(), title);

                        edtTitle.setText("");
                        prvContent.setText("");
                        prvImage.setImageResource(R.drawable.ic_image_placeholder);
                        if (prvImageItem != null) {
                            prvImageItem.setImageResource(R.drawable.ic_image_placeholder);
                        }
                        uploadedImageUrl = "";
                        btnPost.setEnabled(false);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi đăng bài!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void createNotificationsForNewPost(FirebaseFirestore db,
                                               String postId,
                                               String title) {

        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot userDoc : qs) {
                        String uid = userDoc.getId();

                        HashMap<String, Object> noti = new HashMap<>();
                        noti.put("type", "new_post");
                        noti.put("articleId", postId);
                        noti.put("title", title);
                        noti.put("createdAt", System.currentTimeMillis());
                        noti.put("isRead", false);

                        db.collection("users")
                                .document(uid)
                                .collection("notifications")
                                .add(noti);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Không tạo được thông báo bài viết mới: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
