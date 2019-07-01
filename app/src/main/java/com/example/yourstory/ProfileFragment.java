package com.example.yourstory;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;

    private CircleImageView profileUserImage;
    private TextView profileUserName;
    private TextView profileUserBio;
    private TextView profileUserPostsCount;
    private TextView profileUserPostsBtn;
    private int count = 0;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        profileUserImage = view.findViewById(R.id.profile_user_image);
        profileUserName = view.findViewById(R.id.profile_user_name);
        profileUserBio = view.findViewById(R.id.profile_user_bio);
        profileUserPostsCount = view.findViewById(R.id.profile_user_posts_count);
        profileUserPostsBtn = view.findViewById(R.id.profile_user_posts_btn);




        loadCurrentUserData();


        sendToPostsIntent();


        postCount();

        // Inflate the layout for this fragment
        return view;
    }

    private void postCount() {
        firebaseFirestore.collection("Posts")
                .whereEqualTo("userId", currentUserId)
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

    private void sendToPostsIntent() {

        profileUserPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userPotsIntent = new Intent(getActivity(), UserPostsActivity.class);
                userPotsIntent.putExtra("userId", currentUserId);
                startActivity(userPotsIntent);
            }
        });
    }


    private void loadCurrentUserData() {
        firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(image).into(profileUserImage);

                    }

                }
                else {
                    String error = task.getException().getMessage();
                    Toast.makeText(getActivity(), "(FIRESTORE Retrieve Error): " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void logOut() {
        mAuth.signOut();
        sendToSignUp();
    }


    private void sendToSignUp() {
        Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
        startActivity(signUpIntent);
    }

    private void sendToSetupProfile(){
        Intent setupIntent = new Intent(getActivity(), SetUpProfileActivity.class);
        setupIntent.putExtra("backBtn", "profileFragment");
        startActivity(setupIntent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.profile_edit_menu:
                sendToSetupProfile();
                return true;

            case R.id.profile_signout_menu:
                logOut();
                return true;

            default:
                return false;
        }
    }
}
