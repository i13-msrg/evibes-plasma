import { ActionReducer, Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaActions, PlasmaActionTypes } from './plasma.actions';
import { PlasmaState } from './plasma.state';

export const initialState: PlasmaState = {
    blocks: {},
    connected: false,
    configuration: null
};

export function plasmaReducer(state = initialState, action: PlasmaActions) {
    switch (action.type) {
        // case PlasmaActionTypes.GET_PLASMA_BLOCKS: {
        //     const newBlocks: { [number: number]: PlasmaBlock } = action.payload;
        //     return Object.assign({}, state, newBlocks);
        // }
        case PlasmaActionTypes.CONNECTION_OPENED: {
            console.log('received action connection opened');

            return { ...state, connected: true };
        }
        case PlasmaActionTypes.CONNECTION_CLOSED: {
            return { ...state, connected: false };
        }
        case PlasmaActionTypes.SET_CONFIGURATION: {
            console.log('received action set conf');
            console.log(action.payload);
            return { ...state, configuration: action.payload };
        }
    }
}
