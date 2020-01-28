package com.floreerin.sns_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Password_ResetActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_send).setOnClickListener(onClickListener);
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
                case R.id.btn_send:
                    send_password_reset();
                    break;

            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void send_password_reset() {
        String email = ((EditText) findViewById(R.id.reset_email)).getText().toString();

        if(email.length() > 0) { // 이메일 입력이 되어 있을 때 메소드 실행
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startToast("이메일로 비밀번호 재설정 메세지를 보냈습니다.");
                                finish();
                            }
                        }
                    });
        } else{
            startToast("이메일을 입력해주세요");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg ,Toast.LENGTH_LONG).show();
    }

}
