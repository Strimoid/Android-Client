package pl.strimoid.lara.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import pl.strimoid.lara.adapters.EntryAdapter;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class EntryListFragment extends ListFragment implements EntryAdapter.Loadable, OnRefreshListener {

    int mCurCheckPosition = 0;
    int mPage = 1;
    Future<JsonObject> mLoading;
    EntryAdapter mEntryAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;

    public EntryListFragment() {}

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

        if (savedInstanceState != null)
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);

        mEntryAdapter = new EntryAdapter(getActivity(), this);

        setListAdapter(mEntryAdapter);

        load();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onRefreshStarted(View view) {
        //setListShown(false);
        mEntryAdapter.clear();

        mPage = 1;
        load();
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        // Show all replies when proper button clicked
        if (mEntryAdapter.getItemViewType(position) == EntryAdapter.TYPE_SHOW_MORE_BUTTON)
            showAllReplies(position, view);
    }

    private void showAllReplies(int position, View view) {
        JsonObject button = mEntryAdapter.getItem(position);
        JsonObject entry = null;
        int entryPosition = 0;

        // Find parent entry
        for (int i = position - 1; i >= 0; i--) {
            if (!mEntryAdapter.getItem(i).has("is_reply")) {
                entry = mEntryAdapter.getItem(i);
                entryPosition = i;
                break;
            }
        }

        JsonArray replies = entry.get("replies").getAsJsonArray();

        // Insert replies just after parent
        for (int i = 0; i < replies.size() - 2; i++) {
            JsonObject reply = replies.get(i).getAsJsonObject();
            reply.addProperty("is_reply", true);
            mEntryAdapter.insert(reply, entryPosition + 1 + i);
        }

        mEntryAdapter.remove(button);
    }

    public void load() {
        // don't attempt to load more if a load is already in progress
        if (mLoading != null && !mLoading.isDone() && !mLoading.isCancelled())
            return;

        Uri.Builder uriBuilder = Uri.parse("http://api.strimoid.pl/entries").buildUpon();

        if (mEntryAdapter.getCount() > 0)
            uriBuilder.appendQueryParameter("page", String.valueOf(mPage));

        mLoading = Ion.with(getActivity(), uriBuilder.build().toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error loading", Toast.LENGTH_LONG).show();
                            return;
                        }

                        JsonArray entries = result.getAsJsonArray("data");

                        for (JsonElement entry : entries) {
                            mEntryAdapter.add(entry.getAsJsonObject());

                            JsonArray replies = entry.getAsJsonObject()
                                    .get("replies").getAsJsonArray();

                            int size = Math.min(replies.size(), 2);

                            // Add two last replies
                            for (int i = 0; i < size; i++) {
                                JsonObject r = replies.get(replies.size()- size + i).getAsJsonObject();
                                r.addProperty("is_reply", true);

                                mEntryAdapter.add(r);
                            }

                            // Add "show more replies" button
                            if (replies.size() > 2) {
                                String parentId = entry.getAsJsonObject().get("_id").toString();

                                JsonObject sm = new JsonObject();
                                sm.addProperty("show_more", true);
                                sm.addProperty("parent_id", parentId);

                                mEntryAdapter.add(sm);
                            }
                        }

                        mPullToRefreshLayout.setRefreshComplete();
                        //setListShown(true);
                    }
                });

        mPage++;
    }
}