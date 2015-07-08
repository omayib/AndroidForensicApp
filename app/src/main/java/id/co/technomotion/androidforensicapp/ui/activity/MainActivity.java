package id.co.technomotion.androidforensicapp.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import id.co.technomotion.androidforensicapp.R;
import id.co.technomotion.androidforensicapp.helper.CommandResponse;
import id.co.technomotion.androidforensicapp.helper.FileCompressor;
import id.co.technomotion.androidforensicapp.helper.MemoryStorage;
import id.co.technomotion.androidforensicapp.helper.SuperUserCommand;
import id.co.technomotion.androidforensicapp.model.DbFileInfo;
import id.co.technomotion.androidforensicapp.model.Item;
import id.co.technomotion.androidforensicapp.model.PackageInfo;
import id.co.technomotion.androidforensicapp.ui.adapter.PackageAdapter;


public class MainActivity extends ActionBarActivity {
    private ArrayList<Item> listOfDb=new ArrayList<>();
    private ArrayList<PackageInfo> listOfPackage;
    private PackageAdapter adapter;
    private ExpandableListView listview;
    private ProgressDialog pd;
    private SuperUserCommand superUserCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview= (ExpandableListView) findViewById(R.id.listView);

        pd=new ProgressDialog(this);
        pd.setMessage("please wait...");

        superUserCommand=new SuperUserCommand(this);


        System.out.println("mem :"+MemoryStorage.getSdCardPath());
        System.out.println("mem :"+MemoryStorage.isWritable());
        System.out.println("mem :"+MemoryStorage.isAvailable());
        System.out.println("mem :"+MemoryStorage.getAllStorageLocations().toString());

        /**
         * preparing data
         */
        listOfPackage=getPackagesContainDatabases();

        adapter=new PackageAdapter(this,listOfPackage,R.layout.group_item,R.layout.child_item);
        listview.setAdapter(adapter);
        listview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                PackageInfo selectedPackageInfo=  listOfPackage.get(groupPosition);
                if(!selectedPackageInfo.hasDatabase())
                    Toast.makeText(getApplication(),"this apps dont have database",Toast.LENGTH_SHORT).show();
                return !selectedPackageInfo.hasDatabase();
            }
        });
        listview.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                PackageInfo selectedPackageInfo=  listOfPackage.get(groupPosition);
                System.out.println("onGroupExpand "+selectedPackageInfo.getDatabaseDirectory());
                getDbFiles(selectedPackageInfo);
            }
        });

        listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final DbFileInfo dbFileInfo= (DbFileInfo)adapter.getChild(groupPosition, childPosition);
                System.out.println(dbFileInfo.getPath());
                popupDialog(dbFileInfo.getPath());
                return false;
            }
        });
        adapter.notifyDataSetChanged();

    }

    private void popupDialog(final String path){
        String filename=path.substring(path.lastIndexOf("/")+1);
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage("copy file "+filename+" ke sdcard?");
        dialog.setPositiveButton("ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                copyFile(path);
            }
        });
        dialog.setNegativeButton("tidak",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void getDbFiles(final PackageInfo pinfo){
        if(!pd.isShowing())
            pd.show();
        /**
         * check for readiness device
         */
        if (RootTools.isBusyboxAvailable()) {
            System.out.println("busybox available");
        } else {
            System.out.println("busybox NOT available");
            return;
        }

        if(RootTools.isRootAvailable()){
            System.out.println("root available");
        }else{
            System.out.println("root not avlb");
            return;
        }
        superUserCommand.getListOfDatabase(pinfo,new CommandResponse() {
            @Override
            public void onSuccess(String response) {
                //"data/data/" + pinfo.getPackageName()
                pinfo.setDatabaseFiles(new DbFileInfo(response, response));
            }

            @Override
            public void onFailure(Exception e) {
                if(pd.isShowing())
                    pd.dismiss();
            }

            @Override
            public void onCompleted(int id, int exitCode) {
                if(pd.isShowing())
                    pd.dismiss();
                adapter.setChild(pinfo);
            }
        });
    }

    private ArrayList<PackageInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<PackageInfo> res = new ArrayList<PackageInfo>();
        List<android.content.pm.PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            android.content.pm.PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            PackageInfo newInfo = new PackageInfo(i, p.applicationInfo.loadLabel(getPackageManager()).toString(),
                    p.packageName,p.applicationInfo.loadIcon(getPackageManager()));

            res.add(newInfo);
        }
        return res;
    }

    private ArrayList<PackageInfo> getPackagesContainDatabases() {
        ArrayList<PackageInfo> apps = getInstalledApps(true); /* false = no system packages */
        for(PackageInfo packageInfo:apps){
            packageInfo.setDatabaseDirectory();
//            File f = new File("data/data/"+packageInfo.getPackageName()+"/databases");
//            if (f.exists() && f.isDirectory()) {
//                packageInfo.setDatabaseDirectory();
//            }
        }
        return apps;
    }

    private void copyFile(String inputPath) {

        if(!pd.isShowing())
            pd.show();

        final String filename=inputPath.substring(inputPath.lastIndexOf("/"));
        final String tempOutputPath= MemoryStorage.getSdCardPath()+MemoryStorage.FORENSIC_APP_DIR;
        System.out.println("tmp "+tempOutputPath);
        File tempDir=new File(tempOutputPath);
        if(!tempDir.exists()){
            tempDir.mkdir();
        }

        /**
         * check for readiness device
         */
        if (RootTools.isBusyboxAvailable()) {
            System.out.println("busybox available");
        } else {
            System.out.println("busybox NOT available");
            Toast.makeText(getApplicationContext(),"tidak bisa mengakses superuser",Toast.LENGTH_SHORT).show();
            return;
        }

        if(RootTools.isRootAvailable()){
            System.out.println("root available");
        }else{
            System.out.println("root not avlb");
            Toast.makeText(getApplicationContext(),"tidak bisa mengakses superuser",Toast.LENGTH_SHORT).show();
            return;
        }

        superUserCommand.copyFileToSdcard(inputPath,tempOutputPath,new CommandResponse() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFailure(Exception e) {
                if(pd.isShowing())
                    pd.dismiss();
            }

            @Override
            public void onCompleted(int id, int exitCode) {
                FileCompressor.compressToTarBz2(tempOutputPath, tempOutputPath + filename, filename);
                if(pd.isShowing())
                    pd.dismiss();
                Toast.makeText(getApplicationContext(),"saved on "+tempOutputPath,Toast.LENGTH_SHORT).show();
            }
        });

    }




}
