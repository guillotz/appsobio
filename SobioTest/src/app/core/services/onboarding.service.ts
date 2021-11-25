import { Injectable } from '@angular/core';
import { HttpService } from './http.service';

@Injectable({
  providedIn: 'root'
})
export class OnboardingService {
  endpointBase: string = "/persona/";
  finUrlConfirmar = "/confirmar-identidad-con-template";
  endpointDni: string = "/be-registracion-service/api/v1/leer-dni";
  constructor(private httpService: HttpService) { }


  enviarDni(request){
    return this.httpService.post(this.endpointDni, request);
  }

  confirmarIdentidad(tokenID){
    let url = this.endpointBase + tokenID + this.finUrlConfirmar;
    return this.httpService.get(url);
  }
}
