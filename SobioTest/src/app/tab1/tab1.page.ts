import { Component } from '@angular/core';
import { HttpService } from '../core/services/http.service';
import { OnboardingService } from '../core/services/onboarding.service';
import { SobioService } from '../core/services/sobio.service';
declare let SobioPlugin: any;
@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page {

  request: any = {
    fotoDorso: '',
    fotoFrente: '',
    tipoCanal: 'BM',
    tokenUUID: ''
  };
  image: string;
  constructor(private sobioService: SobioService, private httpService: HttpService, private onboardingService:OnboardingService) { }

  uuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  iniciarProceso(){
    this.request.tokenUUID = this.uuid();
    this.sobioService.capturarDNI("FRENTE DNI", "Ubicar el frente del dni en la pantalla")
    .then(datos => {
      console.log("DATOS FRENTE: ", datos);
      this.request.fotoFrente = datos;
      this.sobioService.capturarDNI("DORSO DNI", "Ubicar el dni en la pantalla")
      .then(datos => {
        console.log("DATOS DORSO: ", datos);
        this.request.fotoDorso = datos;
          this.onboardingService.enviarDni(this.request).subscribe(data => {
            console.log("DATOS RECIbIDOS: ", data);
          })
        });
    });
  }


  testDNI() {
    this.sobioService.capturarDNI("Tomar foto frente DNI", "Ubicar el dni en la pantalla").then(data => {
      console.log("DATOS: ", data);
    })
  }

  testPruebaVida() {
    // this.sobioService.pruebaDeVida('FACE_LIVENESS_SIMPLE', true, 'DEFAULT', 'default_aes128_key').then(
    //   data => {
    //     console.log("DATOS: ", data);
    //   }
    // )
    SobioPlugin.launch('FACE_LIVENESS_SIMPLE', true, 'DETECTAR_ROSTRO_FRONTAL', 'default_aes128_key').then(d => {
      console.log("PRUEBA DE VIDA: ", d);
    }).catch(e => {
      console.error('E:', e);
    });
  }
  testHttp(){
    this.httpService.prueba().subscribe(data => {alert(data); console.log(data)});
    let obj = {
      param1: 'aa',
      param2: 'bb',
      param3: 'cc',
      param4: 11,
      param5:{param1:'11', param2: '33'}
    }
    this.onboardingService.confirmarIdentidad(22222).subscribe(data => {console.log(data)})
  }
}
