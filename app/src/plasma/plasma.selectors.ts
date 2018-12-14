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

export const selectPlasmaBlocks = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.blocks
);

export const selectPlasmaSimulationStarted = createSelector(
    selectPlasma,
    (state: PlasmaState) => state.simulationStarted
);
