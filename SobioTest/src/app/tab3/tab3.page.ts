import { Component, OnInit } from '@angular/core';
import { EnvService } from '../core/services/env.service';
import { ModalService } from '../core/services/modal.service';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page implements OnInit{

  protocolo: string;
  url: string;
  constructor(private envService: EnvService, private modalService: ModalService) {}

  ngOnInit(){
    this.protocolo = this.envService.getProtocolo();
    this.url = this.envService.getUrl();
  }

  aplicarConfiguracion(){
    this.envService.setProtocolo(this.protocolo);
    this.envService.setUrl(this.url);
    this.modalService.showMensaje("Nueva configuración", "La nueva url es: " + this.protocolo+this.url);
  }
}
