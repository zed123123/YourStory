package com.example.yourstory;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private TextView toolbarTitle;

    private String current_user_id;
    private BottomNavigationView mainBottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private BookmarkFragment bookmarkFragment;
    private FavoriteFragment favoriteFragment;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        toolbarTitle = mainToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle.setText("Trang chủ");


        mainBottomNav = findViewById(R.id.main_bottomNavigationView);
        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
        bookmarkFragment = new BookmarkFragment();
        favoriteFragment = new FavoriteFragment();


        if(mAuth.getCurrentUser() != null){



            initializeFragment();




            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                    switch (menuItem.getItemId()) {
                        case R.id.bottom_nav_home:
                            replaceFragment(homeFragment);
                            toolbarTitle.setText("Trang chủ");
                            return true;

                        case R.id.bottom_nav_bookmark:
                            replaceFragment(bookmarkFragment);
                            toolbarTitle.setText("Lưu trữ");
                            return true;

                        case R.id.bottom_nav_new_post:
                            Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                            startActivity(newPostIntent);
                            return true;

                        case R.id.bottom_nav_like:
                            replaceFragment(favoriteFragment);
                            toolbarTitle.setText("Yêu thích");
                            return true;

                        case R.id.bottom_nav_profile:
                            replaceFragment(profileFragment);
                            toolbarTitle.setText("Trang cá nhân");
                            return true;

                        default:
                            return false;
                    }
                }
            });
        }


    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if(fragment == homeFragment){

//            fragmentTransaction.detach(homeFragment);
//            fragmentTransaction.attach(homeFragment);


//
//            fragmentTransaction.detach(bookmarkFragment);
//            fragmentTransaction.detach(favoriteFragment);

            fragmentTransaction.hide(bookmarkFragment);
            fragmentTransaction.hide(favoriteFragment);
            fragmentTransaction.hide(profileFragment);
        }

        if(fragment == bookmarkFragment){

//            fragmentTransaction.detach(bookmarkFragment);
//            fragmentTransaction.attach(bookmarkFragment);



//            fragmentTransaction.detach(homeFragment);
//            fragmentTransaction.detach(favoriteFragment);

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(favoriteFragment);
            fragmentTransaction.hide(profileFragment);
        }

        if(fragment == favoriteFragment){

//            fragmentTransaction.detach(favoriteFragment);
//            fragmentTransaction.attach(favoriteFragment);


//            fragmentTransaction.detach(homeFragment);
//            fragmentTransaction.detach(bookmarkFragment);

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(bookmarkFragment);
            fragmentTransaction.hide(profileFragment);

        }

        if(fragment == profileFragment){

//            fragmentTransaction.detach(profileFragment);
//            fragmentTransaction.attach(profileFragment);


//            fragmentTransaction.detach(homeFragment);
//            fragmentTransaction.detach(bookmarkFragment);
//            fragmentTransaction.detach(favoriteFragment);

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(bookmarkFragment);
            fragmentTransaction.hide(favoriteFragment);

        }


        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, bookmarkFragment);
        fragmentTransaction.add(R.id.main_container, favoriteFragment);
        fragmentTransaction.add(R.id.main_container, profileFragment);

        fragmentTransaction.hide(profileFragment);
        fragmentTransaction.hide(bookmarkFragment);
        fragmentTransaction.hide(favoriteFragment);

        fragmentTransaction.commit();
    }


    private void sendToSignUp() {
        Intent signInIntent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(signInIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();




        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToSignUp();
        }
        else {
            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                    if(task.isSuccessful()){

                        // Nếu người dùng chưa cập nhật thông tin thì chuyển sang trang cập nhật thông tin
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetUpProfileActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }

                    }
                    else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }
    }
}
