package com.andromedalatam.sobio.id;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public enum CaptureImageHelper {
  INSTANCE;

  private static final String TAG = "HelperCameraXGFG";

  public Uri handleSamplingAndRotationBitmap(Context context, Uri selectedImage) {
    try {
      Bitmap image = getCapturedImage(context, selectedImage);
      OutputStream imageStream = context.getContentResolver().openOutputStream(selectedImage);
      Bitmap rotated = rotateImageIfRequired(image, selectedImage);
      rotated.compress(Bitmap.CompressFormat.JPEG, 100, imageStream);
      imageStream.close();;
    } catch(IOException ioe) {
      Log.e(TAG, "Error handling sampling and rotation: ", ioe);
    }
    return selectedImage;
  }

  public Bitmap rotateImageIfRequired(Bitmap image, Uri selectedImage) throws IOException {
    ExifInterface exf = new ExifInterface(selectedImage.getPath());
    int orientation = exf.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    if(orientation == ExifInterface.ORIENTATION_ROTATE_90) {
      return rotateImage(image, 90f);
    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
      return rotateImage(image, 180f);
    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
      return rotateImage(image, 270f);
    } else {
      return image;
    }
  }

  public Bitmap rotateImage(Bitmap image, float degree) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    Bitmap result = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    image.recycle();
    return result;
  }

  public Bitmap getCapturedImage(Context context, Uri selectedPhotoUri) throws IOException {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), selectedPhotoUri);
      return ImageDecoder.decodeBitmap(source);
    } else {
      return MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedPhotoUri);
    }
  }

}
