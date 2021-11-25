export interface SobioPlugin {
    launch(mode: ConfigDetectionMode, showGestureIndications: Boolean, flagsDetection: ConfigGestures, key: string): Promise<LaunchResult>;
    captureDNI(titulo?: string, descripcion?: string): Promise<DNIPicture>;
}

export enum ConfigDetectionMode {
    FACE_ONLY = 'FACE_ONLY',
    FACE_LIVENESS_SIMPLE = 'FACE_LIVENESS_SIMPLE',
    FACE_LIVENESS_CUSTOM = 'FACE_LIVENESS_CUSTOM'
}

export enum ConfigGestures {
    DEFAULT = 'DEFAULT',
    DETECTAR_ROSTRO_FRONTAL = 'DETECTAR_ROSTRO_FRONTAL',
    DETECTAR_ROSTRO_A_IZQUIERDA = 'DETECTAR_ROSTRO_A_IZQUIERDA',
    DETECTAR_ROSTRO_A_DERECHA = 'DETECTAR_ROSTRO_A_DERECHA'
}

export class LaunchResult {
    status: string;
    frameFormat: string;
    bufferBase64: string;
    framePreview: FramePreview;
}

export class FramePreview {
    detalle: string;
    buffer: number[];
}

export class DNIPicture {
    path: string;
    base64: string; // file base64
}

declare var SobioPlugin: SobioPlugin;