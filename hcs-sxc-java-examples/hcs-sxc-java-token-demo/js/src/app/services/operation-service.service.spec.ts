import { TestBed } from '@angular/core/testing';

import { OperationServiceService } from './operation-service.service';

describe('OperationServiceService', () => {
  let service: OperationServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OperationServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
