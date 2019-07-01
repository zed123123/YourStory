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

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditTxt, pwdEditTxt, confirmPwdEditTxt;
    private Button signUpBtn;
    private TextView signInBtn;

    private ProgressBar signUpProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        emailEditTxt = findViewById(R.id.sign_up_email_edit_text);
        pwdEditTxt = findViewById(R.id.sign_up_password_edit_text);
        confirmPwdEditTxt = findViewById(R.id.sign_up_confirm_password_edit_text);
        signUpBtn = findViewById(R.id.sign_up_btn);
        signInBtn = findViewById(R.id.sign_up_sign_in_btn);
        signUpProgress = findViewById(R.id.sign_up_progress);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditTxt.getText().toString();
                String pwd = pwdEditTxt.getText().toString();
                String confirmPwd = confirmPwdEditTxt.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(confirmPwd)){
                    signUpProgress.setVisibility(View.VISIBLE);

                    if(pwd.equals(confirmPwd)){

                        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    Intent setUpProfileIntent = new Intent(SignUpActivity.this, SetUpProfileActivity.class);
                                    startActivity(setUpProfileIntent);

                                }
                                else{
                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(SignUpActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                                }

                                signUpProgress.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                    else{
                        Toast.makeText(SignUpActivity.this, "Mật khẩu và Nhập lại mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                        signUpProgress.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                    signUpProgress.setVisibility(View.INVISIBLE);
                }

            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(signInIntent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser =mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
