import { ActionReducer, Action } from '@ngrx/store';
import { PlasmaBlock } from './models/plasmablock';
import { Configuration } from './models/configuration';
import { PlasmaActions, PlasmaActionTypes, NumberOfUTXOs } from './plasma.actions';
import { PlasmaState } from './plasma.state';
import { PlasmaChain } from './models/plasmachain';
import { Transaction } from './models/transaction';
import { ETHBlock } from './models/ethblock';

export const initialPlasmaChain: PlasmaChain = {
    address: '',
    blocks: new Array(),
    depositBlocks: new Array(),
    allTransactions: 0,
    childrenRootTransactions: 0,
    childrenTotalTransactions: 0,
    parentBlocks: 0,
    numberOfUTXOs: 0
};

export const initialState: PlasmaState = {
    mainPlasmaChain: initialPlasmaChain,
    ethBlocks: new Array<ETHBlock>(),
    plasmaChildrenChainsMap: new Map(),
    // childrenBlocks: new Map(),
    connected: false,
    configuration: null,
    simulationStarted: false,
    mainPlasmaChainAddress: null,
    ethTransactions: new Array<Transaction>(),
    propagationInfo: null
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
            const block = action.payload;
            plasmaChain.blocks = [... plasmaChain.blocks, block];
            if (block.depositBlock) {
                plasmaChain.depositBlocks = [... plasmaChain.depositBlocks, block];
            }
            block.transactions.forEach((transaction) => {
                if (transaction.childChainTransaction) {
                    plasmaChain.childrenTotalTransactions ++;
                }
            });

            const updatedTotalTransactions = plasmaChain.allTransactions + block.transactions.length;
            plasmaChain.allTransactions = updatedTotalTransactions;
            return { ...state, mainPlasmaChain: {... plasmaChain}};
        }
        case PlasmaActionTypes.ADD_NEW_CHILD_PLASMA_BLOCK: {
            const childrenPlasmaChainMap1 = { ...state.plasmaChildrenChainsMap };
            const chainAddress = action.payload['chainAddress'];
            const chainBlock = action.payload['childBlock'];
            const child = {... childrenPlasmaChainMap1[chainAddress]};
            child.blocks = [... child.blocks, chainBlock];
            if (chainBlock.depositBlock) {
                child.depositBlocks = [... child.depositBlocks, chainBlock];
            }
            const updatedTotalTransactions = child.allTransactions + chainBlock.transactions.length;
            child.allTransactions = updatedTotalTransactions;
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
        case PlasmaActionTypes.ADD_ETH_BLOCK: {
            return { ...state, ethBlocks: [ ...state.ethBlocks, action.payload ]};
        }
        case PlasmaActionTypes.NUMBER_OF_UTXOS: {
            const address = action.payload['chainAddress'];
            const utxosNumber = action.payload['numberOfUTXOs'];
            if (address === state.mainPlasmaChain.address) {
                const plasmaChain = { ...state.mainPlasmaChain };
                plasmaChain.numberOfUTXOs = utxosNumber;
                return { ... state, mainPlasmaChain: {... plasmaChain}};
            } else {
                const childrenPlasmaChainMap1 = { ...state.plasmaChildrenChainsMap };
                const child = {... childrenPlasmaChainMap1[address]};
                child.numberOfUTXOs = utxosNumber;
                childrenPlasmaChainMap1[address] = { ... child };
                return  { ...state, plasmaChildrenChainsMap : { ... childrenPlasmaChainMap1} };

            }
            return state;
        }
        case PlasmaActionTypes.PARENT_BLOCK_RECEIVED: {
            const address = action.payload['chainAddress'];
            const childrenPlasmaChainMap1 = { ...state.plasmaChildrenChainsMap };
            const child = {... childrenPlasmaChainMap1[address]};
            child.parentBlocks ++;
            childrenPlasmaChainMap1[address] = { ... child };
            return  { ...state, plasmaChildrenChainsMap : { ... childrenPlasmaChainMap1} };
        }
        case PlasmaActionTypes.PROPAGATION_INFO: {
            console.log(action.payload)
            return { ... state, propagationInfo: action.payload };
        }
        case PlasmaActionTypes.RESET: {
            const resetState = { ...initialState};
            const currentState = { ...state };
            resetState.connected = currentState.connected;
            resetState.simulationStarted = currentState.simulationStarted;
            resetState.configuration = currentState.configuration;

            return { ... resetState };
        }

        default:
            return state;
    }
}
