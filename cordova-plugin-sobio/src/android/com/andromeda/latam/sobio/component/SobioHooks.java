package com.andromeda.latam.sobio.component;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;

import com.andromeda.latam.sobio.component.faceDemo.SobioSelfieLivenessActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class SobioHooks {
  private static final String TAG = "SobioHooks";
  public static final int PERMISIONS_REQUEST_CODE = 1023;
  public static final int PERMISIONS_DEV_REQUEST_CODE = 1025;
  private SobioSelfieLivenessActivity sobioSelfieLivenessActivity;
  private com.andromeda.latam.sobio.component.camera2.Camera2Helper cameraView = null;

  public SobioHooks(SobioSelfieLivenessActivity sobioSelfieLivenessActivity, com.andromeda.latam.sobio.component.camera2.Camera2Helper cameraView) {
    this.sobioSelfieLivenessActivity = sobioSelfieLivenessActivity;
    this.cameraView = cameraView;
  }

  //exported
  public enum AppState {
    UNINITIALIZATED(0),
    INIT_SDL(1), INIT_DETECTORS(2), INIT_WEBCAM(3), INIT_ERROR(9),
    READY_TO_START(10), START_DETECTORS(11), RUNNING(12), LEAVE_RUNNING(13), READY_TO_RESTART(14),
    READY_TO_END(91), GOING_TO_END(92),
    ENDING(100), END_OK(101), END_ERROR(102), END_CANCELED(103);

    int valor;

    private AppState(int v) {
      this.valor = v;
    }
    public int getValor() {
      return this.valor;
    }
    public static AppState fromValor(int v) {
      for (AppState item: values()) {
        if (item.valor == v)
          return item;
      }
      return null;
    }
  }

  public enum LivenessAction {
    LIVENESS_ACTION_LOOK_AT_FRONT(0x0010),
    LIVENESS_ACTION_CENTER_EYES(0x0020),
    LIVENESS_ACTION_TURN_TO_LEFT(0x0030),
    LIVENESS_ACTION_TURN_TO_RIGHT(0x0040),
    LIVENESS_ACTION_WAIT_FOR_PROCESS(0x0990);
    int valor;

    private LivenessAction(int v) {
      this.valor = v;
    }
    public int getValor() {
      return this.valor;
    }
    public static LivenessAction fromValor(int v) {
      for (LivenessAction item: values()) {
        if (item.valor == v)
          return item;
      }
      return null;
    }
  }

  public static class LivenesEvent {
    public LivenessAction suggestedAction;
    public int messageCode;
    public String messageAction;

    public LivenesEvent() {}
    public LivenesEvent(int iSugAct, int msgCod, String msgAct) {
      this.suggestedAction = LivenessAction.fromValor(iSugAct);
      this.messageCode = msgCod;
      this.messageAction = msgAct;
    }
  }

  public enum AppStatus {
    SUCCESS, ERROR, CANCEL, TIMEOUT
  }

  public enum AppFrameFormat {
    PNG, JPG
  }

  public enum AppFrameDetalle {
    EMPTY, FACE_FRONT, FACE_LEFT, FACE_RIGHT, PREVIEW;
  }

  public static class AppResultFrame {
    public AppFrameDetalle detalle;
    //    public int bufferSize;
    public byte buffer[];
    public String bufferBase64; //tmp
  }

  public static class AppInfo {
    public AppStatus status;
    public AppFrameFormat frameFormat;
    public AppResultFrame framePreview;
    public String bufferBase64;
  }

  //internal
  public enum WebcamPixelFormat {
    MJPEG(1),
    RGBA(10), BGRA(11), ARGB(12), ABGR(13),
    RGB(20), BGR(21),
    NV21(30), NV12(31),
    RGB565(40),
    YUY2(50), YV12(51), YUV422(52),
    UNKNOW(-1);

    private int valor;

    private WebcamPixelFormat(int v) {
      this.valor = v;
    }

    public int getValor() {
      return valor;
    }
  }

  public enum WebcamOrientation {
    PORTRAIT(1), LANDSCAPE(2), PORTRAIT_FLIPPED(3), LANDSCAPE_FLIPPED(4);

    private int valor;

    private WebcamOrientation(int v) {
      this.valor = v;
    }

    public int getValor() {
      return valor;
    }
  }

  //RGB Fake Frames
//  private final boolean useRGBFakeFrame = false;
//  private int rgbFrameWidth = 160;
//  private int rgbFrameHeight = 160;
//  private byte rgbFrames[][] = null;
//  private int rgbFrameSize = 0;
//  private WebcamPixelFormat rgbFrameFormat = null;


  public boolean webcamInitializated = false;
  public boolean webcamInitializaOnResume = false;
  public AtomicBoolean webcamProcessing = new AtomicBoolean(false);
  public int webcamWidth = 0;
  public int webcamHeight = 0;
  public int webcamPixelFmt = 0;
  public int webcamBufferSize = 0;
  public WebcamOrientation currentOrientation = null;

  public boolean toggleWebCam() {
    if (cameraView != null) {
      if (webcamInitializated) {
        if (closeWebcam()) {
          cameraView.stop();
        }
      } else {
        if (ActivityCompat.checkSelfPermission(sobioSelfieLivenessActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
          cameraView.start();
        } else {
          String[] permissions = {Manifest.permission.CAMERA};
          ActivityCompat.requestPermissions(sobioSelfieLivenessActivity, permissions, 1);
        }
      }
      return true;
    }
    return false;
  }

  public void processWebcamFrame(Image image) {
    WebcamOrientation newOrientation = WebcamOrientation.PORTRAIT;
    byte[] data = com.andromeda.latam.sobio.component.camera2.ImageUtil.YUV_420_888toNV21(image);
    int rotation = sobioSelfieLivenessActivity.getWindowManager().getDefaultDisplay().getRotation();
    switch (rotation) {
      case Surface.ROTATION_0:
        newOrientation = WebcamOrientation.PORTRAIT;
        break;
      case Surface.ROTATION_90:
        newOrientation = WebcamOrientation.LANDSCAPE;
        break;
      case Surface.ROTATION_180:
        newOrientation = WebcamOrientation.PORTRAIT_FLIPPED;
        break;
      case Surface.ROTATION_270:
        newOrientation = WebcamOrientation.LANDSCAPE_FLIPPED;
        break;
      default:
        newOrientation = WebcamOrientation.PORTRAIT;
        break;
    }

    if (!webcamInitializated && !webcamProcessing.getAndSet(true)) {
      try {
        webcamWidth = image.getWidth();
        webcamHeight = image.getHeight();
        switch (ImageFormat.NV21) {
          case ImageFormat.JPEG:
            webcamPixelFmt = WebcamPixelFormat.MJPEG.getValor();
            break;
          case ImageFormat.FLEX_RGBA_8888:
            webcamPixelFmt = WebcamPixelFormat.RGBA.getValor();
            break;
          case ImageFormat.FLEX_RGB_888:
            webcamPixelFmt = WebcamPixelFormat.RGB.getValor();
            break;
          case ImageFormat.NV21:
            webcamPixelFmt = WebcamPixelFormat.NV21.getValor();
            break;
          case ImageFormat.NV16:
            webcamPixelFmt = WebcamPixelFormat.NV12.getValor();
            break;
          case ImageFormat.RGB_565:
            webcamPixelFmt = WebcamPixelFormat.RGB565.getValor();
            break;
          case ImageFormat.YUY2:
            webcamPixelFmt = WebcamPixelFormat.YUY2.getValor();
            break;
          case ImageFormat.YV12:
            webcamPixelFmt = WebcamPixelFormat.YV12.getValor();
            break;
          case ImageFormat.YUV_422_888:
            webcamPixelFmt = WebcamPixelFormat.YUV422.getValor();
            break;

          default:
            webcamPixelFmt = WebcamPixelFormat.UNKNOW.getValor();
            Log.i(TAG, "format unsupported: " + image.getFormat());
            break;
        }
        webcamBufferSize = data.length;
        if (sobioSelfieLivenessActivity.nativeWebcamNotifyContextInfo(webcamWidth, webcamHeight, webcamPixelFmt, newOrientation.getValor(), true, webcamBufferSize)) {
          currentOrientation = newOrientation;
          Log.i(TAG, "nativeWebcamNotifyContextInfo success");
          if (sobioSelfieLivenessActivity.nativeWebcamNotifyStreamOn()) {
            Log.i(TAG, "nativeWebcamNotifyStreamOn success");
          } else {
            Log.i(TAG, "nativeWebcamNotifyStreamOn fail");
          }
          webcamInitializated = true;
        } else {
          Log.e(TAG, "nativeWebcamNotifyContextInfo error");
          webcamInitializated = false;
        }
      } finally {
        synchronized (webcamProcessing) {
          webcamProcessing.set(false);
          webcamProcessing.notifyAll();
        }
      }
    } else {
      if (webcamInitializated && newOrientation != currentOrientation && !webcamProcessing.get()) {
        if (sobioSelfieLivenessActivity.nativeWebcamNotifyUpdateContextInfo(newOrientation.getValor(), true)) {
          currentOrientation = newOrientation;
        }
      } else if (webcamInitializated && !webcamProcessing.get() && data != null && data.length > 0) {
        if (saveRawFrames) {
          if (ActivityCompat.checkSelfPermission(sobioSelfieLivenessActivity, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(sobioSelfieLivenessActivity, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            writeFileExternalStorage("frame", image, currentOrientation);
          } else {
            ActivityCompat.requestPermissions(sobioSelfieLivenessActivity, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISIONS_DEV_REQUEST_CODE);
          }
        }
        sobioSelfieLivenessActivity.nativeWebcamUpdateFrame(data,data.length);
      }
    }
  }

  boolean saveRawFrames = false;
  int maxFiles = 30 * 3;
  int idxFile = 0;

  void writeFileExternalStorage(String prefix, Image image, WebcamOrientation orientation) {
    byte[] data = com.andromeda.latam.sobio.component.camera2.ImageUtil.YUV_420_888toNV21(image);
    //Checking the availability state of the External Storage.
    String state = Environment.getExternalStorageState();
    if (!Environment.MEDIA_MOUNTED.equals(state)) {
      //If it isn't mounted - we can't write into it.
      return;
    }

    String strIdx = String.format("%06d", idxFile);
    String filenameExternal = prefix + "_" + strIdx + "_" + image.getWidth() + "x" + image.getHeight() + "_" + image.getFormat() + "_" + orientation.name() + ".dat";
    idxFile = (idxFile + 1) % maxFiles;

    //Create a new file that points to the root directory, with the given name:
//        File file = new File(getExternalFilesDir(null), filenameExternal);
    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sobioDev");
    if (!directory.exists()) {
      directory.mkdir();
    }
    File file = new File(directory, filenameExternal);
//    Log.i(TAG, "frame target on: " + directory.getAbsolutePath());
//    Log.i(TAG, "frame name proposal: " + filenameExternal);
    //This point and below is responsible for the write operation
    FileOutputStream outputStream = null;
    try {
      file.createNewFile();
      //second argument of FileOutputStream constructor indicates whether
      //to append or create new file if one exists
      outputStream = new FileOutputStream(file, false);

      outputStream.write(data);
      outputStream.flush();
//      Log.i(TAG, "frame saved on: " + filenameExternal);
      Log.i(TAG, "frame saved on: " + file.getAbsolutePath());
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

//  public void openWwebcam() {
//    if (cameraView != null && !cameraView.isCameraOpened()) {
//      if (webcamInitializaOnResume) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//          cameraView.start();
//        }
//      }
//    }
//
//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          Thread.sleep(3000);
//        } catch (InterruptedException e) {
//        }
//        nativeSobioIniciarDeteccion();
//      }
//    }).start();
//
//  }

  public boolean closeWebcam() {
    if (!webcamInitializated)
      return true;
//    if (useRGBFakeFrame) {
//      if (SDLActivity.nativeWebcamNotifyStreamOff()) {
//        SDLActivity.nativeWebcamNotifyCleanContextInfo();
//        webcamInitializated = false;
//        return true;
//      }
//    } else {
    while (!webcamProcessing.getAndSet(true)) {
      synchronized (webcamProcessing) {
        try {
          webcamProcessing.wait(500);
        } catch (InterruptedException e) {
        }
      }
//      }
//      if (webcamInitializated && !webcamProcessing.getAndSet(true)) {
      if (webcamInitializated) {
        try {
          if (sobioSelfieLivenessActivity.nativeWebcamNotifyStreamOff()) {
            sobioSelfieLivenessActivity.nativeWebcamNotifyCleanContextInfo();
            webcamInitializated = false;
            return true;
          } else {
            Log.w(TAG, "nativeWebcamNotifyStreamOff was FALSE");
            sobioSelfieLivenessActivity.nativeWebcamNotifyCleanContextInfo();
            webcamInitializated = false;
            return true;
          }
        } finally {
          synchronized (webcamProcessing) {
            webcamProcessing.set(false);
            webcamProcessing.notifyAll();
          }
        }
      }
    }
    return false;
  }

}
