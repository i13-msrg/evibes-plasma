import { ActionReducer, Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaActions, PlasmaActionTypes } from './plasma.actions';
import { PlasmaState } from './plasma.state';

export const initialState: PlasmaState = {
    blocks: [],
    connected: false,
    configuration: null,
    simulationStarted: false
};

export function plasmaReducer(state = initialState, action: PlasmaActions) {
    switch (action.type) {
        case PlasmaActionTypes.CONNECTION_OPENED: {
            return { ...state, connected: true };
        }
        case PlasmaActionTypes.CONNECTION_CLOSED: {
            return { ...state, connected: false };
        }
        case PlasmaActionTypes.SET_CONFIGURATION: {
            return { ...state, configuration: action.payload };
        }
        case PlasmaActionTypes.ADD_NEW_BLOCK: {
            return { ...state, blocks: [...state.blocks, action.payload]};
        }
        case PlasmaActionTypes.SIMULATION_STARTED: {
            return { ...state, simulationStarted: true};
        }
        case PlasmaActionTypes.SIMULATION_STOPPED: {
            return { ...state, simulationStarted: false};
        }

        default:
            return state;
    }
}
