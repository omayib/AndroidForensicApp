package id.co.technomotion.androidforensicapp.helper;

/**
 * Created by omayib on 5/17/15.
 */
public interface CommandResponse {
    void onSuccess(String response);
    void onFailure(Exception e);
    void onCompleted(int id,int exitCode);
}
