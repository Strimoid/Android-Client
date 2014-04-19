package pl.strimoid.lara.listeners;

import java.util.ArrayList;
import java.util.List;

import pl.strimoid.lara.Session;
import pl.strimoid.lara.models.DrawerItem;

public class GroupChange {

    private final static GroupChange mInstance = new GroupChange();

    List<GroupChangeListener> listeners = new ArrayList<GroupChangeListener>();

    private GroupChange() {
    }

    public static GroupChange getInstance() {
        return mInstance;
    }

    public void addListener(GroupChangeListener newListener) {
        listeners.add(newListener);
    }

    public void changeGroup(DrawerItem newDrawerItem) {
        DrawerItem currentDrawerItem = Session.getInstance().getSelectedDrawerItem();

        if (currentDrawerItem == newDrawerItem) {
            return;
        }

        Session.getInstance().setSelectedDrawerItem(newDrawerItem);

        for (GroupChangeListener listener : listeners) {
            listener.onGroupChange();
        }
    }

}
