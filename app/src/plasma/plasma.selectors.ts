import { createFeatureSelector, createSelector, select } from '@ngrx/store';
import { PlasmaState } from './plasma.state';
import { AppState } from 'src/app/app.state';

export const selectPlasmaState = createFeatureSelector<AppState, PlasmaState> ('plasma');

export const selectPlasma = createSelector(
    selectPlasmaState,
    (state: PlasmaState) => state
);

export const selectPlasmaConfiguration = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.configuration
);

export const selectPlasmaConnected = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.connected
);

export const selectMainPlasmaChain = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.mainPlasmaChain
);

export const selectPlasmaSimulationStarted = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.simulationStarted
);

export const selectPlasmaChildrenChains = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.plasmaChildrenChainsMap
);

export const selectPlasmaETHTransactions = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.ethTransactions
);

export const selectEthereumBlocks = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.ethBlocks
);

export const selectEthereumBlocksSize = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.ethBlocks.length
);

export const selectPropagationInfo = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.propagationInfo
);
