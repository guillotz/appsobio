package com.andromeda.latam.sobio.component;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

public class SobioSelfieLivenessResultLauncherParams {

  public enum ConfigDetectionMode {
    FACE_ONLY(1),
    FACE_LIVENESS_SIMPLE(2),
    FACE_LIVENESS_CUSTOM(3);

    private int valor;
    private ConfigDetectionMode(int v) {
      this.valor = v;
    }

    public int getValor() {
      return this.valor;
    }
  }

  public enum ConfigGestures {
    DEFAULT(0x0000),
    DETECTAR_ROSTRO_FRONTAL(0x0001),
    DETECTAR_ROSTRO_A_IZQUIERDA(0x0002),
    DETECTAR_ROSTRO_A_DERECHA(0x0004);

    private int valor;
    private ConfigGestures(int v) {
      this.valor = v;
    }

    public int getValor() {
      return this.valor;
    }
  }

  public static class SobioSelfieLivenessConfigParams {
    public String key = null;
    public ConfigDetectionMode mode = ConfigDetectionMode.FACE_LIVENESS_SIMPLE;
    public boolean showGestureIndications = true;
    public ConfigGestures flagsDetection = ConfigGestures.DEFAULT;
  }

}
