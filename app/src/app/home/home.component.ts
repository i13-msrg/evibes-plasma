import { Component, OnInit } from '@angular/core';
import { Store, select} from '@ngrx/store';
import { AppState } from '../app.state';
import { selectPlasmaConnected, selectPlasmaBlocks, selectPlasmaSimulationStarted } from 'src/plasma/plasma.selectors';
import * as PlasmaAction from 'src/plasma/plasma.actions';
import { CommonService } from 'src/plasma/services/common.service';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  showSettings = false;
  plasmaBlocks = 0;
  connected = false;
  started = false;

  constructor(private store: Store<AppState>,
              private commonService: CommonService) { }

  ngOnInit() {
    this.store.pipe(select(selectPlasmaConnected)).subscribe(isConnected => {
      this.connected = isConnected;
      if(this.connected) {
        this.commonService.openSnackBar('You are connected to the simulator!', 'Close');
      } else {
        this.commonService.openSnackBar('You are not connected to the simulator!', 'Close');
      }
    });

    this.store.pipe(select(selectPlasmaBlocks)).subscribe(blocks => {
      this.plasmaBlocks = blocks.length;
    });

    this.store.pipe(select(selectPlasmaSimulationStarted)).subscribe(isStarted => {
      if (isStarted === this.started) { return; }
      this.started = isStarted;

      if(isStarted) {
        this.commonService.openSnackBar('Simulation started!', 'Close');
      } else {
        this.commonService.openSnackBar('Simulation stopped!', 'Close');
      }
    });
  }

  settings() {
    this.showSettings = true;
  }

  home() {
    this.showSettings = false;
  }

  startSimulation() {
    if (this.connected && !this.started) {
      this.store.dispatch(new PlasmaAction.StartSimulation());
    }
    else console.log("NO START")
  }

  stopSimulation() {
    if (this.connected && this.started) {
      this.store.dispatch(new PlasmaAction.StopSimulation());
    }
  }

}
