import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenTransferFromComponent } from './token-transfer-from.component';

describe('TokenTransferFromComponent', () => {
  let component: TokenTransferFromComponent;
  let fixture: ComponentFixture<TokenTransferFromComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TokenTransferFromComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenTransferFromComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
