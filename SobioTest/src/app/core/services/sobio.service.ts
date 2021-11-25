import { Injectable } from '@angular/core';

declare let SobioPlugin: any;

@Injectable({
  providedIn: 'root'
})


export class SobioService {

  constructor() { }

  capturarDNI(titulo: string, descripcion: string): Promise<any> {
    return SobioPlugin.captureDNI(titulo, descripcion).then(d => {
      console.log("CAPTURA DNI: ", d);
      return d.base64;
    }).catch(e => {
      console.error('Error: ', e);
      return e;
    });
  }

  pruebaDeVida(tipo: string, indicaciones: boolean, gestosConfig: string, key: string): Promise<any> {
    // SobioPlugin.launch('FACE_LIVENESS_SIMPLE', true, 'DEFAULT', 'default_aes128_key').then(d => {
    return SobioPlugin.launch(tipo, indicaciones, gestosConfig, key).then(d => {
      console.log("PRUEBA DE VIDA: ", d);
      return d;
    }).catch(e => {
      console.error('E:', e);
      return e;
    });
  }
}
