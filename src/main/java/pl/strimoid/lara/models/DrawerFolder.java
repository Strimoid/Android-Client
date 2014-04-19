package pl.strimoid.lara.models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class DrawerFolder implements DrawerItem {

    private String _id, name;
    private ArrayList<String> groups;
    private boolean isGroup;

    public DrawerFolder(String _id, String name) {
        this._id = _id;
        this.name = name;
        this.groups = new ArrayList<String>();

        this.isGroup = true;
    }

    public DrawerFolder(String _id, String name, ArrayList<String> groups) {
        this._id = _id;
        this.name = name;
        this.groups = groups;

        this.isGroup = true;
    }

    public DrawerFolder(String _id, String name, JsonElement groups) {
        this._id = _id;
        this.name = name;

        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<String> groupsArray = new Gson().fromJson(groups, listType);
        Collections.sort(groupsArray);

        this.groups = groupsArray;

        this.isGroup = true;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public boolean isGroup()
    {
        return isGroup;
    }

}
