import { Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';

export enum PlasmaActionTypes {
    CONNECT                             = 'CONNECT',
    CONNECTION_OPENED                   = 'CONNECTION_OPENED',
    CONNECTION_CLOSED                   = 'CONNECTION_CLOSED',
    START_SIMULATION                    = 'START_SIMULATION',
    STOP_SIMULATION                     = 'STOP_SIMULATION',
    GET_CONFIGURATION                   = 'GET_CONFIGURATION',
    SET_CONFIGURATION                   = 'SET_CONFIGURATION',
    SET_PLASMA_CHAIN_ADDRESSES          = 'SET_PLASMA_CHAIN_ADDRESSES',
    UPDATE_CONFIGURATION                = 'UPDATE_CONFIGURATION',
    ADD_NEW_MAIN_PLASMA_BLOCK           = 'ADD_NEW_MAIN_PLASMA_BLOCK',
    ADD_NEW_CHILD_PLASMA_BLOCK          = 'ADD_NEW_CHILD_PLASMA_BLOCK',
    SIMULATION_STARTED                  = 'SIMULATION_STARTED',
    SIMULATION_STOPPED                  = 'SIMULATION_STOPPED',
    ADD_PLASMA_TRANSACTION              = 'ADD_PLASMA_TRANSACTION',
    ADD_ETH_TRANSACTION                 = 'ADD_ETH_TRANSACTION',
    ADD_ETH_BLOCK                       = 'ADD_ETH_BLOCK',
    SUBSCRIBE_SIMULATOR_TOPICS          = 'SUBSCRIBE_SIMULATOR_TOPICS',
    UNSUBSCRIBE_SIMULATOR_TOPICS        = 'UNSUBSCRIBE_SIMULATOR_TOPICS',
    NUMBER_OF_UTXOS                     = 'NUMBER_OF_UTXOS',
    PARENT_BLOCK_RECEIVED               = 'PARENT_BLOCK_RECEIVED',
    PROPAGATION_INFO                   = 'PROPAGATION_INFO',
    RESET                               = 'RESET'
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

    constructor(public payload: any) {}
}

export class StopSimulation implements Action {
    readonly type: string = PlasmaActionTypes.STOP_SIMULATION;

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

export class SetPlasmaChainAddresses implements Action {
    readonly type: string = PlasmaActionTypes.SET_PLASMA_CHAIN_ADDRESSES;

    constructor(public payload: any) {}
}

export class UpdateConfiguration implements Action {
    readonly type: string = PlasmaActionTypes.UPDATE_CONFIGURATION;

    constructor(public payload: Configuration) {}
}

export class SubscribeToSimulatorTopics implements Action {
    readonly type: string = PlasmaActionTypes.SUBSCRIBE_SIMULATOR_TOPICS;
}

export class UnsubscribeFromSimulatorTopics implements Action {
    readonly type: string = PlasmaActionTypes.UNSUBSCRIBE_SIMULATOR_TOPICS;
}

export class AddNewPlasmaTransaction implements Action {
    readonly type: string = PlasmaActionTypes.ADD_PLASMA_TRANSACTION;

    constructor(public payload: any ) {}
}

export class AddNewETHTransaction implements Action {
    readonly type: string = PlasmaActionTypes.ADD_ETH_TRANSACTION;

    constructor(public payload: any ) {}
}

export class AddNewMainPlasmaBlock implements Action {
    readonly type: string = PlasmaActionTypes.ADD_NEW_MAIN_PLASMA_BLOCK;

    constructor(public payload: any ) {}
}

export class AddNewChildPlasmaBlock implements Action {
    readonly type: string = PlasmaActionTypes.ADD_NEW_CHILD_PLASMA_BLOCK;

    constructor(public payload: any) {}
}

export class SimulationStarted implements Action {
    readonly type: string = PlasmaActionTypes.SIMULATION_STARTED;

    constructor(public payload: any = null) {}
}

export class SimulationStopped implements Action {
    readonly type: string = PlasmaActionTypes.SIMULATION_STOPPED;

    constructor(public payload: any = null) {}
}

export class Reset implements Action {
    readonly type: string = PlasmaActionTypes.RESET;

    constructor(public payload: any = null) {}
}

export class NumberOfUTXOs implements Action {
    readonly type: string = PlasmaActionTypes.NUMBER_OF_UTXOS;

    constructor(public payload: any ) {}
}

export class ParentBlockReceived implements Action {
    readonly type: string = PlasmaActionTypes.PARENT_BLOCK_RECEIVED;

    constructor(public payload: any ) {}
}

export class PropagationInfo implements Action {
    readonly type: string = PlasmaActionTypes.PROPAGATION_INFO;

    constructor(public payload: any ) {}
}

export type PlasmaActions = Connect                     |
                            ConnectionOpened            |
                            ConnectionClosed            |
                            StartSimulation             |
                            StopSimulation              |
                            SetConfiguration            |
                            UpdateConfiguration         |
                            GetConfiguration            |
                            SetPlasmaChainAddresses     |
                            AddNewMainPlasmaBlock       |
                            AddNewChildPlasmaBlock      |
                            NumberOfUTXOs               |
                            ParentBlockReceived         |
                            PropagationInfo            |
                            Reset                       |
                            SimulationStarted;
