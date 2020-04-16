import { Component, OnInit } from '@angular/core';
import {AlertService, AppService, AuthenticationService, TokenService } from './services';
import { Router } from '@angular/router';
import { User, App, Token, Notification } from '@/models/';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  currentUser: User;
  currentToken: Token = new Token();
  app: App;
  title: string;
  topicId: string;

  constructor(private router: Router,
              private authenticationService: AuthenticationService,
              private tokenService: TokenService,
              private appService: AppService,
              private titleService: Title
) {
    this.authenticationService.currentUser
      .subscribe(x => this.currentUser = x);
    this.tokenService.currentTokenObservable
      .subscribe(
      x => this.currentToken = x
    );
  }

  ngOnInit(): void {
    this.appService.getDetails().subscribe(data => {
      this.app = data;
      this.title = 'Welcome to ' + this.app.applicationName + ' digital bank.';
      this.topicId = this.app.topicId;
      this.titleService.setTitle(this.app.applicationName + ' - Hedera Tokenization');
    });
  }

  logout() {
      this.authenticationService.logout();
      this.router.navigate(['/login']);
  }
}
