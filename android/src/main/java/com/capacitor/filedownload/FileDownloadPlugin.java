package com.capacitor.filedownload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;

@CapacitorPlugin(name = "FileDownload")
public class FileDownloadPlugin extends Plugin {

  private static final String TAG = "FileDownload";

  private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
  private DownloadManager downloadManager;
  private Context mContext;
  PluginCall _call;

  @PluginMethod
  public void download(PluginCall call) {
    _call = call;
    mContext = getContext();
    requestPermissions();
    downloadFile(call);
  }

  public void sendEvent(String name, JSObject ret) {
    this.notifyListeners(name, ret);
  }

  private void requestPermissions() {
    if (ContextCompat.checkSelfPermission(mContext,
      Manifest.permission.WRITE_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED) {
      //没有授权，编写申请权限代码
      ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    } else {
      Log.d(TAG, "requestMyPermissions: error");
    }
    if (ContextCompat.checkSelfPermission(mContext,
      Manifest.permission.READ_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED) {
      //没有授权，编写申请权限代码
      ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    } else {
      Log.d(TAG, "requestMyPermissions: error");
    }
  }

  private void downloadFile(final PluginCall call) {
    try{
      requestPermissions();
      if (downloadManager == null)
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
      String url = call.getString("uri", "");
      String filename = call.getString("fileName", "");
      String title = call.getString("title", "");
      String description = call.getString("description", "");
      String objectId = call.getString("objectId", "");
      FileDownload fileDownload = new FileDownload(this, title, description, objectId,filename, url);
      fileDownload.downloadFile();
      JSObject ret = new JSObject();
      ret.put("objectId", objectId);
      call.resolve(ret);
    }catch (Exception exception){
      call.reject(exception.getMessage());
      exception.printStackTrace();
    }


  }

  public DownloadManager getDownloadManager() {
    return downloadManager;
  }

  public Context getmContext() {
    return mContext;
  }

  public PluginCall get_call() {
    return _call;
  }

}
