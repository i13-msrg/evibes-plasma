import { ActionReducerMap } from '@ngrx/store';
import { AppState } from './app.state';
import { plasmaReducer } from '../plasma/plasma.reducer';


export const reducers: ActionReducerMap<AppState> = {
    plasma: plasmaReducer
};
