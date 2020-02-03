package com.floreerin.sns_project.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.floreerin.sns_project.MemberInfo;
import com.floreerin.sns_project.R;
import com.floreerin.sns_project.activity.CameraActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MemberInitActivity extends AppCompatActivity {
    private static final String TAG = "MemberInitActivity";
    private ImageView profileImageView;
    private String returnPath;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        profileImageView = findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.btn_check).setOnClickListener(onClickListener);
        findViewById(R.id.btn_camera).setOnClickListener(onClickListener);
        findViewById(R.id.btn_gallery).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_check:
                    changeMemberInit();
                    break;
                case R.id.profileImageView:
                    CardView cardView = findViewById(R.id.cardview_showbtn);
                    if (cardView.getVisibility() == View.VISIBLE){
                        cardView.setVisibility(View.GONE);
                    }else{
                        cardView.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.btn_camera:
                    gotoCamera();
                    break;
                case R.id.btn_gallery:
                    gotoGallery();
                    break;
            }
        }
    };



    @Override
    public void onBackPressed() { // 뒤로가기 버튼 누를 시
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                returnPath = data.getStringExtra("profilePath");
                Log.e("파일 위치 로그 : ", returnPath);

                File file = new File(returnPath);

                if (file != null) {
                    try {
                        // 비트맵 이미지로 가져오기 (이거 좀 수정하자...)
                        ImageDecoder.Source source = ImageDecoder.createSource(file);
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                        // 이미지를 상황에 맞게 회전
                        ExifInterface ei = new ExifInterface(returnPath);
                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int Degree = OrientationToDegrees(orientation);
                        bitmap = rotate(bitmap, Degree);

                        // Bitmap bmp = BitmapFactory.decodeFile(returnPath);
                        profileImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("로그", "오류 : " + e.toString());
                    }
                }
            }
        }
    }

    private void changeMemberInit() { // 프로필 정보를 Firebase DB에 저장하는 메소드
        final String name = ((EditText) findViewById(R.id.member_name)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.member_phone)).getText().toString();
        final String date = ((EditText) findViewById(R.id.member_date)).getText().toString();
        final String address = ((EditText) findViewById(R.id.member_address)).getText().toString();

        if (name.length() > 0 && phone.length() > 9 && date.length() > 5 && address.length() > 0) { // 입력 칸 확인 로직 후 메소드 실행
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            user = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference mountainImagesRef = storageRef.child("images/" + user.getUid() + "/profileImage.jpg"); // 사용자 uid값을 통한 스토리지 저장

            if (returnPath == null){ // 프로필 사진이 없을 경우 정보만 보냄
                MemberInfo memberInfo = new MemberInfo(name, phone, date, address);
                firebaseUploader(memberInfo);

            } else{ // 프로필 사진이 있을경우 같이 보냄
                try {
                    InputStream stream = new FileInputStream(new File(returnPath));
                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return mountainImagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) { // 내장 경로인 returnPath의 사진이 Firebase Storage에 업로드 완료 후 메소드
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult(); // 해당 URL 경로를 가져옴
                                MemberInfo memberInfo = new MemberInfo(name, phone, date, address, downloadUri.toString()); // 이름, 전화번호, 생년월일, 주소, 프로필 사진 경로(firebase URL) 정보 보냄
                                firebaseUploader(memberInfo); // 해당 정보를 FIrebase DB에 저장
                                Log.e(TAG, "이미지 업로드 성공 : " + downloadUri);
                            } else {
                                Log.e(TAG, "이미지 업로드 실패");
                            }
                        }
                    });
                } catch (FileNotFoundException e) { // 파일 경로 오류
                    Log.e(TAG, "업로드 에러 : " + e.toString());
                }
            }
        } else { // 입력 칸 확인 로직 오류
            startToast("회원정보를 입력해주세요");
        }
    }

    private void firebaseUploader(MemberInfo memberInfo) { // Firebase DB Uploader 메소드 실행
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Cloud Firestore (NoSQL) 초기화
        if (user != null) {
            db.collection("users").document(user.getUid()).set(memberInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startToast("회원정보 등록을 성공하였습니다.");
                            Log.d(TAG, "Firebase DB 등록 성공");
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            startToast("회원정보 등록에 실패하였습니다.");
                            Log.w(TAG, "Firebase DB 등록 실패 : ", e);
                        }
                    });
        }
    }

    private void gotoCamera() { // 프로필 사진 촬영 인텐트 실행 메소드
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 0);
    }

    private void gotoGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public int OrientationToDegrees(int orientation){
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }
        else if(orientation == ExifInterface.ORIENTATION_ROTATE_180){
            return 180;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        } else{
            return 0;
        }
    }

    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }
}
