package apple.chen.petpaw;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Connect extends AppCompatActivity {
    private static final String TAG = "Connect";
    private Context mContext=Connect.this;
    private Button scan,cancel;
    private TextView textView;
    private ProgressDialog mProgress;
    private boolean done=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        scan=(Button)findViewById(R.id.scan_button);
        cancel=(Button)findViewById(R.id.stop_button);
        textView=(TextView)findViewById(R.id.ble);
        mProgress=new ProgressDialog(this);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setMessage("搜尋中...請稍候");
                mProgress.show();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something here
                        mProgress.dismiss();
                        done=true;
                    }
                }, 3000);

                if(done){
                    textView.setVisibility(View.VISIBLE);
                }
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(mContext,Map.class);
                startActivity(intent);
                Toast.makeText(mContext,"已連接裝置！",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
