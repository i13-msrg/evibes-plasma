import { Component, OnInit } from '@angular/core';
import { Store, select} from '@ngrx/store';
import { AppState } from '../app.state';
import { selectPlasmaConnected,
         selectPlasmaSimulationStarted,
         selectPlasmaConfiguration,
         selectPlasmaChildrenChains,
         selectMainPlasmaChain,
         selectPlasmaETHTransactions,
         selectEthereumBlocksSize,
         selectEthereumBlocks,
         selectPropagationInfo
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
  plasmaMainChainTransactions = 0;
  plasmaChildrenChainsBlocksMap = new Map();
  currentConfiguration: Configuration = null;
  connected = false;
  started = false;
  objectKeys = Object.keys;
  ethBlockTransactions = new Array();
  ethDepositTransactions = new Array();
  ethBlocksSize = 0;
  lastETHBlockMinedBy = '';
  totalNumberOfETHTransactions = 0;

  firstPlasmaChain = null;
  propagationInfo = null;

  color = 'primary';
  mode = 'determinate';
  value = 0;
  bufferValue = 0;

  constructor(private store: Store<AppState>,
              private commonService: CommonService) {
               }

  ngOnInit() {
    this.store.pipe(select(selectPlasmaConnected)).subscribe(isConnected => {
      this.connected = isConnected;
      if(this.connected) {
        this.commonService.openSnackBar('You are connected to the simulator!', 'Close');
      } else {
        this.commonService.openSnackBar('You are not connected to the simulator!', 'Close');
      }
    });

    this.store.pipe(select(selectMainPlasmaChain)).subscribe(chain => {
      // console.log(chain)
      this.plasmaMainChainBlocks = chain.blocks.length;
      this.firstPlasmaChain = chain;
      this.plasmaMainChainTransactions = chain.allTransactions;
    });

    this.store.pipe(select(selectPlasmaChildrenChains)).subscribe(chainsMap => {
      this.plasmaChildrenChainsBlocksMap = chainsMap;
    });

    this.store.pipe(select(selectPlasmaConfiguration)).subscribe(configuration => {
      this.currentConfiguration = configuration;
    });

    this.store.pipe(select(selectEthereumBlocks)).subscribe(blocks => {
      console.log('eth block');
      console.log(blocks[blocks.length - 1])
      this.lastETHBlockMinedBy = blocks[blocks.length - 1].extraData;
    });

    this.store.pipe(select(selectPlasmaETHTransactions)).subscribe(txs => {
      this.ethDepositTransactions = txs.filter(tx => tx.data.method === 'deposit');
      this.ethBlockTransactions = txs.filter(tx => tx.data.method === 'submitBlock');
      this.totalNumberOfETHTransactions = txs.length;
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

    this.store.pipe(select(selectEthereumBlocksSize)).subscribe(blocksSize => {
      this.ethBlocksSize = blocksSize;
    });

    this.store.pipe(select(selectPropagationInfo)).subscribe(propInfo => {
      this.propagationInfo = propInfo;
    });

  }

  settings() {
    this.showSettings = true;
  }

  home() {
    this.showSettings = false;
  }

  reset() {
    this.store.dispatch(new PlasmaAction.Reset());
  }

  startSimulation() {
    if (this.connected && !this.started) {
      let uuids = new Array();
      for (let i = 0; i < this.currentConfiguration.plasmaChildren; i++) {
        uuids.push(uuid());
      }
      const data = { mainPlasmaChainAddress: uuid(), plasmaChildrenAddresses: uuids};

      this.store.dispatch(new PlasmaAction.SetPlasmaChainAddresses(data));
      this.store.dispatch(new PlasmaAction.SubscribeToSimulatorTopics());
      this.store.dispatch(new PlasmaAction.StartSimulation(data));
      this.mode = 'indeterminate';
    }
    else console.log("NO START")
  }

  stopSimulation() {
    if (this.connected && this.started) {
      this.store.dispatch(new PlasmaAction.UnsubscribeFromSimulatorTopics());
      this.store.dispatch(new PlasmaAction.StopSimulation());
      this.mode = 'determinate';
    }
  }
}
