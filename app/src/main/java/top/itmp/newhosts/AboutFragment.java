package top.itmp.newhosts;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import top.itmp.newhosts.util.Utils;

/**
 * Created by hz on 2016/2/29.
 */
public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionView = (TextView)view.findViewById(R.id.versionView);
        versionView.setText(getString(R.string.app_version,
                Utils.versionInfo(getActivity())));

        // enable context clickable
        TextView aboutView = (TextView)view.findViewById(R.id.aboutTextView);
        aboutView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;

    }
}
