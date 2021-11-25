import { Component } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

declare var SobioPlugin: any;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  public image: string;

  constructor(private sanitizer: DomSanitizer) {}

  buttonTest() {
    SobioPlugin.launch('FACE_LIVENESS_SIMPLE', true, 'DEFAULT', 'default_aes128_key').then(d => {
      console.log(d);
    }).catch(e => {
      console.error('E:', e);
    });
  }

  dniTest() {
    SobioPlugin.captureDNI('abc', 'def').then(d => {
      console.log(d);
      this.image = 'data:image/jpeg;base64,' + d.base64;
    }).catch(e => {
      console.error('E', e);
    });
  }

  getImageUrl() {
    return this.sanitizer.bypassSecurityTrustUrl(this.image);
  }

}
