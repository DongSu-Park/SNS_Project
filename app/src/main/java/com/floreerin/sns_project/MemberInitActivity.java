package com.floreerin.sns_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MemberInitActivity extends AppCompatActivity {
    private static final String TAG = "MemberInitActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        findViewById(R.id.btn_check).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() { // 뒤로가기 버튼 누를 시
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_check:
                    changeMemberInit();
                    break;
            }
        }
    };

    private void changeMemberInit() {
        String name = ((EditText) findViewById(R.id.member_name)).getText().toString();
        String phone = ((EditText) findViewById(R.id.member_phone)).getText().toString();
        String date = ((EditText) findViewById(R.id.member_date)).getText().toString();
        String address = ((EditText) findViewById(R.id.member_address)).getText().toString();

        if(name.length() > 0 && phone.length() > 9 && date.length() > 5 && address.length() > 0)  { // 입력 칸 확인 로직 후 메소드 실행
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore db = FirebaseFirestore.getInstance(); // Cloud Firestore (NoSQL) 초기화

            MemberInfo memberInfo = new MemberInfo(name,phone,date,address);
            if (user != null){
                db.collection("users").document(user.getUid()).set(memberInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startToast("회원정보 등록을 성공하였습니다.");
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                startToast("회원정보 등록에 실패하였습니다.");
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }


        } else{
            startToast("회원정보를 입력해주세요");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg ,Toast.LENGTH_LONG).show();
    }

}
