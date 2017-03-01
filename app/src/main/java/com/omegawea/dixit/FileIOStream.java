package com.omegawea.dixit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by SF on 2/21/2017.
 */

public class FileIOStream extends Activity {
    //    FOR TEST PURPOSE
    private static final String TAG = "FileIOStream";
    private  Context mContext;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    int count;
    String result = "";

    // This is the constructor of the class Employee
    public FileIOStream(Context context) {
        mContext = context;
    }

    public void InputStream(String filename, String data){
        try {
            fileOutputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE );
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "InputStream Error");
        }
    }

    public String OutputStream(String filename) {
        result = "";
        try {
            fileInputStream = mContext.openFileInput(filename);
            while( (count = fileInputStream.read()) != -1){
                result = result + Character.toString((char)count);
            }
            fileInputStream.close();
        }
        catch(Exception e){
            e.printStackTrace();
            Log.i(TAG, "OutputStream Error");
        }
        return result;
    }
}
