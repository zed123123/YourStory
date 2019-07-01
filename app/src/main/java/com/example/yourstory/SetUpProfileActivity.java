package com.example.yourstory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpProfileActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri = null;

    private Button uploadImageBtn, saveProfileBtn;
    private EditText nameEditTxt, bioEditTxt;
    private ProgressBar setupProfileProgress;

    private String userId;

    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar setupToolbar;
    private String back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userId = firebaseAuth.getCurrentUser().getUid();

        setupImage = findViewById(R.id.setup_profile_image_view);
        uploadImageBtn = findViewById(R.id.setup_upload_image_btn);
        saveProfileBtn = findViewById(R.id.setup_profile_save_brn);
        nameEditTxt = findViewById(R.id.setup_profile_name_edit_text);
        bioEditTxt = findViewById(R.id.setup_profile_bio_edit_text);
        setupProfileProgress = findViewById(R.id.setup_profile_progress);



        back = getIntent().getStringExtra("backBtn");

        setupToolbar = findViewById(R.id.setup_profile_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Cập nhật thông tin cá nhân");

        if(Objects.equals(back, "profileFragment")){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_onyx);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        setupProfileProgress.setVisibility(View.VISIBLE);
        saveProfileBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        String name = task.getResult().getString("userName");
                        String image = task.getResult().getString("userImage");
                        String bioTxt = task.getResult().getString("userBio");

                        mainImageUri = Uri.parse(image);

                        nameEditTxt.setText(name);
                        bioEditTxt.setText(bioTxt);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.color.colorOnyx);

                        Glide.with(SetUpProfileActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);

                    }
//                    else{
//                        Toast.makeText(SetUpProfileActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
//
//                    }

                }
                else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetUpProfileActivity.this, "(FIRESTORE Retrieve Error): " + error, Toast.LENGTH_SHORT).show();
                }

                setupProfileProgress.setVisibility(View.INVISIBLE);
                saveProfileBtn.setEnabled(true);
            }
        });




        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Kiểm tra Version của máy phải trên Marshmallow mới cần xin phép đọc bộ nhớ ngoài
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetUpProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetUpProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetUpProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else{
                        BringImagePicker();
                    }
                }
                else{
                    BringImagePicker();
                }

            }
        });


        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = nameEditTxt.getText().toString();
                String bioText = "";
                if(!TextUtils.isEmpty(bioEditTxt.getText())){
                    bioText = bioEditTxt.getText().toString();
                }

                if(!TextUtils.isEmpty(userName) && mainImageUri != null){
                    setupProfileProgress.setVisibility(View.VISIBLE);

                    if(isChanged){
                        final StorageReference image_path =storageReference.child("Profile_image").child(userId + ".jpg");

                        final String finalBioText = bioText;

                        image_path.putFile(mainImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){

                                    storeFirestore(task, userName, finalBioText);

                                }
                                else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetUpProfileActivity.this, "(IMAGE Error): " + error, Toast.LENGTH_LONG).show();

                                    setupProfileProgress.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                    else {
                        storeFirestore(null, userName, bioText);
                    }
                }
            }
        });

    }

    private void storeFirestore (Task<Uri> task, String user_name, String user_bio){
        Uri donwload_uri;

        if(task != null){
            donwload_uri = task.getResult();
        }
        else {
            donwload_uri = mainImageUri;
        }



        Map<String, String> userMap = new HashMap<>();
        userMap.put("userName", user_name);
        userMap.put("userBio", user_bio);
        userMap.put("userImage", donwload_uri.toString());

        // Lưu thông tin người dùng vào FireStore
        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(SetUpProfileActivity.this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetUpProfileActivity.this, "(FIRESTORE Error): " + error, Toast.LENGTH_SHORT).show();

                    setupProfileProgress.setVisibility(View.INVISIBLE);

                }
            }
        });
    }

    private void BringImagePicker() {
        // Mở Activity cắt hình
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(SetUpProfileActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);

                isChanged = true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }
}
