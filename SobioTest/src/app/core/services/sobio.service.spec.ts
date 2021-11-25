import { TestBed } from '@angular/core/testing';

import { SobioService } from './sobio.service';

describe('SobioService', () => {
  let service: SobioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SobioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
