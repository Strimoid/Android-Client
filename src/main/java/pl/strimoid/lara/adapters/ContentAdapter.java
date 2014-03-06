package pl.strimoid.lara.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import pl.strimoid.lara.R;

public class ContentAdapter extends ArrayAdapter<JsonObject> {

    private Loadable mLoadable;

    private class ViewHolder {
        TextView title, description, uv, dv;
        ImageView image;
    }

    public interface Loadable {
        public void load();
    }

    public ContentAdapter(Context context, Loadable loadable) {
        super(context, R.layout.content_widget);

        mLoadable = loadable;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content_widget, null);
            ViewHolder holder = new ViewHolder();

            holder.title = (TextView) view.findViewById(R.id.title);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.uv = (TextView) view.findViewById(R.id.upvote);
            holder.dv = (TextView) view.findViewById(R.id.downvote);
            holder.image = (ImageView) view.findViewById(R.id.image);

            view.setTag(holder);
        }


        // we're near the end of the list adapter, so load more items
        if (position >= getCount() - 3)
            mLoadable.load();

        JsonObject content = getItem(position);
        ViewHolder holder = (ViewHolder) view.getTag();

        String username = content.getAsJsonObject("user").get("_id").getAsString();

        if (content.has("thumbnail")) {
            String imageUrl = content.get("thumbnail").getAsString();

            // start with the ImageView
            Ion.with(holder.image)
                    .load("http://strimoid.pl/uploads/thumbnails/" + imageUrl);

            holder.image.setVisibility(View.VISIBLE);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.title.setText(content.get("title").getAsString());
        holder.description.setText(content.get("description").getAsString());

        holder.uv.setText("  ▲  " + content.get("uv").getAsString());
        holder.dv.setText("  ▼  " + content.get("dv").getAsString());

        return view;
    }

}
