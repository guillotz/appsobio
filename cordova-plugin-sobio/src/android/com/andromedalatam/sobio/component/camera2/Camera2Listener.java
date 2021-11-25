package com.andromedalatam.sobio.component.camera2;

import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.util.Size;

public interface Camera2Listener {
    /**
     * 当打开时执行
     * @param cameraDevice 相机实例
     * @param cameraId 相机ID
     * @param displayOrientation 相机预览旋转角度
     * @param isMirror 是否镜像显示
     */
    void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror);

    void onPreview(Image image);

    /**
     * 当相机关闭时执行
     */
    void onCameraClosed();

    /**
     * 当出现异常时执行
     * @param e 相机相关异常
     */
    void onCameraError(Exception e);

}
