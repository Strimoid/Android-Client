package pl.strimoid.lara.models;

public class DrawerGroup implements DrawerItem {

    private String _id, name;

    public DrawerGroup(String id) {
        this._id = id;
        this.name = id;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public boolean isGroup() {
        return true;
    }

}
