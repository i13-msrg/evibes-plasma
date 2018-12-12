import { Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';

export enum PlasmaActionTypes {
    CONNECT                           = 'CONNECT',
    CONNECTION_OPENED                 = 'CONNECTION_OPENED',
    CONNECTION_CLOSED                 = 'CONNECTION_CLOSED',
    START_SIMULATION                  = 'START_SIMULATION',
    STOP_SIMULATION                   = 'STOP_SIMULATION',
    GET_PLASMA_BLOCKS                 = 'GET_PLASMA_BLOCKS',
    GET_CONFIGURATION                 = 'GET_CONFIGURATION',
    SET_CONFIGURATION                 = 'SET_CONFIGURATION',
    UPDATE_CONFIGURATION              = 'UPDATE_CONFIGURATION',
    UPDATE_NUMBER_OF_PLASMA_BLOCKS    = 'UPDATE_NUMBER_OF_PLASMA_BLOCKS',
}

export class Connect implements Action {
    readonly type: string = PlasmaActionTypes.CONNECT;

    constructor(public payload: any = null) {}
}

export class ConnectionOpened implements Action {
    readonly type: string = PlasmaActionTypes.CONNECTION_OPENED;

    constructor(public payload: any = null) {}
}

export class ConnectionClosed implements Action {
    readonly type: string = PlasmaActionTypes.CONNECTION_CLOSED;

    constructor(public payload: any = null) {}
}

export class StartSimulation implements Action {
    readonly type: string = PlasmaActionTypes.START_SIMULATION;

    constructor(public payload: any = null) {}
}

export class StopSimulation implements Action {
    readonly type: string = PlasmaActionTypes.STOP_SIMULATION;

    constructor(public payload: any = null) {}
}

export class GetPlasmaBlocks implements Action {
    readonly type: string = PlasmaActionTypes.GET_PLASMA_BLOCKS;

    constructor(public payload: any = null) {}
}

export class GetConfiguration implements Action {
    readonly type: string = PlasmaActionTypes.GET_CONFIGURATION;

    constructor(public payload: any = null) {}
}

export class SetConfiguration implements Action {
    readonly type: string = PlasmaActionTypes.SET_CONFIGURATION;

    constructor(public payload: Configuration) {}
}

export class UpdateConfiguration implements Action {
    readonly type: string = PlasmaActionTypes.UPDATE_CONFIGURATION;

    constructor(public payload: Configuration) {}
}


// export class ReceivedPlasmaBlocks implements Action {
//     readonly type: string = PlasmaActionTypes.GET_PLASMA_BLOCKS_RECEIVED;

//     constructor(public payload: { [number: number]: PlasmaBlock }) {}
// }

export type PlasmaActions = GetConfiguration | GetPlasmaBlocks;
