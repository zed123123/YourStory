package com.example.yourstory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private ProgressBar newPostProgressbar;
    private TextView newImageTxt;

    private Uri postImageUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();


        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Bài mới");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newPostToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_edit_text);
        newPostBtn = findViewById(R.id.new_post_upload_btn);
        newPostProgressbar = findViewById(R.id.new_post_progress);
        newImageTxt = findViewById(R.id.new_post_choose_image_txt);


        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Mở Activity cắt hình
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(NewPostActivity.this);



            }
        });


        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null){


//                    if(desc.length() <= 125){


                        newPostProgressbar.setVisibility(View.VISIBLE);

                        final String randomName = UUID.randomUUID().toString();
                        final StorageReference filePath = storageReference.child("Post_image/Full_size_image").child(randomName + ".jpg");
                        filePath.putFile(postImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull final Task<Uri> task) {

                                final String downloadUri = task.getResult().toString();


                                if(task.isSuccessful()){

                                    File newImageFile = new File(postImageUri.getPath());


                                    try {
                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                .setMaxWidth(150)
                                                .setMaxHeight(150)
                                                .setQuality(100)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }



                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumbData = baos.toByteArray();

                                    final StorageReference thumbPath = storageReference.child("Post_image/Thumb").child(randomName + ".jpg");
                                    final UploadTask uploadTask = thumbPath.putBytes(thumbData);

                                    // Lấy downloadUrl của Thumbnail
                                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if(!task.isSuccessful()){
                                                throw task.getException();
                                            }
                                            return thumbPath.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            final String downloadThumbUri = task.getResult().toString();


                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                                    Map<String, Object> postMap = new HashMap<>();
                                                    postMap.put("blogImageUrl", downloadUri);
                                                    postMap.put("blogImageThumbnail", downloadThumbUri);
                                                    postMap.put("blogTxt", desc);
                                                    postMap.put("userId", current_user_id);
                                                    postMap.put("timestamp", FieldValue.serverTimestamp());

                                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {

                                                            if (task.isSuccessful()){

                                                                Toast.makeText(NewPostActivity.this, "Đã đăng bài", Toast.LENGTH_SHORT).show();

                                                                Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);
                                                                finish();
                                                            }
                                                            else {



                                                            }

                                                            newPostProgressbar.setVisibility(View.INVISIBLE);

                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    // Error handling

                                                }
                                            });
                                        }
                                    });
                                }
                                else {

                                    newPostProgressbar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });



//                    }
//                    else{
//                        Toast.makeText(NewPostActivity.this, "Không được quá 125 ký tự", Toast.LENGTH_SHORT).show();
//                    }



                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

                newImageTxt.setVisibility(View.INVISIBLE);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
