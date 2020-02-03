package com.floreerin.sns_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.floreerin.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_signup).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_signup:
                    signup();
                    break;

            }
        }
    };

    private void signup() {
        String email = ((EditText) findViewById(R.id.sign_e_mail)).getText().toString();
        String password = ((EditText) findViewById(R.id.sign_password)).getText().toString();
        String passwordcheck = ((EditText) findViewById(R.id.sign_password_check)).getText().toString();

        if(email.length() > 0 && password.length() > 0 && passwordcheck.length() > 0) { // 이메일, 패스워드, 패스워드 체크 부분에 입력이 되어 있을 때 회원가입 메소드 실행
            if(password.equals(passwordcheck)){ // 패스워드 체크 후 회원가입 세션
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    startToast("회원가입을 성공했습니다.");
                                    finish(); // 회원가입 성공 후 로그인 페이지로 이동
                                } else {
                                    // If sign in fails, display a message to the user.
                                    if(task.getException() != null) {
                                        startToast(task.getException().toString());
                                    }
                                }
                            }
                        });
            } else {
                 startToast("비밀번호가 일치하지 않습니다.");
            }
        } else{
            startToast("이메일 또는 비밀번호를 입력해주세요");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg ,Toast.LENGTH_LONG).show();
    }

}
