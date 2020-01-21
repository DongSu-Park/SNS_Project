package com.floreerin.sns_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            // 현재 로그인이 안되어 있을 경우
            gotoLogin();
        } else {
            // 로그인 되어있을 때
            for (UserInfo profile : user.getProviderData()) {
                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                if (name != null){
                    if (name.length() == 0) { // 회원정보를 파이어베이스에서 가져왔을 때 name 값이 없을 경우 회원 정보 수정 페이지로 이동
                        Toast.makeText(this, "회원 정보를 입력 해주세요", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, MemberInitActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 호출한 MainActivity를 맨위의 스택으로 올리고 나머지 존재했던 액티비티는 모두 삭제
                        startActivity(intent);
                        finish();
                    }
                }

            }
        }

        findViewById(R.id.btn_logout).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) { //' 로그아웃 버튼 누를 시
            switch (v.getId()){
                case R.id.btn_logout:
                    FirebaseAuth.getInstance().signOut();
                    gotoLogin();
                    break;
            }
        }
    };

    private void gotoLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 호출한 MainActivity를 맨위의 스택으로 올리고 나머지 존재했던 액티비티는 모두 삭제
        startActivity(intent);
        finish();
    }
}
