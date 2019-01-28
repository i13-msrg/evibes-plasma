import { Component } from '@angular/core';
import { PlasmaEventBusService } from 'src/plasma/services/plasma.eventbus.service';
import { Store } from '@ngrx/store';
import * as PlasmaAction from 'src/plasma/plasma.actions';
import { AppState } from './app.state';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Plasma Simulator';

  constructor(private store: Store<AppState>,
              private plasmaEventBusService: PlasmaEventBusService) {
     this.store.dispatch(new PlasmaAction.Connect());
    // if (plasmaEventBusService.connected()) {
    //   plasmaEventBusService.configureSimulation();
    // } else {
    //   console.log('not connected');
    // }
  }

  onClick() {
    console.log('click');
  }
}
