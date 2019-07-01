package com.example.yourstory;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UserPostsActivity extends AppCompatActivity {

    private Toolbar userPostsToolbar;
    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogPostRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private String userId;

    private Boolean isFirstPageFirstLoad = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");


        userPostsToolbar = findViewById(R.id.user_posts_toolbar);
        setSupportActionBar(userPostsToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_onyx);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blog_list_view = findViewById(R.id.user_posts_recyclerView);

        blogRecyclerAdapter = new BlogPostRecyclerAdapter(blog_list, user_list);

        blog_list_view.setLayoutManager(new LinearLayoutManager(this));
        blog_list_view.setAdapter(blogRecyclerAdapter);


        blog_list.clear();
        user_list.clear();

        loadData();
    }



    private void loadData() {


        firebaseFirestore.collection("Posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        final String blogPostId = documentSnapshot.getId();
                        final BlogPost blogPost = documentSnapshot.toObject(BlogPost.class).withId(blogPostId);

                        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<DocumentSnapshot> task2) {

                                if(task2.isSuccessful()){

                                    User user = task2.getResult().toObject(User.class);

                                    if(isFirstPageFirstLoad){
                                        blog_list.add(blogPost);
                                        user_list.add(user);
                                    }
                                    else{
                                        user_list.add(0, user);
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    isFirstPageFirstLoad = false;

                }

            }
        });


//        firebaseFirestore.collection("Posts")
//                .whereEqualTo("userId", userId)
//                .orderBy("timestamp", Query.Direction.ASCENDING)
//                .limit(25)
//                .addSnapshotListener(UserPostsActivity.this, new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                        if(!queryDocumentSnapshots.isEmpty()){
//                            for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
//                                if(documentChange.getType() == DocumentChange.Type.ADDED){
//
//                                    final String blogPostId = documentChange.getDocument().getId();
//                                    final BlogPost blogPost = documentChange.getDocument().toObject(BlogPost.class).withId(blogPostId);
//
//                                    firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull final Task<DocumentSnapshot> task2) {
//
//                                            if(task2.isSuccessful()){
//
//                                                User user = task2.getResult().toObject(User.class);
//
//                                                if(isFirstPageFirstLoad){
//                                                    blog_list.add(blogPost);
//                                                    user_list.add(user);
//                                                }
//                                                else{
//                                                    user_list.add(0, user);
//                                                    blog_list.add(0, blogPost);
//                                                }
//                                                blogRecyclerAdapter.notifyDataSetChanged();
//                                            }
//                                        }
//                                    });
//
//                                }
//                            }
//                            isFirstPageFirstLoad = false;
//                        }
//                    }
//                });
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }
}
