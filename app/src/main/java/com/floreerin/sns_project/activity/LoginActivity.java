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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_login).setOnClickListener(onClickListener);
        findViewById(R.id.btn_gotosignup).setOnClickListener(onClickListener);
        findViewById(R.id.btn_pwdreset).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() { // 뒤로가기 버튼 누를 시 종료
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_login:
                    startLogin();
                    break;

                case R.id.btn_gotosignup:
                    gotoSignup();
                    break;

                case R.id.btn_pwdreset:
                    gotoPwdReset();
                    break;
            }
        }
    };



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void startLogin() {
        String email = ((EditText) findViewById(R.id.login_e_mail)).getText().toString();
        String password = ((EditText) findViewById(R.id.login_password)).getText().toString();

        if(email.length() > 0 && password.length() > 0) { // 이메일, 패스워드, 입력이 되어 있을 때 로그인 메소드 실행
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                startToast("로그인에 성공했습니다.");
                                gotoMainActivity();
                            } else {
                                startToast(task.getException().toString());
                            }
                        }
                    });
        } else{
            startToast("이메일 또는 비밀번호를 입력해주세요");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg ,Toast.LENGTH_LONG).show();
    }

    private void gotoMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 호출한 MainActivity를 맨위의 스택으로 올리고 나머지 존재했던 액티비티는 모두 삭제
        startActivity(intent);
        finish();
    }

    private void gotoSignup(){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void gotoPwdReset() {
        Intent intent = new Intent(this, Password_ResetActivity.class);
        startActivity(intent);
    }
}
