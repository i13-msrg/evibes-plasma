import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';

export interface PlasmaState {
    blocks: Array<PlasmaBlock>;
    connected: boolean;
    configuration: Configuration;
    simulationStarted: boolean;
}
