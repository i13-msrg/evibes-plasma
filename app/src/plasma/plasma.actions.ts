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
    SUBSCRIBE_NEW_BLOCK               = 'SUBSCRIBE_NEW_BLOCK',
    UNSUBSCRIBE_NEW_BLOCK             = 'UNSUBSCRIBE_NEW_BLOCK',
    ADD_NEW_BLOCK                     = 'ADD_NEW_BLOCK',
    SIMULATION_STARTED                = 'SIMULATION_STARTED',
    SIMULATION_STOPPED                = 'SIMULATION_STOPPED'
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

export class SubscribeToNewBlock implements Action {
    readonly type: string = PlasmaActionTypes.SUBSCRIBE_NEW_BLOCK;
}

export class UnsubscribeToNewBlock implements Action {
    readonly type: string = PlasmaActionTypes.SUBSCRIBE_NEW_BLOCK;
}

export class AddNewBlock implements Action {
    readonly type: string = PlasmaActionTypes.ADD_NEW_BLOCK;

    constructor(public payload: null) {}
}

export class SimulationStarted implements Action {
    readonly type: string = PlasmaActionTypes.SIMULATION_STARTED;

    constructor(public payload: null) {}
}

export class SimulationStopped implements Action {
    readonly type: string = PlasmaActionTypes.SIMULATION_STOPPED;

    constructor(public payload: null) {}
}

export type PlasmaActions = Connect             |
                            ConnectionOpened    |
                            ConnectionClosed    |
                            StartSimulation     |
                            StopSimulation      |
                            SetConfiguration    |
                            UpdateConfiguration |
                            GetConfiguration    |
                            AddNewBlock         |
                            SimulationStarted   |
                            GetPlasmaBlocks;
