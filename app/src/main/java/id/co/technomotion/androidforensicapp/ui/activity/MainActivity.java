package id.co.technomotion.androidforensicapp.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import id.co.technomotion.androidforensicapp.helper.CommandResponse;
import id.co.technomotion.androidforensicapp.helper.FileCompressor;
import id.co.technomotion.androidforensicapp.helper.SuperUserCommand;
import id.co.technomotion.androidforensicapp.ui.adapter.PackageAdapter;
import id.co.technomotion.androidforensicapp.R;
import id.co.technomotion.androidforensicapp.model.DbFileInfo;
import id.co.technomotion.androidforensicapp.model.Item;
import id.co.technomotion.androidforensicapp.model.PackageInfo;


public class MainActivity extends ActionBarActivity {
    private ArrayList<Item> listOfDb=new ArrayList<>();
    private ArrayList<PackageInfo> listOfPackage;
    private PackageAdapter adapter;
    private ExpandableListView listview;
    private ProgressDialog pd;
    private List<String> listOfSelectedDatabasePath=new ArrayList<>();
    private SuperUserCommand superUserCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview= (ExpandableListView) findViewById(R.id.listView);

        pd=new ProgressDialog(this);
        pd.setMessage("please wait...");

        superUserCommand=new SuperUserCommand(this);

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
                final DbFileInfo dbFileInfo= (DbFileInfo)adapter.getChild(groupPosition,childPosition);
                System.out.println(dbFileInfo.getPath());
                popupDialog(dbFileInfo.getPath());
                return false;
            }
        });
        adapter.notifyDataSetChanged();

    }

    private void popupDialog(final String path){
        String filename=path.substring(path.lastIndexOf("/"));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
        }

        if(RootTools.isRootAvailable()){
            System.out.println("root available");
        }else{
            System.out.println("root not avlb");
        }
        superUserCommand.getListOfDatabase(pinfo,new CommandResponse() {
            @Override
            public void onSuccess(String response) {
                pinfo.setDatabaseFiles(new DbFileInfo(response, "data/data/" + pinfo.getPackageName() + "/databases/" + response));
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
            File f = new File("data/data/"+packageInfo.getPackageName()+"/databases");
            if (f.exists() && f.isDirectory()) {
                packageInfo.setDatabaseDirectory();
            }
        }
        return apps;
    }

    private void copyFile(String inputPath) {
        final String filename=inputPath.substring(inputPath.lastIndexOf("/"));
        final String tempOutputPath= Environment.getExternalStorageDirectory().getPath()+"/forensic_dir_temp";
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
        }

        if(RootTools.isRootAvailable()){
            System.out.println("root available");
        }else{
            System.out.println("root not avlb");
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
                FileCompressor.compresToZip(tempOutputPath, tempOutputPath + "/" + filename, filename);
                if(pd.isShowing())
                    pd.dismiss();
            }
        });

//        Command commandCopy=new Command(0,"cp "+inputPath+" "+tempOutputPath){
//            @Override
//            public void commandCompleted(int id, int exitcode) {
//                super.commandCompleted(id, exitcode);
//                if(pd.isShowing())
//                    pd.dismiss();
//                FileCompressor.compresToZip(tempOutputPath, tempOutputPath + "/" + filename, filename);
//            }
//
//            @Override
//            public void commandOutput(int id, String line) {
//                super.commandOutput(id, line);
//                System.out.println("cout "+line);
//            }
//        };
//
//        try {
//            if(!pd.isShowing())
//                pd.show();
//            RootTools.getShell(true).add(commandCopy);
//            System.out.println(commandCopy.getCommand());
//        } catch (IOException e) {
//            e.printStackTrace();
//            if(pd.isShowing())
//                pd.dismiss();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//            if(pd.isShowing())
//                pd.dismiss();
//        } catch (RootDeniedException e) {
//            e.printStackTrace();
//            if(pd.isShowing())
//                pd.dismiss();
//        }

//        InputStream in = null;
//        OutputStream out = null;
//        try {
//
//            //create output directory if it doesn't exist
//            File dir = new File (outputPath);
//            if (!dir.exists())
//            {
//                dir.mkdirs();
//            }
//
//
//            in = new FileInputStream(inputPath + fileName);
//            out = new FileOutputStream(outputPath + fileName);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            in.close();
//            in = null;
//
//            // write the output file (You have now copied the file)
//            out.flush();
//            out.close();
//            out = null;
//
//        }  catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
//        catch (Exception e) {
//            Log.e("tag", e.getMessage());
//        }

    }




}
