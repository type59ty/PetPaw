package apple.chen.petpaw.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import apple.chen.petpaw.R;

/**
 * Created by apple on 2017/10/25.
 */

public class UserList extends ArrayAdapter<UserInformation> {
    private Activity context;
    private List<UserInformation> uInfoList;


    public UserList(Activity context, List<UserInformation> uInfo) {
        super(context, R.layout.activity_pet_info,uInfo);
        this.context=context;
        this.uInfoList=uInfo;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();

        View ListViewItem=inflater.inflate(R.layout.view_database_layout,null,true);
        TextView text_name=(TextView) ListViewItem.findViewById(R.id.text_name);
        TextView text_email=(TextView) ListViewItem.findViewById(R.id.text_email);
        TextView text_petName=(TextView) ListViewItem.findViewById(R.id.text_petName);
        TextView text_petWeight=(TextView) ListViewItem.findViewById(R.id.text_petWeight);
        TextView text_petAge=(TextView) ListViewItem.findViewById(R.id.text_petAge);

        UserInformation uInfo=uInfoList.get(position);

        text_email.setText(uInfo.getEmail());
        text_name.setText(uInfo.getName());
        text_petName.setText(uInfo.getPetName());
        text_petWeight.setText(uInfo.getPetWeight());
        text_petAge.setText(uInfo.getPetAge());



        return ListViewItem;
    }
}
