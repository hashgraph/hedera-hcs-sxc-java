import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService, TokenService, AuthenticationService } from '@/services';
import {Token, User} from '@/models';

@Component({
  selector: 'app-token-burn',
  templateUrl: './token-burn.component.html',
  styleUrls: ['./token-burn.component.css']
})
export class TokenBurnComponent {

  currentToken: Token;
  currentUser: User;
  quantity: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tokenService: TokenService,
    private authenticationService: AuthenticationService,
    private alertService: AlertService)
  {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.tokenService.currentTokenObservable.subscribe(
      x => this.currentToken = x
    );
    // redirect to home if not logged in
    if (! this.authenticationService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  onSubmit() {
    this.tokenService.burn(this.currentUser, this.quantity)
      .subscribe(
        (data) => {
          this.alertService.success(data.result, true);
        },
        (error) => {
          this.alertService.error(error);
        }
      );
  }
}
