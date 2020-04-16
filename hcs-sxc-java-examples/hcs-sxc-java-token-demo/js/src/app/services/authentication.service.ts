import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import { map } from 'rxjs/operators';
import {User, WINDOW} from '@/models';
import {EnvService} from '@/services';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
    private currentUserSubject: BehaviorSubject<User>;
    public currentUser: Observable<User>;
    private usersUrl: string;

    constructor(private http: HttpClient,
                private env: EnvService,
                @Inject(WINDOW) private window: Window) {
      this.usersUrl = 'http://' + this.window.location.hostname + ':' + env.javaPort + '/users/';
      this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(sessionStorage.getItem('currentUser')));
      this.currentUser = this.currentUserSubject.asObservable();
    }

    public get currentUserValue(): User {
        return this.currentUserSubject.value;
    }

    login(username: string) {
      return this.http.get<User>(this.usersUrl + username)
        .pipe(map(user => {
            if (user.name) {
              user.token = 'fake-jwt-token';
              this.currentUserSubject.next(user);
              return user;
            } else {
              return this.error('Username is incorrect');
            }
          }
        ));
    }

  error(message) {
    return throwError({ error: { message } });
  }
  logout() {
    // remove user from local storage to log user out
    this.currentUserSubject.next(null);
  }
}
