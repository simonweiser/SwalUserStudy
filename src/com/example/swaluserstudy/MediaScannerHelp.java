package com.example.swaluserstudy;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class MediaScannerHelp implements MediaScannerConnectionClient {

	private Context c;
	
	public MediaScannerHelp(Context context){
		this.c = context;
		
	}
	 public void addFile(String filename)
	    {
	        String [] paths = new String[1];
	        paths[0] = filename;
	        MediaScannerConnection.scanFile(c, paths, null, this);
	    }
	@Override
	public void onMediaScannerConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		 Log.i("ScannerHelper","Scan done - path:" + path + " uri:" + uri);

	}
	
}
