import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenBurnComponent } from './token-burn.component';

describe('TokenBurnComponent', () => {
  let component: TokenBurnComponent;
  let fixture: ComponentFixture<TokenBurnComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TokenBurnComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenBurnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
