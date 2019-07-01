package com.example.yourstory;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditTxt, pwdEditTxt;
    private TextView signUpTxt;
    private Button signInBtn;
    private ProgressBar signInProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();

        emailEditTxt = findViewById(R.id.sign_in_email_edit_text);
        pwdEditTxt = findViewById(R.id.sign_in_password_edit_text);
        signUpTxt = findViewById(R.id.sign_in_sign_up_btn);
        signInBtn = findViewById(R.id.sign_in_btn);
        signInProgress = findViewById(R.id.sign_in_progress);


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditTxt.getText().toString();
                String pwd = pwdEditTxt.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)){

                    signInProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                            }
                            else {
                                String errorMsg =task.getException().getMessage();
                                Toast.makeText(SignInActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                            }
                            signInProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }
                else {
                    Toast.makeText(SignInActivity.this, "Email và Password không được trống", Toast.LENGTH_SHORT).show();
                }

            }
        });

        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
                finish();
            }
        });
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }
}
