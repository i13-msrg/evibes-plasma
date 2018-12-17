import { Component, OnInit } from '@angular/core';
import { Store, select} from '@ngrx/store';
import { AppState } from '../app.state';
import { selectPlasmaConnected,
         selectPlasmaSimulationStarted,
         selectPlasmaConfiguration,
         selectPlasmaChildrenChains,
         selectMainPlasmaChain
        } from 'src/plasma/plasma.selectors';
import * as PlasmaAction from 'src/plasma/plasma.actions';
import { CommonService } from 'src/plasma/services/common.service';
import { Configuration } from 'src/plasma/models/configuration';
import { v4 as uuid } from 'uuid';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  showSettings = false;
  plasmaMainChainBlocks = 0;
  plasmaChildrenChainsBlocksMap = new Map();
  currentConfiguration: Configuration = null;
  connected = false;
  started = false;
  objectKeys = Object.keys;

  constructor(private store: Store<AppState>,
              private commonService: CommonService) {
               }

  ngOnInit() {
    this.store.pipe(select(selectPlasmaConnected)).subscribe(isConnected => {
      this.connected = isConnected;
      if(this.connected) {
        this.commonService.openSnackBar('You are connected to the simulator!', 'Close');
        this.store.dispatch(new PlasmaAction.SubscribeToPlasmaAddresses());
      } else {
        this.commonService.openSnackBar('You are not connected to the simulator!', 'Close');
      }
    });

    this.store.pipe(select(selectMainPlasmaChain)).subscribe(chain => {
      console.log(chain)
      this.plasmaMainChainBlocks = chain.blocks.length;
    });

    this.store.pipe(select(selectPlasmaChildrenChains)).subscribe(chainsMap => {
      this.plasmaChildrenChainsBlocksMap = chainsMap;
    });

    this.store.pipe(select(selectPlasmaConfiguration)).subscribe(configuration => {
      this.currentConfiguration = configuration;
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
      let uuids = new Array();
      for (let i = 0; i < this.currentConfiguration.plasmaChildren; i++) {
        uuids.push(uuid());
      }
      const data = { mainPlasmaChainAddress: uuid(), plasmaChildrenAddresses: uuids};
      
      this.store.dispatch(new PlasmaAction.SetPlasmaChainAddresses(data));
      this.store.dispatch(new PlasmaAction.StartSimulation(data));
    }
    else console.log("NO START")
  }

  stopSimulation() {
    if (this.connected && this.started) {
      this.store.dispatch(new PlasmaAction.StopSimulation());
    }
  }

}
