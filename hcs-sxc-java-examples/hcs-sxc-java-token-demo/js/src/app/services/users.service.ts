import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { BehaviorSubject } from 'rxjs';
import {EnvService} from '@/services';
import {User, WINDOW} from '@/models';

@Injectable()
export class UsersService {

  private readonly usersUrl: string;
  public nodeUsers: Observable<User[]>;
  private nodeUsersSubject: BehaviorSubject<User[]>;
  public allUsers: Observable<User[]>;
  private allUsersSubject: BehaviorSubject<User[]>;

  constructor(private http: HttpClient,
              private env: EnvService,
              @Inject(WINDOW) private window: Window) {
    const port = env.javaPort;
    this.usersUrl = 'http://' + this.window.location.hostname + ':' + port;

    this.nodeUsersSubject = new BehaviorSubject<User[]>(User[0]);
    this.nodeUsers = this.nodeUsersSubject.asObservable();

    this.allUsersSubject = new BehaviorSubject<User[]>(User[0]);
    this.allUsers = this.allUsersSubject.asObservable();

    this.http.get<User[]>(this.usersUrl + '/users/').subscribe(data => {
      this.nodeUsersSubject.next(data);
    });
    this.http.get<User[]>(this.usersUrl + '/allusers/').subscribe(data => {
      this.allUsersSubject.next(data);
    });
  }
}
