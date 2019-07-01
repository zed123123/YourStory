package com.example.yourstory;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    private Toolbar otherUserToolbar;
    private CircleImageView profileUserImage;
    private TextView profileUserName;
    private TextView profileUserBio;
    private TextView profileUserPostsCount;
    private TextView profileUserPostsBtn;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        profileUserImage = findViewById(R.id.profile_other_user_image);
        profileUserName = findViewById(R.id.profile_other_user_name);
        profileUserBio = findViewById(R.id.profile_other_user_bio);
        profileUserPostsCount = findViewById(R.id.profile_other_user_posts_count);
        profileUserPostsBtn = findViewById(R.id.profile_other_user_posts_btn);


        otherUserToolbar = findViewById(R.id.profile_other_user_toolbar);
        setSupportActionBar(otherUserToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_onyx);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);




        loadUserData();

        postCount();

        sendToPostsIntent();

    }

    private void sendToPostsIntent() {
        profileUserPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userPotsIntent = new Intent(OtherUserProfileActivity.this, UserPostsActivity.class);
                userPotsIntent.putExtra("userId", userId);
                startActivity(userPotsIntent);
            }
        });
    }


    private void postCount() {
        firebaseFirestore.collection("Posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(25)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){

                            // Lấy <userId> trong <PostID>
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                count++;

                                profileUserPostsCount.setText(count+"");
                            }

                        }

                    }
                });
    }


    private void loadUserData() {
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        String name = task.getResult().getString("userName");
                        String image = task.getResult().getString("userImage");
                        String bioTxt = task.getResult().getString("userBio");


                        profileUserName.setText(name);
                        profileUserBio.setText(bioTxt);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.color.colorOnyx);

                        Glide.with(OtherUserProfileActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileUserImage);

                    }

                }
                else {
                    String error = task.getException().getMessage();
                    Toast.makeText(OtherUserProfileActivity.this, "(FIRESTORE Retrieve Error): " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }
}
