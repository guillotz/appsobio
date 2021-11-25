import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, finalize } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';
import { ModalService } from '../services/modal.service';
import { Router } from '@angular/router';
@Injectable()
export class Interceptor implements HttpInterceptor {
    private reqs = 0;
    constructor(private loaderService: LoaderService, private modalService: ModalService, private router: Router) { }
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        this.loaderService.show()
        //   console.log(`SOY EL INTERCEPTOR - ${req.url}`);
        var jsonReq: HttpRequest<any> = req.clone({
            setHeaders: { 'Access-Control-Allow-Origin': '*' }
        })
        jsonReq = jsonReq.clone({
            setHeaders: { 'Content-Type': 'application/json' }
        });
        jsonReq = jsonReq.clone(
            { headers: jsonReq.headers.set('Accept', '*/*') }
        );
        jsonReq = jsonReq.clone(
            { headers: jsonReq.headers.set('Access-Control-Allow-Headers', 'Authorization, Expires, Pragma, DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range') }
        );

        this.reqs++;
        return next.handle(jsonReq).pipe(
            map((event: HttpEvent<any>) => {
                if (event instanceof HttpResponse) {
                    this.restartReqs();
                    if (event.status != 200) {
                        //  Porcion de código para realizar cuando venga un error nuestro 
                    }
                }
                return event;
            }),
            catchError((error: HttpErrorResponse) => {
                console.log("ERROR: ", error);
                this.restartReqs();
                this.loaderService.hide();

                if (error.status == 403) {
                    this.router.navigate(['error']);
                    return throwError(error);
                }
                this.modalService.showError(error);
                return throwError(error);

            }),
            /*
            finalize(() => {
                this.loaderService.hide();
            })
            */
        );
    }
    restartReqs() {
        this.reqs--;
        if (this.reqs <= 0) {
            this.loaderService.hide();
        }
    }
}