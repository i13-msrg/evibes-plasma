import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaChain } from './models/plasmachain';
import { Transaction } from './models/transaction';

export interface PlasmaState {
    mainPlasmaChain: PlasmaChain;
    ethBlocks: Array<any>;
    plasmaChildrenChainsMap: Map<string, PlasmaChain>;
    connected: boolean;
    configuration: Configuration;
    simulationStarted: boolean;
    mainPlasmaChainAddress: string;
    ethTransactions: Array<Transaction>;
}
