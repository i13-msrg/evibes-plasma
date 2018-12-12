import { TestBed } from '@angular/core/testing';

import { PlasmaEventBusService } from './plasma.eventbus.service';

describe('PlasmaEventBusService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PlasmaEventBusService = TestBed.get(PlasmaEventBusService);
    expect(service).toBeTruthy();
  });
});
