package apple.chen.petpaw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import apple.chen.petpaw.Login.LoginActivity;
import apple.chen.petpaw.utils.BottomNavigationViewHelper;
import apple.chen.petpaw.utils.LoginTime;
import apple.chen.petpaw.utils.RunMapInformationToday;
import apple.chen.petpaw.utils.RunMapInformationTotal;
import apple.chen.petpaw.utils.SaveUserPhoto;
import apple.chen.petpaw.utils.UserInformation;

public class PetInfo extends AppCompatActivity {
    private static final String TAG = "PetInfo";
    private static final int ACTIVITY_NUM=0;
    private Context mContext=PetInfo.this;

    //add Firebase Database stuff
    private FirebaseStorage storage;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorage;
    private ChildEventListener mChildEventListener;
    private DatabaseReference myRef;
    private String userID;
    private ListView mListView;
    private TextView userName,userEmail;
    private ArrayAdapter adapter;
    List<UserInformation> array;
    private Button mSetImageBtn;
    private de.hdodenhof.circleimageview.CircleImageView mPetPic;
    private static final int CAMERA_REQUEST_CODE=1;
    private TextView text_petAge,text_petName,text_petWeight,text_email,text_name;
    private TextView text_today_count,text_today_distance,text_today_times,text_today_speed;
    private TextView text_total_count,text_total_distance,text_total_times,text_total_speed;
    private int loginDate,loginMonth,loginYear,currentDate,currentMonth,currentYear;
    private int tsec = 0, csec = 0, cmin = 0;
    private boolean startflag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info);
        setupBottomNavigationView();
        //StorageReference storageReference=storage.getReference();


        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth=FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        //myRef=mFirebaseDatabase.getReferenceFromUrl("https://petpaw-edc6b.firebaseio.com/");
        myRef=mFirebaseDatabase.getReference();
        FirebaseUser user=mAuth.getCurrentUser();
        userID=user.getUid();
        array=new ArrayList<>();

        //initialize
        text_petName=(TextView)findViewById(R.id.text_petName);
        text_petWeight=(TextView)findViewById(R.id.text_petWeight);
        text_email=(TextView)findViewById(R.id.text_email);
        //text_name=(TextView)findViewById(R.id.text_petName);
        text_petAge=(TextView)findViewById(R.id.text_petAge);

        text_today_count=(TextView)findViewById(R.id.text_today_count);
        text_today_distance=(TextView)findViewById(R.id.text_today_distance);
        text_today_times=(TextView)findViewById(R.id.text_today_times);
        text_today_speed=(TextView)findViewById(R.id.text_today_speed);

        text_total_count=(TextView)findViewById(R.id.text_total_count);
        text_total_distance=(TextView)findViewById(R.id.text_total_distance);
        text_total_times=(TextView)findViewById(R.id.text_total_times);
        mPetPic=(de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.profile_image);
        text_total_speed=(TextView)findViewById(R.id.text_total_speed);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

        //判斷是否過為隔天
        determineTheDay();

        Toolbar toolbar=(Toolbar)findViewById(R.id.tabs);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_petinfo,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.changeData:
                Intent intent1 = new Intent();
                intent1.setClass(mContext,PetInfoEdit.class);
                startActivity(intent1);
                break;
            case R.id.logout:
                Intent intent = new Intent();
                intent.setClass(mContext,LoginActivity.class);
                startActivity(intent);
                FirebaseAuth.getInstance().signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        final FirebaseUser user=mAuth.getCurrentUser();
        // Read from the database
        myRef.child(user.getUid()).child("UserInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                array.clear();
                UserInformation uInfo=dataSnapshot.getValue(UserInformation.class);
                array.add(uInfo);
                //UserList adapter=new UserList(PetInfo.this,array);
                //mListView.setAdapter(adapter);

                text_email.setText(uInfo.getEmail());
                //text_name.setText(uInfo.getName());
                text_petName.setText(uInfo.getPetName());
                text_petWeight.setText(uInfo.getPetWeight());
                text_petAge.setText(uInfo.getPetAge());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef.child(user.getUid()).child("RunMapToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunMapInformationToday runMapInformationToday = dataSnapshot.getValue(RunMapInformationToday.class);
                text_today_count.setText(Integer.toString(runMapInformationToday.getCount()));
                text_today_distance.setText(String.format("%.2f",runMapInformationToday.getDistance()));
                text_today_speed.setText(String.format("%.2f",runMapInformationToday.getSpeed()));

                csec = runMapInformationToday.getTime() % 60;
                cmin = runMapInformationToday.getTime() / 60;
                String s = "";
                if (cmin < 10) {
                    s = "0" + cmin;
                } else {
                    s = "" + cmin;
                }
                if (csec < 10) {
                    s = s + ":0" + csec;
                } else {
                    s = s + ":" + csec;
                }
                //s字串為00:00格式
                text_today_times.setText(s);
                //text_today_times.setText(Integer.toString(runMapInformationToday.getTime()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef.child(user.getUid()).child("RunMapTotal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunMapInformationTotal runMapInformationTotal =dataSnapshot.getValue(RunMapInformationTotal.class);
                text_total_count.setText(Integer.toString(runMapInformationTotal.getTotalcount()));
                text_total_distance.setText(String.format("%.2f",runMapInformationTotal.getTotaldistance()));
                text_total_speed.setText(String.format("%.2f",runMapInformationTotal.getTotalspeed()));


                csec = runMapInformationTotal.getTotaltime() % 60;
                cmin = runMapInformationTotal.getTotaltime() / 60;
                String s = "";
                if (cmin < 10) {
                    s = "0" + cmin;
                } else {
                    s = "" + cmin;
                }
                if (csec < 10) {
                    s = s + ":0" + csec;
                } else {
                    s = s + ":" + csec;
                }
                //s字串為00:00格式
                text_total_times.setText(s);
                //text_total_times.setText(Integer.toString(runMapInformationTotal.getTotaltime()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef.child(user.getUid()).child("UserPhoto").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SaveUserPhoto saveUserPhoto=dataSnapshot.getValue(SaveUserPhoto.class);
                Picasso.with(mContext).load(saveUserPhoto.getDownloadUrl()).fit().centerCrop().into(mPetPic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
/*
        StorageReference storageRef = storage.getReference();
        storageRef.child("users/me/profile.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                generatedFilePath = downloadUri.toString(); /// The string(file link) that you need
            }
        });*/

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /**
     * BottomNavigationView setup
     */
    public void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView:setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void determineTheDay(){
        //讀取日期時間
        Calendar mCalendar = new GregorianCalendar();
        loginDate=mCalendar.get(Calendar.DAY_OF_MONTH);
        loginMonth=mCalendar.get(Calendar.MONTH);
        loginYear=mCalendar.get(Calendar.YEAR);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("LoginTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoginTime mLoginTime =dataSnapshot.getValue(LoginTime.class);
                currentDate=mLoginTime.getLoginDate();
                currentMonth=mLoginTime.getLoginMonth();
                currentYear=mLoginTime.getLoginYear();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        //過一天後 歸零
        if(loginDate!=currentDate){
            // || loginMonth!=currentMonth || loginYear!=currentYear
            /*int time=0;
            float distance=0;
            int count=0;
            RunMapInformationToday runMapInformationToday=new RunMapInformationToday(time,distance,count);
            myRef.child(user.getUid()).child("RunMapToday").setValue(runMapInformationToday);*/
        }

    }
}
