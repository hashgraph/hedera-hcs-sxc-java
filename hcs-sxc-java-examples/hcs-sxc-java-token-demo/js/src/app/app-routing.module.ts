import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { AuthGuard } from './guards';
import {
  TokenListComponent,
  TokenFormComponent,
  TokenMintComponent,
  TokenBurnComponent,
  TokenTransferComponent,
  TokenTransferFromComponent,
  TransactionListComponent
} from './components';

const appRoutes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'tokens', component: TokenListComponent, runGuardsAndResolvers: 'always' },
  { path: 'addtoken', component: TokenFormComponent, runGuardsAndResolvers: 'always' },
  { path: 'mintToken', component:  TokenMintComponent, runGuardsAndResolvers: 'always'},
  { path: 'burnToken', component:  TokenBurnComponent, runGuardsAndResolvers: 'always'},
  { path: 'transfer', component:  TokenTransferComponent, runGuardsAndResolvers: 'always'},
  { path: 'transferFrom', component: TokenTransferFromComponent, runGuardsAndResolvers: 'always'},
  { path: 'operations', component: TransactionListComponent, runGuardsAndResolvers: 'always'}
];

export const routing = RouterModule.forRoot(appRoutes, {onSameUrlNavigation: 'reload'});
