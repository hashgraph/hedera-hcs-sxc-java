import { Component, OnInit } from '@angular/core';
import {Operation} from '@/models/operation';
import {AuthenticationService, TokenService} from '@/services';
import {Router} from '@angular/router';
import {OperationServiceService} from '@/services';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit {
  operations: Operation[];

  constructor(private operationService: OperationServiceService,
              private authenticationService: AuthenticationService,
              private router: Router) {

    this.operationService.operationsObservable.subscribe(
      x => this.operations = x
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
    this.operationService.refreshOperations(this.authenticationService.currentUserValue.name);
  }

}
