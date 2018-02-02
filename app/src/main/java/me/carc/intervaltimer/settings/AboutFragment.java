package me.carc.intervaltimer.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.carc.intervaltimer.BuildConfig;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.utils.Commons;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        Activity activity = getActivity();

        String versionName;
        try {
            versionName = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?.?";
        }

        if(BuildConfig.DEBUG)
            versionName += " " + BuildConfig.BUILD_TYPE;

        // add app name and version
        TextView aboutApp = view.findViewById(R.id.aboutApp);
        aboutApp.setText(getResources().getString(R.string.app_name) + "  " + versionName);

        // create real paragraphs
        TextView t = view.findViewById(R.id.aboutTextView);
        t.setText(Html.fromHtml(
                getString(R.string.about) +
                        String.format(getString(R.string.about_bottom), getString(R.string.website), getString(R.string.bugtracker))
        ));

        // make links in about text clickable
        t.setMovementMethod(LinkMovementMethod.getInstance());
        t.setLinkTextColor(ContextCompat.getColor(activity, R.color.colorAccent));

/*		Button website = (Button) view.findViewById(R.id.websiteButton);
        website.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
				startActivity(launchBrowser);
			}
		});
*/

        return view;
    }/**/

    @OnClick(R.id.websiteButton)
    void website() {
        Commons.Toast(getActivity(), R.string.website_todo, Color.RED, Toast.LENGTH_SHORT);
//        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
//        startActivity(launchBrowser);
    }

    @OnClick(R.id.shareButton)
    void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.shared_string_share)));
    }

    @OnClick(R.id.rateButton)
    void rate() {
        new SendFeedback(getActivity(), SendFeedback.TYPE_RATE);
    }

    @OnClick(R.id.feedbackButton)
    void feebback() {
        new SendFeedback(getActivity(), SendFeedback.TYPE_FEEDBACK);
    }
}