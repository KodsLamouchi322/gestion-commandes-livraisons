import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Transporteurs } from './transporteurs';

describe('Transporteurs', () => {
  let component: Transporteurs;
  let fixture: ComponentFixture<Transporteurs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Transporteurs],
    }).compileComponents();

    fixture = TestBed.createComponent(Transporteurs);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
