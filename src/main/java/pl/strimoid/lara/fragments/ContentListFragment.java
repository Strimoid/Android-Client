package pl.strimoid.lara.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import pl.strimoid.lara.R;
import pl.strimoid.lara.activities.ContentActivity;
import pl.strimoid.lara.adapters.ContentAdapter;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ContentListFragment extends ListFragment
        implements ContentAdapter.Loadable, OnRefreshListener {

    private boolean mDualPane;
    private int mCurCheckPosition = 0;
    private int mPage = 1;
    private Future<JsonObject> loading;
    private ContentAdapter mContentAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;
    private TypeFilter mTypeFilter = TypeFilter.POPULAR;

    public enum TypeFilter { POPULAR, NEW }

    public ContentListFragment() {}

    public ContentListFragment(TypeFilter typeFilter) {
        mTypeFilter = typeFilter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) view;

        // As we're using a ListFragment we create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)
                        // Here we mark just the ListView and it's Empty View as pullable
                .theseChildrenArePullable(android.R.id.list, android.R.id.empty)
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.detail);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            mTypeFilter = (TypeFilter) savedInstanceState.getSerializable("typeFilter");
        }

        mContentAdapter = new ContentAdapter(getActivity(), this);

        /*if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition);
        }*/

        setListAdapter(mContentAdapter);

        load();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
        outState.putSerializable("typeFilter", mTypeFilter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showDetails(position);

        //super.onListItemClick(l, v, position, id);
    }

    public void load() {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        Uri.Builder uriBuilder = Uri.parse("http://api.strimoid.pl/contents").buildUpon();

        switch(mTypeFilter) {
            case POPULAR:
                uriBuilder.appendQueryParameter("type", "popular"); break;
            case NEW:
                uriBuilder.appendQueryParameter("type", "new"); break;
        }

        if (mContentAdapter.getCount() > 0)
            uriBuilder.appendQueryParameter("page", String.valueOf(mPage));

        loading = Ion.with(getActivity(), uriBuilder.build().toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // this is called back onto the ui thread, no Activity.runOnUiThread or Handler.post necessary.
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error loading", Toast.LENGTH_LONG).show();
                            return;
                        }

                        JsonArray contents = result.getAsJsonArray("data");

                        // add the tweets
                        for (int i = 0; i < contents.size(); i++) {
                            mContentAdapter.add(contents.get(i).getAsJsonObject());
                        }

                        mPullToRefreshLayout.setRefreshComplete();
                        //setListShown(true);
                    }
                });

        mPage++;
    }

    @Override
    public void onRefreshStarted(View view) {
        //setListShown(false);
        mContentAdapter.clear();

        mPage = 1;
        load();
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index) {
        mCurCheckPosition = index;
        JsonObject content = mContentAdapter.getItem(index);

        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            ContentFragment cf = (ContentFragment)
                    getFragmentManager().findFragmentById(R.id.detail);
            if (cf == null || cf.getShownIndex() != index) {
                Bundle arguments = new Bundle();
                arguments.putInt("index", index);
                arguments.putString("id", content.get("_id").getAsString());
                arguments.putString("title", content.get("title").getAsString());

                if (content.has("url"))
                    arguments.putString("url", content.get("url").getAsString());

                cf = new ContentFragment();
                cf.setArguments(arguments);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.detail, cf);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ContentActivity.class);
            intent.putExtra("index", index);
            intent.putExtra("id", content.get("_id").getAsString());
            intent.putExtra("title", content.get("title").getAsString());
            intent.putExtra("url", content.get("url").getAsString());
            startActivity(intent);
        }
    }
}