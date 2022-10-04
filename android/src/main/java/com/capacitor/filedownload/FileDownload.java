package com.capacitor.filedownload;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.io.File;

public class FileDownload {

  private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;

  private static final String TAG = "FileDownload";

  private FileDownloadPlugin instance;

  private long downloadId;
  private String pathstr;

  private String title;
  private String description;
  private String objectId;
  private String filename;
  private String url;
  private boolean downloading = true;


  public FileDownload(FileDownloadPlugin instance, String title, String description, String objectId, String filename, String url) {
    this.instance = instance;
    this.title = title;
    this.description = description;
    this.objectId = objectId;
    this.filename = filename;
    this.url = url;
  }


  public void downloadFile() {
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    request.setAllowedOverRoaming(false);
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
    request.setTitle(title);
    request.setDescription(description);
    request.setVisibleInDownloadsUi(true);
    File file = new File(instance.getmContext().getExternalFilesDir(""), filename);
    request.setDestinationUri(Uri.fromFile(file));
    pathstr = file.getAbsolutePath();
    JSObject ret = new JSObject();
    ret.put("objectId", objectId);
    ret.put("progress", 0);
    instance.sendEvent("downloadProgress", ret);
    downloadId = instance.getDownloadManager().enqueue(request);
    new Thread(new Runnable() {
      @SuppressLint("Range")
      @Override
      public void run() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        while (downloading) {
          DownloadManager.Query q = new DownloadManager.Query();
          q.setFilterById(downloadId);
          Cursor cursor = instance.getDownloadManager().query(query);
          cursor.moveToFirst();
          int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
          int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
          if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
            downloading = false;
          }
          double dl_progress = (((bytes_downloaded * 100l) / bytes_total));
          dl_progress = dl_progress / 100;
          Log.d(TAG, "PROGRESS " + dl_progress);
          JSObject ret = new JSObject();
          ret.put("objectId", objectId);
          ret.put("progress", dl_progress);
          instance.sendEvent("downloadProgress", ret);
          cursor.close();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
          }
        }

      }
    }).start();
    instance.getmContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      checkStatus();
    }
  };

  private void checkStatus() {
    DownloadManager.Query query = new DownloadManager.Query();
    query.setFilterById(downloadId);
    Cursor cursor = instance.getDownloadManager().query(query);
    if (cursor.moveToFirst()) {
      int index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
      int status = cursor.getInt(index);
      JSObject ret = new JSObject();
      ret.put("objectId", objectId);
      switch (status) {
        case DownloadManager.STATUS_PAUSED:
          Log.d(TAG, "STATUS_PAUSED file" + this.filename);
          ret.put("status", "STATUS_PAUSED");
          instance.sendEvent("downloadStatus", ret);
          break;
        case DownloadManager.STATUS_PENDING:
          Log.d(TAG, "STATUS_PENDING file" + this.filename);
          ret.put("status", "STATUS_PENDING");
          instance.sendEvent("downloadStatus", ret);
          break;
        case DownloadManager.STATUS_RUNNING:
          ret.put("status", "STATUS_RUNNING");
          Log.d(TAG, "STATUS_RUNNING file" + this.filename);
          instance.sendEvent("downloadStatus", ret);
          break;
        case DownloadManager.STATUS_SUCCESSFUL:
          Log.d(TAG, "STATUS_SUCCESSFUL file" + this.filename);
          downloading = false;
          ret.put("status", "STATUS_SUCCESSFUL");
          instance.sendEvent("downloadStatus", ret);
          cursor.close();
          ret.put("path", "file://" + pathstr);
          instance.get_call().resolve(ret);
          ret = new JSObject();
          ret.put("objectId", objectId);
          ret.put("progress", 1);
          instance.sendEvent("downloadProgress", ret);
          break;
        case DownloadManager.STATUS_FAILED:
          int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
          int reason = cursor.getInt(columnReason);
          String reasonText = "";
          switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
              reasonText = "ERROR_CANNOT_RESUME";
              break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
              reasonText = "ERROR_DEVICE_NOT_FOUND";
              break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
              reasonText = "ERROR_FILE_ALREADY_EXISTS";
              break;
            case DownloadManager.ERROR_FILE_ERROR:
              reasonText = "ERROR_FILE_ERROR";
              break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
              reasonText = "ERROR_HTTP_DATA_ERROR";
              break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
              reasonText = "ERROR_INSUFFICIENT_SPACE";
              break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
              reasonText = "ERROR_TOO_MANY_REDIRECTS";
              break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
              reasonText = "ERROR_UNHANDLED_HTTP_CODE";
              break;
            case DownloadManager.ERROR_UNKNOWN:
              reasonText = "ERROR_UNKNOWN";
              break;
          }
          Log.e(TAG, "Failed download file" + this.filename + " : " + reasonText);
          downloading = false;
          ret.put("status", "STATUS_FAILED");
          instance.sendEvent("downloadStatus", ret);
          cursor.close();
          instance.get_call().reject("Error");
          instance.getmContext().unregisterReceiver(receiver);
          break;
      }
    }
  }}
