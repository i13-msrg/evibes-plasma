import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaChain } from './models/plasmachain';

export interface PlasmaState {
    mainPlasmaChain: PlasmaChain;
    plasmaChildrenChainsMap: Map<string, PlasmaChain>;
    connected: boolean;
    configuration: Configuration;
    simulationStarted: boolean;
    mainPlasmaChainAddress: string;
}
