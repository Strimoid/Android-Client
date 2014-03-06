package pl.strimoid.lara.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import pl.strimoid.lara.R;
import pl.strimoid.lara.utils.HTML;

public class EntryAdapter extends ArrayAdapter<JsonObject> {

    public static final int TYPE_ENTRY = 0;
    public static final int TYPE_SHOW_MORE_BUTTON = 1;

    private Loadable mLoadable;
    private HTML mHTML;

    private class ViewHolder {
        TextView username, text, uv, dv;
        ImageView avatar, reply;
    }

    public interface Loadable {
        public void load();
    }

    public EntryAdapter(Context context, Loadable loadable) {
        super(context, R.layout.entry_widget);

        mLoadable = loadable;
        mHTML = new HTML();
    }

    public int getItemViewType(int position) {
        return getItem(position).has("show_more") ? TYPE_SHOW_MORE_BUTTON : TYPE_ENTRY;
    }

    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        int viewType = getItemViewType(position);

        if (view == null) {
            if (viewType == TYPE_SHOW_MORE_BUTTON)
                return createShowMoreButton();

            view = LayoutInflater.from(getContext()).inflate(R.layout.entry_widget, null);
            ViewHolder holder = new ViewHolder();

            holder.username = (TextView) view.findViewById(R.id.username);
            holder.text = (TextView) view.findViewById(R.id.text);
            holder.uv = (TextView) view.findViewById(R.id.upvote);
            holder.dv = (TextView) view.findViewById(R.id.downvote);
            holder.avatar = (ImageView) view.findViewById(R.id.avatar);
            holder.reply = (ImageView) view.findViewById(R.id.reply);

            view.setTag(holder);
        }

        if (viewType == TYPE_SHOW_MORE_BUTTON) {
            TextView loadMore = (TextView) view;
            loadMore.setText("Pokaż więcej odpowiedzi.");
            return view;
        }

        // we're near the end of the list adapter, so load more items
        if (position >= getCount() - 10)
            mLoadable.load();

        ViewHolder holder = (ViewHolder) view.getTag();

        JsonObject entry = getItem(position);
        JsonObject user = entry.getAsJsonObject("user");

        if (user.has("avatar")) {
            String avatarUrl = user.get("avatar").getAsString();

            Ion.with(holder.avatar)
                    .load("http://strimoid.pl/uploads/avatars/" + avatarUrl);

            holder.avatar.setVisibility(View.VISIBLE);
        } else {
            holder.avatar.setVisibility(View.GONE);
        }

        // Add border on the left side and gray background if it's reply
        if (entry.has("is_reply") && entry.get("is_reply").getAsBoolean()) {
            holder.reply.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.parseColor("#f0f0f0"));
        } else {
            holder.reply.setVisibility(View.GONE);
            view.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        holder.username.setText(user.get("_id").getAsString());
        holder.text.setText(mHTML.parse(entry.get("text").getAsString()));

        holder.uv.setText("  ▲  " + entry.get("uv").getAsString());
        holder.dv.setText("  ▼  " + entry.get("dv").getAsString());

        return view;
    }

    private View createShowMoreButton() {
        TextView showMore = new TextView(getContext());
        showMore.setText("Pokaż więcej odpowiedzi.");
        showMore.setGravity(Gravity.CENTER);
        showMore.setPadding(10, 10, 10, 10);
        showMore.setBackgroundColor(Color.parseColor("#ffffff"));

        return showMore;
    }

}
