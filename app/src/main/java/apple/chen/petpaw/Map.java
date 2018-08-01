package apple.chen.petpaw;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import apple.chen.petpaw.utils.BottomNavigationViewHelper;
import apple.chen.petpaw.utils.LoginTime;
import apple.chen.petpaw.utils.RunMapInformationToday;
import apple.chen.petpaw.utils.RunMapInformationTotal;

import static apple.chen.petpaw.R.id.map;


public class Map extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener  {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int ACTIVITY_NUM = 1;
    private static final int REQUEST_LOCATION = 0;
    private static final long INTERVAL = 1000 * 5 * 1; //8 sec
    private static final long FASTEST_INTERVAL = 1000 * 5 * 1; // 8 sec
    private static final float SMALLEST_DISPLACEMENT = 0.25F; //quarter of a meter
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private static final String TAG = "";
    private Context mContext = Map.this;
    private GoogleMap mMap;
    private int markerCount;
    private ArrayList<LatLng> points;
    //private ArrayList<LatLng> list=new ArrayList<>();
    private Button startBtn, pauseBtn,resumeBtn,finishBtn;
    Polyline line;
    boolean isClick;
    private TextView text_distance, timer;
    private int howmanytimes;
    private int tsec = 0, csec = 0, cmin = 0;
    private boolean startflag = false;
    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private float Todaydistance,Totaldistance,totaldistance,speed,TotalSpeed;
    private int Todaycount,Totalcount,totalcount;
    private int loginDate,loginMonth,loginYear;
    private int TodayTime,TotalTime;

    public Map() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starting.");
        setContentView(R.layout.activity_map);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        setupBottomNavigationView();
        //Create The MapView Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        text_distance = (TextView) findViewById(R.id.text_distance);
        timer = (TextView) findViewById(R.id.timer);

        markerCount = 0;
        points = new ArrayList<LatLng>();

        //Check If Google Services Is Available
        if (getServicesAvailable()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
            // Toast.makeText(this, "Google Service Is Available!!", Toast.LENGTH_SHORT).show();
        }
        startBtn = (Button) findViewById(R.id.startBtn);
        pauseBtn = (Button) findViewById(R.id.stoptBtn);
        resumeBtn=(Button)findViewById(R.id.resumeBtn);
        finishBtn=(Button)findViewById(R.id.finishBtn);

        //宣告timer
        Timer timer01 = new Timer();
        //設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
        timer01.schedule(task, 0, 1000);

        startBtn.setOnClickListener(listener);
        pauseBtn.setOnClickListener(listener);
        resumeBtn.setOnClickListener(listener);
        finishBtn.setOnClickListener(listener);

        //讀取日期時間
        Calendar mCalendar = new GregorianCalendar();
        loginDate=mCalendar.get(Calendar.DAY_OF_MONTH);
        loginMonth=mCalendar.get(Calendar.MONTH);
        loginYear=mCalendar.get(Calendar.YEAR);

        LoginTime mLoginTime=new LoginTime(loginDate,loginMonth,loginYear);
        myRef.child(user.getUid()).child("LoginTime").setValue(mLoginTime);

        //自訂toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.tabs);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_map,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.connect:
                Intent intent=new Intent(mContext,Connect.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * BottomNavigationView setup
     */
    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView:setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    /**
     * GOOGLE MAPS AND MAPS OBJECTS
     */

    // After Creating the Map Set Initial Location
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Uncomment To Show Google Location Blue Pointer
        mMap.setMyLocationEnabled(true);
    }


    Marker mk = null;

    // Add A Map Pointer To The MAp
    public void addMarker(GoogleMap googleMap, double lat, double lon) {

        if (markerCount == 1) {
            animateMarker(mLastLocation, mk);
        } else if (markerCount == 0) {
            //Set Custom BitMap for Pointer
            int height = 50;
            int width = 50;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_dog);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap = googleMap;

            LatLng latlong = new LatLng(lat, lon);
            mk = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin3))
                    .icon(BitmapDescriptorFactory.fromBitmap((smallMarker))));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16));

            //Set Marker Count to 1 after first marker is created
            markerCount = 1;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            //mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        //Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }
    public boolean getServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    /**
     * LOCATION LISTENER EVENTS
     */

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        final FirebaseUser user=mAuth.getCurrentUser();

        myRef.child(user.getUid()).child("RunMapToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunMapInformationToday runMapInformationToday = dataSnapshot.getValue(RunMapInformationToday.class);
                Todaycount = runMapInformationToday.getCount();
                Todaydistance = runMapInformationToday.getDistance();
                TodayTime=runMapInformationToday.getTime();
                speed=runMapInformationToday.getSpeed();
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
                Totalcount= runMapInformationTotal.getTotalcount();
                Totaldistance = runMapInformationTotal.getTotaldistance();
                TotalTime=runMapInformationTotal.getTotaltime();
                TotalSpeed=runMapInformationTotal.getTotalspeed();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getServicesAvailable();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //Method to display the location on UI
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {


            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                String loc = "" + latitude + " ," + longitude + " ";
                //Toast.makeText(this,loc, Toast.LENGTH_SHORT).show();

                //Add pointer to the map at location
                addMarker(mMap, latitude, longitude);


            } else {

                Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Creating location request object
    protected void createLocationRequest() {
        /*mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(AppConstants.DISPLACEMENT);*/
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT); //added
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    //Starting the location updates
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    //Stopping location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    public void onLocationChanged(final Location location) {
        // Assign the new location
        mLastLocation = location;
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Add pointer to the map at location

        //Toast.makeText(getApplicationContext(), "Location changed!",
        //      Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        displayLocation();
        if (isClick == true) {
            points.add(latLng);
            redrawLine();
            //text_distance.setText(String.valueOf(calculateMiles()));
            text_distance.setText(String.format("%.2f", calculateMiles()));
        } else {
        }
    }

    /***
     *   Handling line and calculating distance
     */
    private void redrawLine() {
        PolylineOptions options = new PolylineOptions().width(10).color(Color.RED).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options); //add Polyline
    }

    protected float calculateMiles() {
        float totalDistance = 0;
        for (int i = 1; i < line.getPoints().size(); i++) {
            Location currLocation = new Location("this");
            currLocation.setLatitude(line.getPoints().get(i).latitude);
            currLocation.setLongitude(line.getPoints().get(i).longitude);

            Location lastLocation = new Location("this");
            lastLocation.setLatitude(line.getPoints().get(i - 1).latitude);
            lastLocation.setLongitude(line.getPoints().get(i - 1).longitude);

            totalDistance += lastLocation.distanceTo(currLocation);
        }
        return totalDistance / 1000;
        //return (int) (Math.floor(totalDistance*100)/100.0);
    }

    /***
     *   Handling timer
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    csec = tsec % 60;
                    cmin = tsec / 60;
                    String s = "";
                   /* if (tsec%5==0){
                        if (startflag){
                            //int[] time=new int[]{tsec};
                            //float[] distance=new float[]{calculateMiles()};
                            SharedPreferences sharedPref=getSharedPreferences("graphInfo",mContext.MODE_PRIVATE);
                            SharedPreferences.Editor editor=sharedPref.edit();
                            editor.putInt("currentTime",tsec);
                            editor.putFloat("currentDistance",calculateMiles());
                            editor.apply();

                        }
                    }*/
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
                    timer.setText(s);
                    break;
            }
        }
    };
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (startflag) {
                //如果startflag是true則每秒tsec+1
                tsec++;
                Message message = new Message();
                //傳送訊息1
                message.what = 1;
                handler.sendMessage(message);
                //TodayTime=tsec;
            }
        }
    };

    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }

        }
    }

    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.startBtn:
                    pauseBtn.setVisibility(View.VISIBLE);
                    startBtn.setVisibility(View.INVISIBLE);
                    displayLocation();
                    startflag = true;
                    isClick = true;
                    Toast.makeText(mContext,"動起來！",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.stoptBtn:
                    resumeBtn.setVisibility(View.VISIBLE);
                    finishBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.INVISIBLE);
                    startflag = false;
                    isClick = false;
                    break;
                case R.id.resumeBtn:
                    resumeBtn.setVisibility(View.INVISIBLE);
                    finishBtn.setVisibility(View.INVISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    startflag = true;
                    isClick = true;
                    break;
                case R.id.finishBtn:
                    resumeBtn.setVisibility(View.INVISIBLE);
                    finishBtn.setVisibility(View.INVISIBLE);
                    startBtn.setVisibility(View.VISIBLE);
                    isClick=false;
                    startflag=false;
                    howmanytimes++;
                    saveTodayRunMapInformation();
                    saveTotalRunMapInformation();
                    Toast.makeText(mContext,"已儲存運動！",Toast.LENGTH_SHORT).show();
                    //TextView 初始化
                    tsec=0;
                    points.clear();
                    text_distance.setText("0.00");
                    timer.setText("00:00");
                    mMap.clear();
            }
        }
    };

    private void saveTodayRunMapInformation(){
        int time=TodayTime+tsec;
        float distance=calculateMiles();
        distance=Todaydistance+distance;
        int count=Todaycount+1;
        float speed=distance/tsec;

        RunMapInformationToday runMapInformationToday =new RunMapInformationToday(time,distance,count,speed);
        //UserInformation petInformation=new UserInformation(petName, petWeight, petAge);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunMapToday").setValue(runMapInformationToday);

    }
    private void saveTotalRunMapInformation(){
        float distance=calculateMiles();
        int totaltime=TotalTime+tsec;
        totaldistance= Totaldistance+distance;
        totalcount=Totalcount+1;
        TotalSpeed=totaldistance/totaltime;


        RunMapInformationTotal runMapInformationTotal=new RunMapInformationTotal(totaltime,totaldistance,totalcount,TotalSpeed);

        FirebaseUser user=mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunMapTotal").setValue(runMapInformationTotal);
    }
}

