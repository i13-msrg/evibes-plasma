import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaChain } from './models/plasmachain';
import { Transaction } from './models/transaction';
import { ETHBlock } from './models/ethblock';
import { PropagationInfo } from './models/propagationinfo';

export interface PlasmaState {
    mainPlasmaChain: PlasmaChain;
    ethBlocks: Array<ETHBlock>;
    plasmaChildrenChainsMap: Map<string, PlasmaChain>;
    connected: boolean;
    configuration: Configuration;
    simulationStarted: boolean;
    mainPlasmaChainAddress: string;
    ethTransactions: Array<Transaction>;
    propagationInfo: PropagationInfo;
}
