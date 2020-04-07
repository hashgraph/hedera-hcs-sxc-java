import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenTransferComponent } from './token-transfer.component';

describe('TokenTransferComponent', () => {
  let component: TokenTransferComponent;
  let fixture: ComponentFixture<TokenTransferComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TokenTransferComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenTransferComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
