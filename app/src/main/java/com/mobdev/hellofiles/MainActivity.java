package com.mobdev.hellofiles;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Marco Picone (picone.m@gmail.com) 20/03/2020
 * Simple Activity and application to show how to work with Files on Android
 */
public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE = 54;

    public static String TAG = "HelloFiles";

    private static final String EXTERNAL_DOCUMENT_FILENAME = "loglist.txt";

    private static final String DOCUMENT_APPLICATION_TYPE = "text/plain";

	private Context mContext = null;

    private static final int EXTERNAL_FILE_CREATE_REQUEST_ID = 1818;

    private static final int EXTERNAL_FILE_OPEN_REQUEST_ID = 2828;

    private HistoryFragment historyFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);
        
        historyFragment = new HistoryFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container, historyFragment).commit();

        this.mContext = this;
        
        Toolbar toolbar = (Toolbar)findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        checkForWriteExternalStoragePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_create_document){
            exportOnSharedDocument();
            return true;
        }

        if(id == R.id.action_open_document){
            openSharedDocument();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Send the request to create a file on the external shared memory (both internal or external)
     */
    public void exportOnSharedDocument(){
        try{

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(DOCUMENT_APPLICATION_TYPE);
                intent.putExtra(Intent.EXTRA_TITLE, EXTERNAL_DOCUMENT_FILENAME);

                startActivityForResult(intent, EXTERNAL_FILE_CREATE_REQUEST_ID);
            }else {
                Toast.makeText(getApplicationContext(), "Function not available !", Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Send the request to create a file on the external shared memory (both internal or external)
     */
    public void openSharedDocument(){
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(DOCUMENT_APPLICATION_TYPE);
                startActivityForResult(intent, EXTERNAL_FILE_OPEN_REQUEST_ID);
            }
            else {
                Toast.makeText(getApplicationContext(), "Function not available !", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTERNAL_FILE_CREATE_REQUEST_ID) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        LogDescriptorManager.getInstance(getApplicationContext()).exportOnSharedDocument(data.getData());
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
        if (requestCode == EXTERNAL_FILE_OPEN_REQUEST_ID) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        if(LogDescriptorManager.getInstance(getApplicationContext()).readFromSharedDocument(data.getData()))
                            this.historyFragment.updateHistory();
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private void showStoragePermissionDeniedMessage() {
        Toast.makeText(this,"Write External Storage Permission Not Granted !",Toast.LENGTH_LONG).show();
    }

    private void checkForWriteExternalStoragePermissions(){

        String myPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        int permissionCheck = ContextCompat.checkSelfPermission(this,myPermission);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            Log.w(TAG,"checkForWriteExternalStoragePermissions() -> WRITE_EXTERNAL_STORAGE Not Granted !");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,myPermission)) {

                Log.d(TAG,"checkForWriteExternalStoragePermissions() -> shouldShowRequestPermissionRationale(): true");

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(this,"The Application needs the access to your external storage to properly work ! Check System Setting to grant access !",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{myPermission}, MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);

            } else {

                // No explanation needed, we can request the permission.

                Log.d(TAG,"checkForWriteExternalStoragePermissions() -> shouldShowRequestPermissionRationale(): false");

                ActivityCompat.requestPermissions(this,new String[]{myPermission}, MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            Log.d(TAG,"checkForWriteExternalStoragePermissions() -> WRITE_EXTERNAL_STORAGE GRANTED !");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        Log.d(TAG,"onRequestPermissionsResult() -> requestCode:"+requestCode);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"onRequestPermissionsResult() -> Permission GRANTED !");
                    // permission was granted, yay! Do the
                    Toast.makeText(mContext,"Permission Granted !",Toast.LENGTH_LONG).show();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Log.e(TAG,"onRequestPermissionsResult() -> Permission DENIED !");
                    showStoragePermissionDeniedMessage();
                }
                return;
            }
        }
    }
    
}
