import { Component, OnInit } from '@angular/core';
import { Token, BalanceItem } from '@/models';
import { TokenService, AuthenticationService } from '@/services';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-token-list',
  templateUrl: './token-list.component.html',
  styleUrls: ['./token-list.component.css']
})
export class TokenListComponent implements OnInit {
  tokens: Token[];
  currentToken: Token = new Token();
  balances: BalanceItem[];

  constructor(private tokenService: TokenService,
              private authenticationService: AuthenticationService,
              private router: Router
  ) {
    this.tokenService.currentTokenObservable.subscribe(
      x => {
        this.currentToken = x;
        this.tokenService.getBalances(this.currentToken.name).subscribe(data => {
          this.balances = data;
          const tokenBalanceItem = new BalanceItem();
          tokenBalanceItem.balance = this.currentToken.balance;
          tokenBalanceItem.user = 'Token';
          this.balances.push(tokenBalanceItem);
        });
      }
    );
    this.tokenService.currentTokensObservable.subscribe(
      x => this.tokens = x
    );
    // redirect to home if not logged in
    if (! this.authenticationService.currentUserValue) {
      this.router.navigate(['/']);
    }


  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.tokenService.refreshTokens();
  }

  chooseToken(tokenId): void {
    this.tokenService.setCurrentTokenById(tokenId);
    this.currentToken = this.tokenService.getCurrentToken();
  }
}
