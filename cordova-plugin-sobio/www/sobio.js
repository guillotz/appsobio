const exec = require('cordova/exec');

exports.launch = (mode, showGestureIndications, flagsDetection, key) => {
    return new Promise((res, rej) => {
        exec((winParam) => {
            res(winParam);
        }, (error) => {
            rej(error);
        }, 'SobioPlugin', 'launch', [mode, showGestureIndications, flagsDetection, key]);
    });
}

exports.captureDNI = (titulo, descripcion) => {
    const params = [];
    if(titulo) {
        params.push(titulo)
    }
    if(descripcion) {
        params.push(descripcion)
    }
    return new Promise((res, rej) => {
        exec((winParam) => {
            res(winParam);
        }, (error) => {
            rej(error);
        }, 'SobioPlugin', 'captureDNI', params);
    });
}