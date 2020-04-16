import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService, TokenService, AuthenticationService } from '@/services';
import {Token, User} from '@/models';

@Component({
  selector: 'app-token-form',
  templateUrl: './token-form.component.html',
  styleUrls: ['./token-form.component.css']
})
export class TokenFormComponent {

  token: Token;
  currentUser: User;

  constructor(
      private route: ActivatedRoute,
      private router: Router,
      private tokenService: TokenService,
      private authenticationService: AuthenticationService,
      private alertService: AlertService) {
    this.token = new Token();
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    // redirect to home if not logged in
    if (! this.authenticationService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  onSubmit() {
    this.token.template = 'Demo Token Template';
    this.token.owner = this.currentUser.name;
    this.tokenService.save(this.token)
    .subscribe(
      (data) => {
        this.alertService.success(data.result, true);
        this.gotoTokenList();
      },
      (error) => {
          this.alertService.error(error);
      }
    );
  }

  gotoTokenList() {
    this.router.navigate(['/tokens']);
  }

}
