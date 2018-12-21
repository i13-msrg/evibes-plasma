import { ActionReducer, Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaActions, PlasmaActionTypes } from './plasma.actions';
import { PlasmaState } from './plasma.state';
import { PlasmaChain } from './models/plasmachain';
import { Transaction } from './models/transaction';

export const initialPlasmaChain: PlasmaChain = {
    address: '',
    blocks: new Array(),
    depositBlocks: new Array(),
    allTransactions: 0,
    childrenRootTransactions: 0
};

export const initialState: PlasmaState = {
    mainPlasmaChain: initialPlasmaChain,
    plasmaChildrenChainsMap: new Map(),
    // childrenBlocks: new Map(),
    connected: false,
    configuration: null,
    simulationStarted: false,
    mainPlasmaChainAddress: null,
    ethTransactions: new Array<Transaction>()
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
        case PlasmaActionTypes.SET_PLASMA_CHAIN_ADDRESSES: {
            const mainPlasmaChainAddress = action.payload['mainPlasmaChainAddress'];
            const mainPlasmaChain = { ...state.mainPlasmaChain, address: mainPlasmaChainAddress };

            const childrenPlasmaChainMap1 = { ...state.plasmaChildrenChainsMap };

            if (action.payload['plasmaChildrenAddresses']) {
                action.payload['plasmaChildrenAddresses'].forEach((childAddress: string) => {
                    const childChain = { ...initialPlasmaChain, address: childAddress };
                    childrenPlasmaChainMap1[childAddress] = childChain;
                    console.log('adding address: ' + childAddress);
                });
            } else {
                console.log('no children');
            }
            return {... state, mainPlasmaChain: mainPlasmaChain,
                               plasmaChildrenChainsMap: childrenPlasmaChainMap1};
        }
        case PlasmaActionTypes.ADD_NEW_MAIN_PLASMA_BLOCK: {
            const plasmaChain = { ...state.mainPlasmaChain };
            plasmaChain.blocks = [... plasmaChain.blocks, action.payload];
            return { ...state, mainPlasmaChain: {... plasmaChain}};
        }
        case PlasmaActionTypes.ADD_NEW_CHILD_PLASMA_BLOCK: {
            const childrenPlasmaChainMap1 = { ...state.plasmaChildrenChainsMap };
            const chainAddress = action.payload['chainAddress'];
            const chainBlock = action.payload['childBlock'];
            const child = {... childrenPlasmaChainMap1[chainAddress]};
            child.blocks = [... child.blocks, chainBlock];
            childrenPlasmaChainMap1[chainAddress] = { ... child };
            // const childrenBlocks1 = { ...state.childrenBlocks};
            // childrenBlocks1[chainAddress] = [...childrenBlocks1[chainAddress], chainBlock]

            return { ...state, plasmaChildrenChainsMap : { ... childrenPlasmaChainMap1} };
        }
        case PlasmaActionTypes.SIMULATION_STARTED: {
            return { ...state, simulationStarted: true};
        }
        case PlasmaActionTypes.SIMULATION_STOPPED: {
            return { ...state, simulationStarted: false};
        }
        case PlasmaActionTypes.ADD_ETH_TRANSACTION: {
            return { ...state, ethTransactions: [ ...state.ethTransactions, action.payload ]};
        }
        case PlasmaActionTypes.RESET: {
            return { ... initialState };
        }

        default:
            return state;
    }
}
