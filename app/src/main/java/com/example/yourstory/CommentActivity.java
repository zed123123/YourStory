package com.example.yourstory;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText commentField;
    private ImageView commentPostBtn;
    private RecyclerView comment_List;
    private CommentRecyclerAdapter commentsRecyclerAdapter;
    private List<Comment> commentsList;
    private List<User> user_list;



    private String blog_post_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastVisible;


    private  String currentUserId;
    private Boolean isFirstPageFirstLoad = true;

    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Bình luận");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_onyx);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        commentField = findViewById(R.id.comment_editText);
        commentPostBtn = findViewById(R.id.comment_send_btn);
        comment_List = findViewById(R.id.comment_list);
        mSwipeRefreshLayout = findViewById(R.id.comment_swipe_to_refresh);


        blog_post_id = getIntent().getStringExtra("blog_post_id");


        // RecyclerView Firebase List
        commentsList = new ArrayList<>();
        user_list = new ArrayList<>();
        commentsRecyclerAdapter = new CommentRecyclerAdapter(commentsList, user_list, blog_post_id);
        comment_List.setHasFixedSize(true);
        comment_List.setLayoutManager(new LinearLayoutManager(this));
        comment_List.setAdapter(commentsRecyclerAdapter);




        loadComments();







//        comment_List.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//
//                Boolean reachBottom = !recyclerView.canScrollVertically(1);
//
//                if(reachBottom){
//                    loadMoreComments();
//                }
//            }
//        });





        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message = commentField.getText().toString();

                if(!comment_message.isEmpty()){

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_message);
                    commentMap.put("userId", currentUserId);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                            .add(commentMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){

                                Toast.makeText(CommentActivity.this, "Cannot post comment: " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }
                            else{
                                commentField.setText("");
                                commentsList.clear();
                                user_list.clear();
                                loadComments();

                            }
                        }
                    });

                }

            }
        });



        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadComments();
            }
        });
    }



    private void loadComments() {

        commentsList.clear();
        user_list.clear();

        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){

                                    String commentId = doc.getDocument().getId();

                                    final Comment comments = doc.getDocument().toObject(Comment.class).withId(commentId);

                                    String commentUserId = doc.getDocument().getString("userId");
                                    firebaseFirestore.collection("Users").document(commentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                User user = task.getResult().toObject(User.class);

                                                if(isFirstPageFirstLoad){
                                                    commentsList.add(comments);
                                                    user_list.add(user);
                                                }
                                                else{
                                                    commentsList.add(0, comments);
                                                    user_list.add(0, user);
                                                }
                                                commentsRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }

                            }
                            isFirstPageFirstLoad = false;
                        }
                    }
                });

        mSwipeRefreshLayout.setRefreshing(false);
    }


//    private void loadComments() {
//
//        Query firstQuery = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(10);
//
//        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//
//                if(!queryDocumentSnapshots.isEmpty()){
//                    if(isFirstPageFirstLoad){
//
//                        // Lưu lại comment cuối cùng được hiển thị
//                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
//                        commentsList.clear();
//                        user_list.clear();
//                    }
//
//                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
//                        if(doc.getType() == DocumentChange.Type.ADDED){
//
//                            String commentId = doc.getDocument().getId();
//                            final Comment comment = doc.getDocument().toObject(Comment.class).withId(commentId);
//
//                            String commentUserId = doc.getDocument().getString("userId");
//                            firebaseFirestore.collection("Users").document(commentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                    if(task.isSuccessful()){
//
//                                        User user = task.getResult().toObject(User.class);
//
//                                        if(isFirstPageFirstLoad){
//                                            commentsList.add(comment);
//                                            user_list.add(user);
//                                        }
//                                        else{
//                                            commentsList.add(0, comment);
//                                            user_list.add(0, user);
//                                        }
//                                        commentsRecyclerAdapter.notifyDataSetChanged();
//                                    }
//                                }
//                            });
//
//                        }
//
//                    }
//                    isFirstPageFirstLoad = false;
//                }
//            }
//        });
//    }
//
//    private void loadMoreComments() {
//        Query nextQuery = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(10);
//
//        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                if(!queryDocumentSnapshots.isEmpty()){
//                    if(isFirstPageFirstLoad){
//                        // Lưu lại comment cuối cùng được hiển thị
//                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
//                    }
//
//                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
//                        if(doc.getType() == DocumentChange.Type.ADDED){
//
//                            String commentId = doc.getDocument().getId();
//                            final Comment comment = doc.getDocument().toObject(Comment.class).withId(commentId);
//
//
//                            String commentUserId = doc.getDocument().getString("userId");
//                            firebaseFirestore.collection("Users").document(commentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if(task.isSuccessful()){
//                                        User user = task.getResult().toObject(User.class);
//
//
//                                        commentsList.add(comment);
//                                        user_list.add(user);
//
//
//                                        commentsRecyclerAdapter.notifyDataSetChanged();
//                                    }
//                                }
//                            });
//
//                        }
//
//                    }
//                    isFirstPageFirstLoad = false;
//                }
//            }
//        });
//    }


    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }



}
