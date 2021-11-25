import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { AlertController } from '@ionic/angular';
@Injectable({
  providedIn: 'root'
})
export class ModalService {

  showModal = new Subject<boolean>();
  showPage = new Subject<boolean>();
  mensajeError = new Subject<any>();
  msg = new Subject<any>();
  accion = new Subject<any>();
  accion2 = new Subject<any>();
  timeout: boolean = false;
  constructor(private alertController: AlertController) { }


  async presentAlert() {
    const alert = await this.alertController.create({
      cssClass: 'my-custom-class',
      header: 'Alert',
      subHeader: 'Subtitle',
      message: 'This is an alert message.',
      buttons: ['OK']
    });

    await alert.present();

    const { role } = await alert.onDidDismiss();
    console.log('onDidDismiss resolved with role', role);
  }

  async showError(error) {
    var titulo = "FE 0001";
    var mensaje = "Error de comunicacion con el servidor, revise la configuraci&oacute;n";

    if (error.status > 0) {
      if (error.status != 404) {
        if (error.data != null) {
          var e = error.data.error;
          titulo = e.codigo;
          mensaje = e.descripcion;
        }
      } else {
        mensaje = "Endpoint no encontrado";
      }
    }
    const alert = await this.alertController.create({
      header: titulo,
      message: mensaje,
      buttons: ['OK']
    });
    await alert.present();
    // const { role } = await alert.onDidDismiss();
    // console.log('onDidDismiss resolved with role', role);
  }

  async showMensaje(titulo,mensaje){
    const alert = await this.alertController.create({
      header: titulo,
      message: mensaje,
      buttons: ['Aceptar']
    });
    await alert.present();
  }
}
