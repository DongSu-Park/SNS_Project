package com.floreerin.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.floreerin.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            // 현재 로그인이 안되어 있을 경우
            gotoLogin(); // 로그인 페이지로 이동
        } else {
            // 로그인 되어있을 때
            FirebaseFirestore db = FirebaseFirestore.getInstance(); // Cloud Firestore (NoSQL) 초기
            DocumentReference docRef = db.collection("users").document(user.getUid()); // 해당 회원 정보의 Uid를 가져옴
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // 해당 회원 Uid의 회원 정보 db 가져옴
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document != null){
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                            } else {
                                Log.d(TAG, "No such document");
                                startToast("회원 정보를 입력 해주세요");
                                gotoMemberInit(); // 회원 정보를 입력하는 엑티비티 이동
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
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

    private void gotoMemberInit(){
        Intent intent = new Intent(this, MemberInitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 호출한 MainActivity를 맨위의 스택으로 올리고 나머지 존재했던 액티비티는 모두 삭제
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg ,Toast.LENGTH_LONG).show();
    }
}
