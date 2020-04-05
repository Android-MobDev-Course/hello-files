package com.mobdev.hellofiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Marco Picone (picone.m@gmail.com) 20/03/2020
 * Singleton to handle and manage existing logs within the application.
 */
public class LogDescriptorManager {

	private static final String TAG = "LogDescriptorManager";

	private Context context = null;

	private String outputFileName = "loglist.txt";

	/*
	 * The instance is static so it is shared among all instances of the class. It is also private
	 * so it is accessible only within the class.
	 */
	private static LogDescriptorManager instance = null;
	
	private ArrayList<LogDescriptor> logList = null;

	private Gson gson = null;

	/*
	 * The constructor is private so it is accessible only within the class.
	 */
	private LogDescriptorManager(Context context){

		Log.d(MainActivity.TAG,"Number Manager Created !");
		this.context = context;
		this.gson = new Gson();

		/*
		 * Try to read an existing log list and load into the ArrayList
		 */
		try {
			this.logList = readLogListFromFile();
			Log.d(MainActivity.TAG,"Log File available ! List size: " + this.logList.size());
		} catch(Exception e) {
			//If there is not an existing file create an empty ArrayList
			this.logList = new ArrayList<LogDescriptor>();
			Log.e(MainActivity.TAG,"Error Reading Log List on File: " + e.getLocalizedMessage());
		}

	}

	public static LogDescriptorManager getInstance(Context context){
		/*
		 * The constructor is called only if the static instance is null, so only the first time 
		 * that the getInstance() method is invoked.
		 * All the other times the same instance object is returned.
		 */
		if(instance == null)
			instance = new LogDescriptorManager(context);
		return instance;
	}
	
	public void addLog(LogDescriptor log){
		this.logList.add(log);
		
		try {
			saveLogListOnAppInternalStorage();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error Saving Log List on File ...", Toast.LENGTH_LONG).show();
		}
		
	}
	
	public void addLogToHead(LogDescriptor log){
		this.logList.add(0,log);
		
		try {
			saveLogListOnAppInternalStorage();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error Saving Log List on File ...", Toast.LENGTH_LONG).show();
		}
	}
	
	public void removeLog(int position){
		this.logList.remove(position);
		
		try {
			saveLogListOnAppInternalStorage();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error Saving Log List on File ...", Toast.LENGTH_LONG).show();
		}
	}
	
	public void removeLog(LogDescriptor log){
		this.logList.remove(log);
		
		try {
			saveLogListOnAppInternalStorage();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Error Saving Log List on File ...", Toast.LENGTH_LONG).show();
		}
	}
	
	public ArrayList<LogDescriptor> getLogList(){
		return logList;
	}

	/**
	 * Check the status of the External Memory and if it is ready and available for the application
	 * @return a boolean value with the status of the external memory
	 */
	private boolean isExternalMemoryReady()
	{
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		return mExternalStorageAvailable && mExternalStorageWriteable;

	}

	/**
	 * Create a new Document/File on the Public Shared Memory. Starts from the Location (URI)
	 * selected by the user through a Picker
	 * @param uri
	 * @return a boolean value with the status of the operation
	 */
	public boolean exportOnSharedDocument(Uri uri) {

		if(uri == null){
			Log.e(TAG, "Error Exporting on Shared Storage Document ! Uri = Null !");
			return false;
		}

		try {

			OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
			String outputContent = getSerializedJsonContent();

			if(outputStream != null && outputContent != null){
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
				bw.write(outputContent);
				bw.flush();
				bw.close();

				return true;
			}
			else{
				Log.e(TAG, "Error Exporting on Shared Storage Document ! OutputStream or Json Content = Null !");
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean readFromSharedDocument(Uri uri) {

		if(uri == null){
			Log.e(TAG, "Error Reading From Shared Storage Document ! Uri = Null !");
			return false;
		}

		try {

			InputStream inputStream = context.getContentResolver().openInputStream(uri);

			if(inputStream != null){
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

				StringBuilder stringBuilder = new StringBuilder();
				String line;

				while((line = br.readLine()) != null)
					stringBuilder.append(line);

				br.close();

				String result = stringBuilder.toString();
				Log.i(TAG, "Read File frome Shared URI. Content: " + result);

				Type collectionType = new TypeToken<Collection<LogDescriptor>>(){}.getType();
				Collection<LogDescriptor> dLogList = gson.fromJson(result, collectionType);

				//Save the retrieved list
				if(dLogList != null) {
					this.logList = new ArrayList<>(dLogList);
					this.saveLogListOnAppInternalStorage();
					return true;
				}
				else {
					Log.e(TAG, "Error Reading from Shared Storage Document ! Log List = Null !");
					return false;
				}
			}
			else{
				Log.e(TAG, "Error Reading from Shared Storage Document ! inputStream = Null !");
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Serialize the Log List in a JSON String
	 * @return The Serialized JSON String of the Log List
	 */
	private String getSerializedJsonContent(){

		try{
			return gson.toJson(this.logList);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Export the log list on file
	 * @return a boolean value with the status of the operation
	 */
	public boolean exportStoredLogListAppExternalStorage()
	{
		//Check if the external memory is ready to be used
		if(isExternalMemoryReady())
		{
				
			try {
				
				//Get a File object associated to the root path of the external storage
				File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

				//Path associated to the root of the external storage
				String path = dir.getAbsolutePath();
				
				//The filename of the new file 
				String fileName = "mobdev_loglist.txt";
						
				Log.d(MainActivity.TAG,"External Storage Base Path: " + path);
				
				//Create a new File object representing the file that we are going to create
				File outputFile = new File(path + File.separator + fileName);
				
				//Check if the file does not exist and if not create it
				if(!outputFile.exists())
					outputFile.createNewFile();
				
				//Open the Output stream to write on the target file
				FileOutputStream fos = new FileOutputStream(outputFile);

				String output = getSerializedJsonContent();

				if(output != null){
					fos.write(output.getBytes());
					//Close the output stream
					fos.close();
					Log.d(MainActivity.TAG,"LogList Correctly Exported ! ("+outputFile.getAbsolutePath()+")");
					Toast.makeText(context, "LogList Correctly Exported on External Memory!", Toast.LENGTH_LONG).show();
				}
				else {
					Log.e(MainActivity.TAG,"Error Exporting LogList ! Null Json Content ! ");
					Toast.makeText(context, "Error Exporting LogList ! !", Toast.LENGTH_LONG).show();
				}
				
			} catch (Exception e) {
				Log.e(MainActivity.TAG,"Error Exporting LogList: " + e.getLocalizedMessage());
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		else
		{	
			Log.e(MainActivity.TAG, "Error External Storage Not Available !");	
			return false;
		}
	}
	
	/**
	 * Read (if available) the log list from the Internal Application Storage
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private ArrayList<LogDescriptor> readLogListFromFile() throws FileNotFoundException, IOException 
	{
		Log.d(MainActivity.TAG, "Reading Bookmark List from Internal Storage ...");
		
		//Create a StringBuffer used to append the chat read from the file
		StringBuffer strContent = new StringBuffer("");
		
		int ch;

		//Open the FileInputStream using the Context object
		FileInputStream fis = context.openFileInput(outputFileName);

		//While to read char by char the file (It is not the only way ! You can also read line by line)
		while( (ch = fis.read()) != -1)
			strContent.append((char)ch);
		
		//Close the input stream
		fis.close();

		Log.d(MainActivity.TAG, "Read Internal Storage Bookamark File: " + strContent);

		Type collectionType = new TypeToken<Collection<LogDescriptor>>(){}.getType();

		//Create a Type object for the deserialization of the ArrayList of LogDescriptor
		Collection<LogDescriptor> dLogList = gson.fromJson(strContent.toString(), collectionType);
		
		//Save the retrieved list
		if(dLogList != null)
			return new ArrayList<LogDescriptor>(dLogList);

		//If the list is null return an empty list
		return new ArrayList<LogDescriptor>();
	}

	/**
	 * Save the log list on file using the JSON format
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void saveLogListOnAppInternalStorage() throws FileNotFoundException, IOException
	{
		Log.d(MainActivity.TAG, "Saving LogDescriptor List on File ...");

		//Create the Gson object to serialize the ArrayList
		Gson gson = new Gson();

		//Create a Type object for the serialization of the ArrayList of LogDescriptor
		Type collectionType = new TypeToken<Collection<LogDescriptor>>(){}.getType();

		//Serialize in JSON
		String ssidListJson = gson.toJson(this.logList, collectionType);

		//Write the obtained string on file
		FileOutputStream fos = context.openFileOutput(outputFileName, Context.MODE_PRIVATE);
		fos.write(ssidListJson.getBytes());
		fos.close();
	}
}

