package apple.chen.petpaw.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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

import apple.chen.petpaw.PetInfo;
import apple.chen.petpaw.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Context mContext=LoginActivity.this;
    private FirebaseAuth mAuth;
    private TextInputLayout accoutLayout;
    private TextInputLayout passwordLayout;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private FirebaseUser user;
    private TextView signup;
    //CallbackManager callbackManager;
    //private AccessToken accessToken;
    private ProgressBar progressBar;
    Handler handler;
    private FirebaseAuth.AuthStateListener mAuthListener;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化FacebookSdk，記得要放第一行，不然setContentView會出錯
        //FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        initView();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent=new Intent();
        intent.setClass(LoginActivity.this,PetInfo.class);
        startActivity(intent);
        finish();

        // Pass the activity result back to the Facebook SDK
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        accoutLayout = (TextInputLayout) findViewById(R.id.account_layout);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);
        accoutLayout.setErrorEnabled(true);
        loginBtn = (Button) findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(TextUtils.isEmpty(account)){
                    accoutLayout.setError(getString(R.string.plz_input_accout));
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordLayout.setError(getString(R.string.plz_input_pw));
                    return;
                }
                accoutLayout.setError("");
                passwordLayout.setError("");
                mAuth.signInWithEmailAndPassword(account, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.setClass(LoginActivity.this, PetInfo.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                                    finish();
                                } else {
                                    unsetProgressBar();
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                //按下登入 才開始轉進度條
                setProgressBar();
            }



        });
        signup=(TextView)findViewById(R.id.link_signup);
        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(mContext,SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }

        });
    }
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

  /* private void facebookLogin(){
        callbackManager = CallbackManager.Factory.create();

        //找到login button
        LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);

        //幫loginButton增加callback function
        //這邊為了方便 直接寫成inner class
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            //登入成功
            @Override
            public void onSuccess(LoginResult loginResult) {

                //accessToken之後或許還會用到 先存起來

                accessToken = loginResult.getAccessToken();

                // save accessToken to SharedPreference
                //prefUtil.saveAccessToken(accessToken);

                Log.d("FB","access token got.");

                //send request and call graph api

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {

                            //當RESPONSE回來的時候

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                //讀出姓名 ID FB個人頁面連結

                                Log.d("FB","complete");
                                Log.d("FB",object.optString("name"));
                                Log.d("FB",object.optString("link"));
                                Log.d("FB",object.optString("id"));

                                // Getting FB User Data
                                //Bundle facebookData = getFacebookData(object);

                            }
                        });

                //包入你想要得到的資料 送出request

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }
            //登入取消
            @Override
            public void onCancel() {
                // App code

                Log.d("FB","CANCEL");
            }
            //登入失敗

            @Override
            public void onError(FacebookException exception) {
                // App code
                //exception.printStackTrace();
                Log.d("FB",exception.toString());
                //deleteAccessToken();
            }
        });

    }*/
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
