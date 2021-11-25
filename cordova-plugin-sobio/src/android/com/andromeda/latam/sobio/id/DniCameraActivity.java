package com.andromeda.latam.sobio.id;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.ionic.starter.R;


/**
 implementation 'androidx.camera:camera-core:1.1.0-alpha03'
 implementation 'androidx.camera:camera-camera2:1.1.0-alpha03'
 implementation 'androidx.camera:camera-lifecycle:1.1.0-alpha03'
 implementation 'androidx.camera:camera-view:1.0.0-alpha23'
 */
public class DniCameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

  private static final int CAMERA_START_PERMISSIONS = 1;
  private static final String TAG = "CameraXGFG";
  private ImageCapture imageCapture;
  private ExecutorService cameraExecutor;
  private ActivityResultLauncher resultLauncherGallery;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dni_camera);
    getSupportActionBar().hide();
    ImageView ivTakePhoto = (ImageView) findViewById(R.id.ivTakePhoto);
    String pageTitle = getIntent().getStringExtra("PageTitle");
    String pageDescription = getIntent().getStringExtra("PageDescription");
    if (pageTitle != null) {
      TextView tvCameraTitle = (TextView) findViewById(R.id.tvCameraTitle);
      tvCameraTitle.setText(pageTitle);
    }
    if (pageDescription != null) {
      TextView tvCameraDesc = (TextView) findViewById(R.id.tvCameraDesc);
      tvCameraDesc.setText(pageDescription);
    }
    resultLauncherGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
      if (result.getResultCode() == Activity.RESULT_OK) {
        if(result.getData() == null || result.getData().getData() == null) {
          setResult(RESULT_CANCELED);
        } else {
          setResult(RESULT_OK, (new Intent()).putExtra("data", result.getData().getData().toString()).putExtra("taked", false));
        }
      } else {
        setResult(RESULT_CANCELED);
      }
      finish();
    });

    ivTakePhoto.setOnClickListener(view -> {
      takePhoto();
    });
//    ImageView ivGallery = (ImageView) findViewById(R.id.ivGallery);
//    ivGallery.setOnClickListener(view -> {
//      goToGallery();
//    });
    if (allPermissionsGranted()) {
      startCamera();
    } else {
      checkPermissionsAndStartCamera();
    }
    cameraExecutor = Executors.newSingleThreadExecutor();
  }

  private boolean allPermissionsGranted() {
    return
      ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(getBaseContext(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(getBaseContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
  }

  public void checkPermissionsAndStartCamera() {
    String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    ActivityCompat.requestPermissions(this, permissions, CAMERA_START_PERMISSIONS);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    boolean allowed = true;
    if (requestCode == CAMERA_START_PERMISSIONS) {
      for (int i = 0; i < permissions.length; i++) {
        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
          allowed = false;
        }
      }
    }
    if (allowed) {
      startCamera();
    }
  }

  private void goToGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    resultLauncherGallery.launch(intent);
  }

  private void takePhoto() {
    if(imageCapture == null) {
      return;
    }
    ContextWrapper cw = new ContextWrapper(getApplicationContext());
    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
    File photoFile = new File(directory, "capture.jpeg");
    ImageCapture.OutputFileOptions outputOptions = (new ImageCapture.OutputFileOptions.Builder(photoFile)).build();
    final DniCameraActivity dniCameraActivit = this;
    imageCapture.takePicture(
      outputOptions,
      ContextCompat.getMainExecutor(dniCameraActivit),
      new ImageCapture.OnImageSavedCallback() {
        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
          Uri savedUri = CaptureImageHelper.INSTANCE.handleSamplingAndRotationBitmap(dniCameraActivit, Uri.fromFile(photoFile));
          Log.d(TAG, String.format("Photo captured succeded: %s", savedUri.toString()));
          setResult(RESULT_OK, (new Intent()).putExtra("data", savedUri.toString()).putExtra("taked", true));
          finish();
        }
        public void onError(ImageCaptureException exception) {
          Log.e(TAG, "Photo capture failed: ", exception);
          setResult(RESULT_CANCELED);
          finish();
        }
      }
    );
  }

  @SuppressLint("UnsafeOptInUsageError")
  private void startCamera() {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    cameraProviderFuture.addListener(() -> {
      ProcessCameraProvider cameraProvider;
      try {
        cameraProvider = cameraProviderFuture.get();
      } catch (Exception e) {
        Log.e(TAG, "Camera provider future failed to get", e);
        return;
      }
      PreviewView camPreview = (PreviewView) findViewById(R.id.cameraPreview);
      int rotation = camPreview.getDisplay().getRotation();
      Preview preview = (new Preview.Builder())
        .setTargetRotation(rotation)
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build();
      imageCapture = (new ImageCapture.Builder()).setTargetResolution(new Size(720,1080))
        .setTargetRotation(rotation)
        .build();
      ImageAnalysis imageAnalyzer = (new ImageAnalysis.Builder())
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build();
      imageAnalyzer.setAnalyzer(cameraExecutor, new LuminosityAnalyzer());
      cameraProvider.unbindAll();
      CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
      UseCaseGroup useCaseGroup = (new UseCaseGroup.Builder()).addUseCase(preview).addUseCase(imageAnalyzer).addUseCase(imageCapture).build();
      try {
        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);
        preview.setSurfaceProvider(camPreview.getSurfaceProvider());
      } catch(Exception exc) {
        Log.e(TAG, "Use case binding failed", exc);
      }

    }, ContextCompat.getMainExecutor(this));
  }

  private class LuminosityAnalyzer implements ImageAnalysis.Analyzer {

    @Override()
    public void analyze(ImageProxy image) {

    }

  }

}
