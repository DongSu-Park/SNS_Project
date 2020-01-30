package com.floreerin.sns_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        profileImageView = findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.btn_check).setOnClickListener(onClickListener);
    }

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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_check:
                    changeMemberInit();
                    break;
                case R.id.profileImageView:
                    gotoCamera();
                    break;
            }
        }
    };

    private void changeMemberInit() {
        final String name = ((EditText) findViewById(R.id.member_name)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.member_phone)).getText().toString();
        final String date = ((EditText) findViewById(R.id.member_date)).getText().toString();
        final String address = ((EditText) findViewById(R.id.member_address)).getText().toString();

        if (name.length() > 0 && phone.length() > 9 && date.length() > 5 && address.length() > 0) { // 입력 칸 확인 로직 후 메소드 실행
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            StorageReference storageRef = storage.getReference();

            final StorageReference mountainImagesRef = storageRef.child("images/" + user.getUid() + "/profileImage.jpg"); // 사용자 uid값을 통한 스토리지 저장

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
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.e("성공", "성공" + downloadUri);

                            FirebaseFirestore db = FirebaseFirestore.getInstance(); // Cloud Firestore (NoSQL) 초기화
                            MemberInfo memberInfo = new MemberInfo(name, phone, date, address, downloadUri.toString());
                            if (user != null) {
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
                        } else {
                            Log.e("로그", "실패");
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e("업로드 로그 ", "에러 : " + e.toString());
            }
        } else {
            startToast("회원정보를 입력해주세요");
        }
    }

    private void gotoCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 0);
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
