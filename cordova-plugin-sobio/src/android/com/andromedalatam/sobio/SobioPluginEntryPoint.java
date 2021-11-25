package com.andromedalatam.sobio;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.andromedalatam.sobio.component.SobioHooks;
import com.andromedalatam.sobio.component.SobioSelfieLivenessResultCallback;
import com.andromedalatam.sobio.component.SobioSelfieLivenessResultContract;
import com.andromedalatam.sobio.component.SobioSelfieLivenessResultLauncherParams;
import com.andromedalatam.sobio.component.faceDemo.SobioSelfieLivenessActivity;
import com.andromedalatam.sobio.id.DniCameraActivity;
import com.andromedalatam.sobio.id.FileHelper;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class SobioPluginEntryPoint extends CordovaPlugin {

    private static final String TAG = "SobioCordovaPlugin";
    private CallbackContext callback = null;
    private final static int ACTION_LAUNCH = 199;
    private final static int DNI_CAPTURE = 200;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, String.format("Initializing %s", TAG));
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
      callback = callbackContext;
      if("launch".equals(action)) {
          Intent launchIntent = new Intent(cordova.getContext(), SobioSelfieLivenessActivity.class);
          launchIntent.putExtra("key", args.getString(3));
          launchIntent.putExtra("flagsDetection", args.getString(2));
          launchIntent.putExtra("mode", args.getString(0));
          launchIntent.putExtra("showGestureIndications", args.getBoolean(1));
          cordova.setActivityResultCallback(this);
          cordova.getActivity().startActivityForResult(launchIntent, ACTION_LAUNCH);
        } else if("captureDNI".equals(action)) {
          Intent launchIntent = new Intent(cordova.getContext(), DniCameraActivity.class);
          if(args.length() > 0) {
            launchIntent.putExtra("PageTitle", args.getString(0));
          }
          if(args.length() > 1) {
            launchIntent.putExtra("PageDescription", args.getString(1));
          }
          cordova.setActivityResultCallback(this);
          cordova.getActivity().startActivityForResult(launchIntent, DNI_CAPTURE);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == ACTION_LAUNCH && resultCode == Activity.RESULT_OK) {
          Map<String, Object> result = new HashMap<String, Object>();
          result.put("status", SobioHooks.AppStatus.valueOf(intent.getStringExtra("status")));
          result.put("frameFormat", SobioHooks.AppFrameFormat.valueOf(intent.getStringExtra("frameFormat")));
          result.put("bufferBase64", intent.getStringExtra("bufferBase64"));
          Map framePreview = new HashMap<String, Object>();
          framePreview.put("detalle", SobioHooks.AppFrameDetalle.valueOf(intent.getStringExtra("framePreview.detalle")));
          framePreview.put("buffer", null);
          String framePreviewBufferBase64 = intent.getStringExtra("framePreview.bufferBase64");
          if (framePreviewBufferBase64 != null && !framePreviewBufferBase64.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              framePreview.put("buffer", Base64.getDecoder().decode(framePreviewBufferBase64));
            } else {
              framePreview.put("buffer", android.util.Base64.decode(framePreviewBufferBase64, android.util.Base64.DEFAULT));
            }
          }
          result.put("framePreview", framePreview);
          callback.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject(result)));
        } else if(requestCode == DNI_CAPTURE) {
          JSONObject out = new JSONObject();
          if (resultCode == Activity.RESULT_OK) {
            String imageUrl = intent.getExtras().get("data").toString();
            boolean taked = (Boolean) intent.getExtras().get("taked");
            try {
              out.put("path", imageUrl);
              out.put("base64", FileHelper.INSTANCE.getBase64File(imageUrl, cordova.getContext(), taked));
              callback.sendPluginResult(new PluginResult(PluginResult.Status.OK, out));
            } catch (JSONException e) {
              Log.e(TAG, "Error packaging the response", e);
              callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, out));
            } catch (IOException e) {
              Log.e(TAG, "Can't open file", e);
              callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, out));
            }
          } else {
            callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, out));
          }
        } else {
            Log.d(TAG, String.format("Initializing invalid requestCode %d or resultCode %d", requestCode, resultCode));
        }
    }

}
