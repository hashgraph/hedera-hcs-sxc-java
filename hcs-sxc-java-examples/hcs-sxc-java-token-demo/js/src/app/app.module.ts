import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import { routing } from './app-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppComponent } from './app.component';
import {TokenService, UsersService} from '@/services';
import { TokenListComponent, TokenFormComponent } from './components';
import { AlertComponent, LoginComponent, HomeComponent, TokenMintComponent } from '@/components';
import { TokenBurnComponent, TokenTransferComponent, TokenTransferFromComponent } from '@/components';
import { JwtInterceptor, ErrorInterceptor } from '@/helpers';
import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {myRxStompConfig} from '@/my-rx-stomp.config';
import {WINDOW_PROVIDERS} from '@/models';
import {EnvServiceProvider} from '@/providers';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';

@NgModule({
  declarations: [
    AppComponent,
    TokenListComponent,
    TokenFormComponent,
    AlertComponent,
    HomeComponent,
    LoginComponent,
    TokenMintComponent,
    TokenBurnComponent,
    TokenTransferComponent,
    TokenTransferFromComponent,
    TransactionListComponent
  ],
  imports: [
    BrowserModule,
    routing,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    EnvServiceProvider,
    WINDOW_PROVIDERS,
    {
      provide: InjectableRxStompConfig,
      useValue: myRxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    },
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    TokenService,
    UsersService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

