import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';

export interface PlasmaState {
    blocks: {[number: number]: PlasmaBlock};
    connected: boolean;
    configuration: Configuration;
}