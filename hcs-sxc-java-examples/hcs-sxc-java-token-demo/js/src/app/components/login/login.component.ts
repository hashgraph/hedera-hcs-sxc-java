import {Component, Inject, OnInit} from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import {first, map} from 'rxjs/operators';
import {App, User, WINDOW} from '@/models/';
import {AppService, EnvService} from '@/services';

import { AlertService, AuthenticationService, UsersService } from '@/services';
import {HttpClient} from '@angular/common/http';

@Component({templateUrl: 'login.component.html'})
export class LoginComponent implements OnInit {
    loginForm: FormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    app: App;
    title: string;
    users: User[];

    constructor(
        private http: HttpClient,
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private authenticationService: AuthenticationService,
        private alertService: AlertService,
        private env: EnvService,
        private appService: AppService,
        private usersService: UsersService,
        @Inject(WINDOW) private window: Window
    ) {
      this.usersService.nodeUsers.subscribe(
        x => this.users = x
      );
        // redirect to home if already logged in
      if (this.authenticationService.currentUserValue) {
          this.router.navigate(['/']);
      }
    }

    ngOnInit() {
      this.appService.getDetails().subscribe(data => {
        this.app = data;
        this.title = 'Welcome to ' + this.app.applicationName + ' digital bank.';
      });
      this.loginForm = this.formBuilder.group({
        username: ['', Validators.required]
      });

      // get return url from route parameters or default to '/'
      // this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
      this.returnUrl = '/';
    }

    // convenience getter for easy access to form fields
    get f() { return this.loginForm.controls; }

    onSubmit() {
        this.submitted = true;

        // stop here if form is invalid
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.authenticationService.login(this.f.username.value)
            .pipe(first())
            .subscribe(
                data => {
                    this.router.navigate([this.returnUrl]);
                },
                error => {
                    this.alertService.error(error);
                    this.loading = false;
                });
    }
}
