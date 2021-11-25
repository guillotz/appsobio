import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  BASE: string;
  constructor(private httpClient: HttpClient, private envService: EnvService) { 
    this.envService.urlCompleta.subscribe(url => {
      this.BASE = url;
    })
  }
  public postTest(obj){
    return this.httpClient.post("https://httpbin.org/post", obj);
  }

  public get(path: string) {
    return this.httpClient.get(this.BASE + path);
  }

  public prueba(){
    return this.httpClient.get("http://192.168.0.194:9001/be-registracion-service/api/v1/persona/1/buscar-idPersonaCrecer");
  }
  public post(path: string, data: any) {
    return this.httpClient.post(this.BASE + path, JSON.stringify(data));
  }

  public put(path: string, data: any) {
    return this.httpClient.put(this.BASE + path, JSON.stringify(data));
  }
  public delete(path: string) {
    return this.httpClient.delete(this.BASE + path);
  }
}
