package top.itmp.newhosts;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by hz on 2016/2/23.
 */
public class NewHostsFragment extends Fragment {
    private static TextView version;
    private static TextView output;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_hosts, container, false);
        Button reset = (Button)rootView.findViewById(R.id.reset);
        Button update = (Button)rootView.findViewById(R.id.update);
        version = (TextView)rootView.findViewById(R.id.versionInfo);
        output = (TextView)rootView.findViewById(R.id.output);
        version.setTextColor(Color.parseColor("#FF3F51B5"));
        version.setTextSize(16.0f);
        output.setTextColor(Color.parseColor("#FFFF4081"));
        output.setText(getString(R.string.helpMessage));
        output.setMovementMethod(ScrollingMovementMethod.getInstance());

        checkHostsVersionInfo(getActivity());

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*UpdateCheck updateCheck = new UpdateCheck(getActivity());
                try {
                    updateCheck.execute(new URL("https://api.github.com/repos/itmplog/hosts/commits?path=hosts.txt&page=1&per_page=1"));
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }*/
                UpdateCheck.check(getActivity(), "https://api.github.com/repos/itmplog/hosts/commits?path=hosts&page=1&per_page=1");
                checkHostsVersionInfo(getActivity());
            }
        });

        return rootView;
    }

    public static void checkHostsVersionInfo(Context context){
        File file = new File("/system/etc/hosts");
        version.setText("Last Modified Time:\n\t" + new Date(file.lastModified()));
        String checkedUpdate = PreferenceManager.getDefaultSharedPreferences(context).getString("update", null);
        if(checkedUpdate != null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            try {
                Date checked = simpleDateFormat.parse(checkedUpdate);
                version.append("\nServer Update Time:\n\t" + checked);
            }catch (ParseException e){
                e.printStackTrace();
            }
        }
    }
}
