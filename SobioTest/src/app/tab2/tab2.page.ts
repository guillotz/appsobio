import { Component } from '@angular/core';
import { SobioService } from '../core/services/sobio.service';
import { DomSanitizer } from '@angular/platform-browser';
@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page {

  constructor(private sobioService: SobioService, public sanitizer: DomSanitizer) { }
  objInfo: any = {};

  scanDniLocal() {
    this.sobioService.capturarDNI("FRENTE DNI", "UBIQUE EL FRENTE DEL DNI EN LA PANTALLA")
      .then(data => {
        this.objInfo.frenteDNI = "data:image/jpeg;base64," + data;
        this.sobioService.capturarDNI("DORSO DNI", "UBIQUE EL DORSO DEL DNI EN LA PANTALLA")
          .then(data => {
            this.objInfo.dorsoDNI = "data:image/jpeg;base64," + data;
          });
      });
      console.log("OBJ INFO: ", this.objInfo);
  }
  testPruebaVida() {
    this.sobioService.pruebaDeVida('FACE_LIVENESS_SIMPLE', true, 'DEFAULT', 'default_aes128_key').then(
      data => {
        console.log("DATOS: ", data);
      }
    )
    
  }
}
