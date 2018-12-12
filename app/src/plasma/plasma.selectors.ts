import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PlasmaState } from './plasma.state';
import { AppState } from 'src/app/app.state';

export const selectPlasmaState = createFeatureSelector<AppState, PlasmaState> ('plasma');

export const selectPlasma = createSelector(
    selectPlasmaState,
    (state: PlasmaState) => state
);

export const selectPlasmaConfiguration = createSelector(
    selectPlasma,
    (state: PlasmaState) => {
        if (state && state.configuration) {
            return state.configuration;
        }
        return null;
    }
);
