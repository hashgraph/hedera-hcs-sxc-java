import {Inject, Injectable} from '@angular/core';
import {Token, WINDOW} from '@/models';
import {BehaviorSubject} from 'rxjs';
import {Observable} from 'rxjs/Observable';
import {Operation} from '@/models/operation';
import {HttpClient} from '@angular/common/http';
import {EnvService} from '@/services/env.service';

@Injectable({
  providedIn: 'root'
})
export class OperationServiceService {
  private readonly operationsUrl: string;
  private operations: Operation[];
  private operationsSubject: BehaviorSubject<Operation[]>;
  public operationsObservable: Observable<Operation[]>;

  constructor(private http: HttpClient,
              private env: EnvService,
              @Inject(WINDOW) private window: Window) {
    const port = env.javaPort;
    this.operationsUrl = 'http://' + this.window.location.hostname + ':' + port + '/operations/';
    this.operationsSubject = new BehaviorSubject<Operation[]>(null);
    this.operationsObservable = this.operationsSubject.asObservable();
  }
  private findAll(userName: string): Observable<Operation[]> {
    const response = this.http.get<Operation[]>(this.operationsUrl + userName);
    return response;
  }

  public refreshOperations(userName: string) {
    this.findAll(userName).subscribe(data => {
      this.operations = data;
      this.operationsSubject.next(this.operations);
    });
  }
}
