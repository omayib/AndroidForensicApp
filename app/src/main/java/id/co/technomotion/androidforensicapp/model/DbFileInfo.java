package id.co.technomotion.androidforensicapp.model;

public class DbFileInfo implements Item{
    String name;
    String path;
    private boolean isChecked;

    public DbFileInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "DbFileInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public boolean isGroup() {
        return false;
    }
}