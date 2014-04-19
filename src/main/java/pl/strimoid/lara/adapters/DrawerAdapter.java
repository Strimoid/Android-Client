package pl.strimoid.lara.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pl.strimoid.lara.R;
import pl.strimoid.lara.models.DrawerFolder;

public class DrawerAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<DrawerFolder> mData;

    private class FolderViewHolder {
        TextView title;
        ImageView indicator;
    }

    private class GroupViewHolder {
        TextView title;
    }

    public DrawerAdapter(Context context, ArrayList<DrawerFolder> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).getGroups().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mData.get(groupPosition).getGroups().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.drawer_folder, null);
            FolderViewHolder holder = new FolderViewHolder();

            holder.title = (TextView) view.findViewById(R.id.title);
            holder.indicator = (ImageView) view.findViewById(R.id.indicator);

            view.setTag(holder);
        }

        DrawerFolder folder = (DrawerFolder) getGroup(groupPosition);
        FolderViewHolder holder = (FolderViewHolder) view.getTag();

        holder.title.setText(folder.getName());

        if (folder.getGroups().isEmpty()) {
            holder.indicator.setVisibility(View.INVISIBLE);
        } else if (isExpanded) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_collapse);
        } else {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_expand);
        }

        view.setTag(R.id.TAG_ID, folder.getId());
        view.setTag(R.id.TAG_IS_GROUP, folder.isGroup());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.drawer_group, null);
            GroupViewHolder holder = new GroupViewHolder();

            holder.title = (TextView) view.findViewById(R.id.title);

            view.setTag(holder);
        }

        String group = (String) getChild(groupPosition, childPosition);
        GroupViewHolder holder = (GroupViewHolder) view.getTag();

        holder.title.setText(group);

        view.setTag(R.id.TAG_ID, group);
        view.setTag(R.id.TAG_IS_GROUP, true);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
