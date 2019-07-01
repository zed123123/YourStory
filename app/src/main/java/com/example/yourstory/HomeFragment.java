package com.example.yourstory;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogPostRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private SwipeRefreshLayout mSwipeRefreshLayout;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        mSwipeRefreshLayout = view.findViewById(R.id.home_swipe_to_refresh);

        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.home_recyclerView);

        blogRecyclerAdapter = new BlogPostRecyclerAdapter(blog_list, user_list);

        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if(firebaseAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();

//            blog_list.clear();
//            user_list.clear();

            loadData();



//            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//
//
//                    Boolean reachBottom = !recyclerView.canScrollVertically(1);
//
//                    if(reachBottom){
//                        loadMorePosts();
//                    }
//                }
//            });




        }




        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadData();
            }
        });





        // Inflate the layout for this fragment
        return view;
    }

    private void loadData() {

        blog_list.clear();
        user_list.clear();

        // Hiển thị post theo thứ tự, giới hạn số lượng post hiển thị
        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.ASCENDING);
//                .limit(5);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()){
                    if(isFirstPageFirstLoad){

                        // Lưu lại post cuối cùng được hiển thị
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        blog_list.clear();
                        user_list.clear();
                    }

                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            String blogPostId = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            String blogUserId = doc.getDocument().getString("userId");
                            firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if(task.isSuccessful()){

                                        User user = task.getResult().toObject(User.class);

                                        if(isFirstPageFirstLoad){
                                            blog_list.add(blogPost);
                                            user_list.add(user);
                                        }
                                        else {
                                            user_list.add(0, user);
                                            blog_list.add(0, blogPost);
                                        }

                                        blogRecyclerAdapter.notifyDataSetChanged();
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

    private void loadMorePosts() {
        if(firebaseAuth.getCurrentUser() != null){
            // Hiển thị post theo thứ tự
            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(5);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(!queryDocumentSnapshots.isEmpty()){

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);

                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);


                                String blogUserId = doc.getDocument().getString("userId");
                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if(task.isSuccessful()){

                                            User user = task.getResult().toObject(User.class);

                                            blog_list.add(blogPost);
                                            user_list.add(user);


                                            blogRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                            }

                        }
                    }
                }
            });
        }
    }

}
