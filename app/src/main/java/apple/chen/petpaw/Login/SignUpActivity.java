package apple.chen.petpaw.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import apple.chen.petpaw.R;
import apple.chen.petpaw.utils.LoginTime;
import apple.chen.petpaw.utils.RunMapInformationTotal;
import apple.chen.petpaw.utils.SaveUserPhoto;
import apple.chen.petpaw.utils.UserInformation;

/**
 * Created by apple on 2017/10/19.
 */

public class SignUpActivity extends AppCompatActivity{
    private static final String TAG = "SignUpActivity";
    private Context mContext=SignUpActivity.this;
    private TextInputLayout nameLayout;
    private TextInputLayout mailLayout;
    private TextInputLayout sign_passwordLayout;
    //UserInfo
    private EditText mailEdit;
    private EditText sign_passwordEdit;
    private EditText nameEdit;
    private EditText pet_name_edit;
    private EditText pet_weight_edit;
    private EditText pet_age_edit;

    private Button signUpBtn;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private String userID;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private Button btn;
    private int loginDate,loginMonth,loginYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initView();
    }


    private void initView() {
        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        //userID = user.getUid();

        /*mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };*/
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "onDataChange: Added information to database: \n" +
                        dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        nameEdit=(EditText)findViewById(R.id.name_edit);
        mailEdit=(EditText) findViewById(R.id.mail_edit);
        pet_name_edit=(EditText) findViewById(R.id.pet_name_edit);
        pet_weight_edit=(EditText) findViewById(R.id.pet_weight_edit);
        pet_age_edit=(EditText) findViewById(R.id.pet_age_edit);

        sign_passwordEdit=(EditText) findViewById(R.id.sign_password_edit);

        nameLayout=(TextInputLayout)findViewById(R.id.name_layout);
        mailLayout=(TextInputLayout) findViewById(R.id.mail_layout);
        sign_passwordLayout=(TextInputLayout) findViewById(R.id.sign_password_layout);

        nameLayout.setErrorEnabled(true);
        sign_passwordLayout.setErrorEnabled(true);
        mailLayout.setErrorEnabled(true);


        signUpBtn=(Button) findViewById(R.id.signup_button);
        signUpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String email = mailEdit.getText().toString();
                String password = sign_passwordEdit.getText().toString();

                Log.d(TAG, "onClick: Attempting to submit to database: \n" +
                        "name: " + name + "\n" +
                        "email: " + email + "\n"
                );

                if (TextUtils.isEmpty(name)) {
                    nameLayout.setError("請輸入名字");
                    sign_passwordLayout.setError("");
                    mailLayout.setError("");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    mailLayout.setError("請輸入信箱");
                    nameLayout.setError("");
                    sign_passwordLayout.setError("");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    nameLayout.setError("");
                    mailLayout.setError("");
                    sign_passwordLayout.setError("請輸入密碼");
                    return;
                }
                nameLayout.setError("");
                mailLayout.setError("");
                sign_passwordLayout.setError("");
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    saveUserInformation();
                                    initializeRunMapTotalInformation();
                                    initializeRunMapTodayInformation();
                                    initLoginTime();
                                    initUserPhoto();
                                    Toast.makeText(SignUpActivity.this,"註冊成功",Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent();
                                    intent.setClass(SignUpActivity.this,LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    unsetProgressBar();
                                    Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


                setProgressBar();

            }
        });

    }


    private void saveUserInformation(){
        String name=nameEdit.getText().toString().trim();
        String email=mailEdit.getText().toString().trim();
        String petName=pet_name_edit.getText().toString().trim();
        String petWeight=pet_weight_edit.getText().toString().trim();
        String petAge=pet_age_edit.getText().toString().trim();

        UserInformation userInformation=new UserInformation(name,email,petName, petWeight, petAge);
        //UserInformation petInformation=new UserInformation(petName, petWeight, petAge);


        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("UserInfo").setValue(userInformation);
    }
    public void initializeRunMapTodayInformation(){
        int time=0;
        float distance=0;
        int count=0;
        float speed=0;
        RunMapInformationTotal runMapInformationTotal=new RunMapInformationTotal(time,distance,count,speed);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunMapToday").setValue(runMapInformationTotal);
    }
    public void initializeRunMapTotalInformation(){
        int totaltime=0;
        float totaldistance=0;
        int totalcount=0;
        float totalspeed=0;
        RunMapInformationTotal runMapInformationTotal=new RunMapInformationTotal(totaltime,totaldistance,totalcount,totalspeed);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunMapTotal").setValue(runMapInformationTotal);
    }
    public void initUserPhoto(){
        String downloadUrl="https://firebasestorage.googleapis.com/v0/b/petpaw-edc6b.appspot.com/o/default%2Fdog.png?alt=media&token=f8606fc8-0c7e-44bf-adfa-209d650e3c8b";
        SaveUserPhoto saveUserPhoto=new SaveUserPhoto(downloadUrl);
        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("UserPhoto").setValue(saveUserPhoto);
    }
    private void initLoginTime(){

        //初始化日期時間
        loginDate=0;
        loginMonth=0;
        loginYear=0;
        LoginTime mLoginTime=new LoginTime(loginDate,loginMonth,loginYear);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("LoginTime").setValue(mLoginTime);
    }

    private void setProgressBar(){
        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                progressBar=(ProgressBar)findViewById(R.id.login_progressbar);
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        Handler handler = new Handler(getMainLooper());
        handler.post(runnable);
    }
    private void unsetProgressBar(){
        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                progressBar=(ProgressBar)findViewById(R.id.login_progressbar);
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        Handler handler = new Handler(getMainLooper());
        handler.post(runnable);
    }


}
