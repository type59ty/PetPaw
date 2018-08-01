package apple.chen.petpaw;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import apple.chen.petpaw.utils.SaveUserPhoto;
import apple.chen.petpaw.utils.UserInformation;

public class PetInfoEdit extends AppCompatActivity {
    private static final String TAG = "PetInfo";
    private static final int ACTIVITY_NUM=0;
    private Context mContext=PetInfoEdit.this;
    private Button logoutBtn;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorage;
    private ChildEventListener mChildEventListener;
    private DatabaseReference myRef;
    private String userID;
    private Button mSetImageBtn;
    private de.hdodenhof.circleimageview.CircleImageView mPetPic;
    private static final int CAMERA_REQUEST_CODE=1;
    private Button okBtn,cancelBtn;
    private EditText petNameEdit,petAgeEdit,petWeightEdit;
    private TextView changePic;
    private String Name,Email,petName,petAge,petWeight;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info_edit);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth=FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        FirebaseUser user=mAuth.getCurrentUser();
        userID=user.getUid();

        okBtn=(Button)findViewById(R.id.ok_button);
        cancelBtn=(Button)findViewById(R.id.cancel_button);
        petNameEdit=(EditText)findViewById(R.id.petName_change);
        petAgeEdit=(EditText)findViewById(R.id.petAge_change);
        petWeightEdit=(EditText)findViewById(R.id.petWeight_change);
        changePic=(TextView)findViewById(R.id.changePic);
        mProgress=new ProgressDialog(this);
        mPetPic=(de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.profile_image);

        //Image
        mStorage= FirebaseStorage.getInstance().getReference();
        //mPetPic=(ImageView)findViewById(R.id.petPic);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
                Intent intent=new Intent(mContext,PetInfo.class);
                startActivity(intent);
                Toast.makeText(mContext,"修改成功！",Toast.LENGTH_SHORT).show();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,PetInfo.class);
                startActivity(intent);
            }
        });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,CAMERA_REQUEST_CODE);
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CAMERA_REQUEST_CODE && resultCode==RESULT_OK){
            mProgress.setMessage("上傳中...請稍候");
            mProgress.show();
            final Uri uri=data.getData();

            StorageReference filepath=mStorage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgress.dismiss();

                    @SuppressWarnings("VisibleForTests") Uri downloadUri=taskSnapshot.getDownloadUrl();
                    Picasso.with(mContext).load(downloadUri).fit().centerCrop().into(mPetPic);


                    SaveUserPhoto saveUserPhoto=new SaveUserPhoto(downloadUri.toString());
                    FirebaseUser user=mAuth.getCurrentUser();
                    myRef.child(user.getUid()).child("UserPhoto").setValue(saveUserPhoto);


                    Toast.makeText(mContext,"更換成功！",Toast.LENGTH_SHORT).show();
                    //Intent intent=new Intent(mContext,PetInfo.class);
                    //startActivity(intent);
                }
            });

        }
    }
    private void saveUserInformation(){

        String petName=petNameEdit.getText().toString().trim();
        String petAge=petAgeEdit.getText().toString().trim();
        String petWeight=petWeightEdit.getText().toString().trim();
        String name=Name;
        String email=Email;

        UserInformation userInformation=new UserInformation(name,email,petName, petWeight, petAge);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("UserInfo").setValue(userInformation);
    }

    @Override
    public void onStart() {
        super.onStart();

        //mAuth.addAuthStateListener(mAuthListener);
        final FirebaseUser user=mAuth.getCurrentUser();
        // Read from the database
        myRef.child(user.getUid()).child("UserInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInformation uInfo=dataSnapshot.getValue(UserInformation.class);
                Name=uInfo.getName();
                Email=uInfo.getEmail();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

    }

}
