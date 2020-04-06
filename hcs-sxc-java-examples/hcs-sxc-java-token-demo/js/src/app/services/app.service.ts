import {Inject, Injectable} from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {App, WINDOW} from '@/models';
import { Observable } from 'rxjs/Observable';
import {EnvService} from '@/services';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  private readonly appUrl: string;

  constructor(private http: HttpClient,
              private env: EnvService,
              @Inject(WINDOW) private window: Window) {
    const port = env.javaPort;

    this.appUrl = 'http://' + this.window.location.hostname + ':' + port + '/application';
    sessionStorage.clear();
  }

  public getDetails(): Observable<App> {
    return this.http.get<App>(this.appUrl);
  }
}
