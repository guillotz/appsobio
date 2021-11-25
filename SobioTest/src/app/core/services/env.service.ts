import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class EnvService {
  private protocolo: string = "https://";
  private url: string = "be-test.bancocredicoop.coop";
  urlCompleta: Subject<string> = new Subject<string>();
  constructor() { 
    this.urlCompleta.next(this.protocolo + this.url);
  }

  public getProtocolo() {
    return this.protocolo;
  }
  public setProtocolo(protocolo: string) {
    this.protocolo = protocolo;
    this.urlCompleta.next(this.protocolo + this.url);
  }

  public getUrl() {
    return this.url;
  }
  public setUrl(url) {
    this.url = url;
    this.urlCompleta.next(this.protocolo + this.url);
  }

}
