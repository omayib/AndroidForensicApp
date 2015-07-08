package id.co.technomotion.androidforensicapp.helper;

import android.content.Context;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import id.co.technomotion.androidforensicapp.model.PackageInfo;

/**
 * Created by omayib on 5/17/15.
 */
public class SuperUserCommand {
    private Context context;
    public SuperUserCommand(Context context) {
        this.context=context;
    }

    public void getListOfDatabase(PackageInfo packageInfo, final CommandResponse callback){
        // "ls data/data/"+packageInfo.getPackageName()+"/databases"
        String cmd="find data/data/"+packageInfo.getPackageName()+" -type f -name '*db*' && find data/data/"+packageInfo.getPackageName()+" -type f -name '*sqlite*'";
        Command command = new Command(packageInfo.getId(), cmd){

            @Override
            public void commandOutput(int id, String line) {
                super.commandOutput(id, line);
//                pinfo.setDatabaseFiles(new DbFileInfo(line, "data/data/" + pinfo.getPackageName() + "/databases/" + line));
                callback.onSuccess(line);
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                callback.onCompleted(id,exitcode);
            }
        };

        try {
//            if(!pd.isShowing())
//                pd.show();
            RootTools.getShell(true).add(command);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailure(e);
//            if(pd.isShowing())
//                pd.dismiss();
        } catch (TimeoutException e) {
            e.printStackTrace();
            callback.onFailure(e);
//            if(pd.isShowing())
//                pd.dismiss();
        } catch (RootDeniedException e) {
            e.printStackTrace();
            callback.onFailure(e);
//            if(pd.isShowing())
//                pd.dismiss();
        }
    }

    public void copyFileToSdcard(String inputPath, final String tempOutputPath, final CommandResponse callback){
        final String fileName=inputPath.substring(inputPath.lastIndexOf("/"));
        Command commandCopy=new Command(0,"cp "+inputPath+" "+tempOutputPath){
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                callback.onCompleted(id,exitcode);
//                FileCompressor.compresToZip(tempOutputPath, tempOutputPath + "/" + fileName, fileName);
            }

            @Override
            public void commandOutput(int id, String line) {
                super.commandOutput(id, line);
                System.out.println("cout "+line);
                callback.onSuccess(line);
            }
        };

        try {
            RootTools.getShell(true).add(commandCopy);
            System.out.println(commandCopy.getCommand());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailure(e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            callback.onFailure(e);
        } catch (RootDeniedException e) {
            e.printStackTrace();
            callback.onFailure(e);
        }

    }
}
