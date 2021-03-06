package com.andromeda.latam.sobio.id;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public enum FileHelper {
  INSTANCE;

  public String getBase64File(String path, Context context, boolean internal) throws IOException {
    Uri uPath = Uri.parse(path);
    FileInputStream fileInputStream;
    if(internal) {
      File file = new File(getRealPath(uPath, context));
      fileInputStream = new FileInputStream(file);
    } else {
      File file = new File(getImageUrlWithAuthority(context, uPath));
      fileInputStream = new FileInputStream(file);
    }
    byte[] bytes = getBytes(fileInputStream);
    return Base64.encodeToString(bytes, Base64.DEFAULT);
  }

  private byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }

  public String getRealPath(Uri uri, Context context) {
    if (isExternalStorageDocument(uri)) {
      final String docId = DocumentsContract.getDocumentId(uri);
      final String[] split = docId.split(":");
      final String type = split[0];
      if ("primary".equalsIgnoreCase(type)) {
        return Environment.getExternalStorageDirectory() + "/" + split[1];
      }
    } else if (isDownloadsDocument(uri)) {
      final String id = DocumentsContract.getDocumentId(uri);
      final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
      return getDataColumn(context, contentUri, null, null);
    } else if (isMediaDocument(uri)) {
      final String docId = DocumentsContract.getDocumentId(uri);
      final String[] split = docId.split(":");
      final String type = split[0];
      Uri contentUri = null;
      if ("image".equals(type)) {
        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
      } else if ("video".equals(type)) {
        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
      } else if ("audio".equals(type)) {
        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
      }
      final String selection = "_id=?";
      final String[] selectionArgs = new String[] {split[1]};
      return getDataColumn(context, contentUri, selection, selectionArgs);
    } else if (isGooglePhotosUri(uri))
      return uri.getLastPathSegment();
    else if ("file".equalsIgnoreCase(uri.getScheme()))
      return uri.getPath();
    return uri.toString();
  }

  public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = { column };
    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        final int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(index);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
    return null;
  }

  public boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  public boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  public boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  public boolean isGooglePhotosUri(Uri uri) {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }

  private static String getImageUrlWithAuthority(Context context, Uri uri) {
    InputStream is = null;
    if (uri.getAuthority() != null) {
      try {
        is = context.getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        return writeToTempImageAndGetPathUri(context, bmp).toString();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        try {
          if (is != null) {
            is.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  private static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    return Uri.parse(path);
  }
}
