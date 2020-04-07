import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { User, App } from '@/models/';
import { AppService, AuthenticationService } from '@/services';
import { Router } from '@angular/router';

@Component({ templateUrl: 'home.component.html' })
export class HomeComponent implements OnInit, OnDestroy {
  currentUser: User;
  currentUserSubscription: Subscription;
  app: App;
  title: string;

constructor(
        private authenticationService: AuthenticationService,
        private router: Router,
        private appService: AppService
    ) {
        this.currentUserSubscription = this.authenticationService.currentUser.subscribe(user => {
            this.currentUser = user;
        });
    }

    ngOnInit() {
      this.appService.getDetails().subscribe(data => {
        this.app = data;
        this.title = 'Welcome to ' + this.app.applicationName + ' digital bank.';
      });
    }

    ngOnDestroy() {
        // unsubscribe to ensure no memory leaks
        this.currentUserSubscription.unsubscribe();
    }
    logout() {
        this.authenticationService.logout();
        this.router.navigate(['/login']);
    }
}
