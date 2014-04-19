package pl.strimoid.lara;

import com.google.gson.JsonObject;

import pl.strimoid.lara.models.DrawerGroup;
import pl.strimoid.lara.models.DrawerItem;

public class Session {

    private final static Session mInstance = new Session();

    private DrawerItem mSelectedDrawerItem;
    private JsonObject mUserData;

    private Session() {
        mSelectedDrawerItem = new DrawerGroup("all");
    }

    public static Session getInstance() {
        return mInstance;
    }

    public DrawerItem getSelectedDrawerItem() {
        return mSelectedDrawerItem;
    }

    public void setSelectedDrawerItem(DrawerItem newDrawerItem) {
        mSelectedDrawerItem = newDrawerItem;
    }

    public JsonObject getUserData() {
        return mUserData;
    }

    public void setUserData(JsonObject userData) {
        mUserData = userData;
    }

}
