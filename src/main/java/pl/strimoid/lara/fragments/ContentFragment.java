package pl.strimoid.lara.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import pl.strimoid.lara.R;

public class ContentFragment extends Fragment {

    private String mUrl;

    public ContentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mUrl = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_detail, container, false);

        getActivity().setProgressBarVisibility(true);

        WebView wv = (WebView) view.findViewById(R.id.web_view);
        wv.getSettings().setJavaScriptEnabled(true);

        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (getActivity() == null)
                    return;

                getActivity().setProgress(progress * 100);

                if (progress == 100)
                    getActivity().setProgressBarVisibility(false);
            }
        });

        wv.loadUrl(mUrl);

        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }
}
