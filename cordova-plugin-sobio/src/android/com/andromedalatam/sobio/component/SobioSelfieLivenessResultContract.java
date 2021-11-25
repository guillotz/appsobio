package com.andromedalatam.sobio.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andromedalatam.sobio.component.faceDemo.SobioSelfieLivenessActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SobioSelfieLivenessResultContract extends ActivityResultContract<SobioSelfieLivenessResultLauncherParams.SobioSelfieLivenessConfigParams, SobioHooks.AppInfo> {

  private static SobioSelfieLivenessResultContract _instance;

  public static SobioSelfieLivenessResultContract getInstance() {
    if (_instance == null) {
      _instance = new SobioSelfieLivenessResultContract();
    }
    return _instance;
  }

  @NonNull
  @Override
  public Intent createIntent(@NonNull Context context, SobioSelfieLivenessResultLauncherParams.SobioSelfieLivenessConfigParams input) {
    Intent launchIntent = new Intent(context, SobioSelfieLivenessActivity.class);
    //set intent params on intent
    launchIntent.putExtra("key", input.key);
    launchIntent.putExtra("flagsDetection", input.flagsDetection.name());
    launchIntent.putExtra("mode", input.mode.name());
    launchIntent.putExtra("showGestureIndications", input.showGestureIndications);
    return launchIntent;
  }

  @Override
  public SobioHooks.AppInfo parseResult(int resultCode, @Nullable Intent intent) {
    SobioHooks.AppInfo result = null;
    if (resultCode == Activity.RESULT_OK) {
      result = new SobioHooks.AppInfo();
      result.status = SobioHooks.AppStatus.valueOf(intent.getStringExtra("status"));
      result.frameFormat = SobioHooks.AppFrameFormat.valueOf(intent.getStringExtra("frameFormat"));
      result.bufferBase64 = intent.getStringExtra("bufferBase64");

      result.framePreview = new SobioHooks.AppResultFrame();
      result.framePreview.detalle = SobioHooks.AppFrameDetalle.valueOf(intent.getStringExtra("framePreview.detalle"));
      result.framePreview.buffer = null;
      String framePreviewBufferBase64 = intent.getStringExtra("framePreview.bufferBase64");
      if (framePreviewBufferBase64 != null && !framePreviewBufferBase64.isEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          result.framePreview.buffer = Base64.getDecoder().decode(framePreviewBufferBase64);
        } else {
          result.framePreview.buffer = android.util.Base64.decode(framePreviewBufferBase64, android.util.Base64.DEFAULT);
        }
      }
    }
    return result;
  }
}
