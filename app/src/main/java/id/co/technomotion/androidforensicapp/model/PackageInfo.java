package id.co.technomotion.androidforensicapp.model;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class PackageInfo implements Item{
    private int id=-1;
    private String appName = "";
    private String packageName = "";
    private Drawable icon;
    private String databaseDirectory ="";
    private List<DbFileInfo> databaseFiles=new ArrayList<>();

    public PackageInfo(int id, String appName, String packageName, Drawable icon) {
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
    }

    public boolean hasDatabase(){
        return !databaseDirectory.isEmpty();
    }
    public void setDatabaseDirectory() {
        this.databaseDirectory = "data/data/"+packageName+"/databases";
    }

    public void setDatabaseFiles(DbFileInfo databaseFiles) {
        this.databaseFiles.add(databaseFiles);
    }

    public int getId() {
        return id;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getDatabaseDirectory() {
        return databaseDirectory;
    }

    public List<DbFileInfo> getDatabaseFiles() {
        return databaseFiles;
    }


    @Override
    public boolean isGroup() {
        return true;
    }
}