import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Token, BalanceItem, Result, WINDOW, User} from '@/models';
import { Observable } from 'rxjs/Observable';
import { BehaviorSubject } from 'rxjs';
import {EnvService} from '@/services';

@Injectable()
export class TokenService {

  private readonly tokensUrl: string;
  private readonly balanceUrl: string;
  private tokens: Token[];
  private currentTokensSubject: BehaviorSubject<Token[]>;
  public currentTokensObservable: Observable<Token[]>;
  private currentToken: Token;
  private currentTokenSubject: BehaviorSubject<Token>;
  public currentTokenObservable: Observable<Token>;

  constructor(private http: HttpClient,
              private env: EnvService,
              @Inject(WINDOW) private window: Window) {
    const port = env.javaPort;
    this.tokensUrl = 'http://' + this.window.location.hostname + ':' + port + '/tokens/';
    this.balanceUrl = 'http://' + this.window.location.hostname + ':' + port + '/balance/';
    this.currentTokenSubject = new BehaviorSubject<Token>(new Token());
    this.currentTokenObservable = this.currentTokenSubject.asObservable();
    this.currentTokensSubject = new BehaviorSubject<Token[]>(null);
    this.currentTokensObservable = this.currentTokensSubject.asObservable();
  }

  public getCurrentToken(): Token {
    return this.currentToken;
  }
  private findAll(): Observable<Token[]> {
    const response = this.http.get<Token[]>(this.tokensUrl);
    return response;
  }

  public refreshTokens() {
    this.findAll().subscribe(data => {
      this.tokens = data;
      if (this.tokens.length > 0) {
        this.currentToken = this.tokens[0];
      } else {
        this.currentToken = new Token();
      }
      this.currentTokensSubject.next(this.tokens);
      this.currentTokenSubject.next(this.currentToken);
    });
  }

  public save(token: Token): Observable<Result> {
    const result = this.http.post<Result>(this.tokensUrl, token);
    return result;
  }

  public mint(currentUser: User, quantity: number) {
    const url = this.tokensUrl + 'mint/' + this.currentToken.name + '/' + currentUser.name + '/' + quantity;
    const result = this.http.post<Result>(url, '');
    return result;
  }

  public burn(currentUser: User, quantity: number) {
    const url = this.tokensUrl + 'burn/' + this.currentToken.name + '/' + currentUser.name + '/' + quantity;
    const result = this.http.post<Result>(url, '');
    return result;
  }

  public transfer(currentUser: User, quantity: number, toAccount: string) {
    const url = this.tokensUrl + 'transfer/' + this.currentToken.name + '/' + currentUser.name + '/' + quantity + '/' + toAccount;
    const result = this.http.post<Result>(url, '');
    return result;
  }

  public transferFrom(currentUser: User, quantity: number, toAccount: string) {
    const url = this.tokensUrl + 'transferfrom/' + this.currentToken.name + '/' + currentUser.name + '/' + quantity + '/' + toAccount;
    const result = this.http.post<Result>(url, '');
    return result;
  }


  public getBalances(tokenName: string): Observable<BalanceItem[]> {
    if (tokenName === undefined) {
      return new Observable<BalanceItem[]>();
    } else {
      const response = this.http.get<BalanceItem[]>(this.balanceUrl + tokenName);
      return response;
    }
  }

  public setCurrentTokenById(tokenId: number) {
    this.currentToken = this.tokens.find(x => x.id === tokenId);
    this.currentTokenSubject.next(this.currentToken);
  }
  public setCurrentTokenByName(tokenName: string) {
    if (tokenName === undefined) {
      this.currentToken = new Token();
      return;
    } else {
      this.currentToken = this.tokens.find(x => x.name === tokenName);
      this.currentTokenSubject.next(this.currentToken);
    }
  }
}
