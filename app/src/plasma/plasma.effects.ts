import { Injectable } from '@angular/core';
import { PlasmaEventBusService } from './services/plasma.eventbus.service';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { tap, map, switchMap, catchError } from 'rxjs/operators';
import * as PlasmaAction from './plasma.actions';

@Injectable()
export class PlasmaEffects {

    constructor(private actions$: Actions,
                private plasmaEventBusService: PlasmaEventBusService) { }

    @Effect({dispatch: false}) connect$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.CONNECT),
        tap((action: PlasmaAction.PlasmaActions) => {
            this.plasmaEventBusService.connect();
        })
    );

    @Effect() getConfiguration$: Observable<PlasmaAction.PlasmaActions> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.GET_CONFIGURATION),
        switchMap((action: PlasmaAction.PlasmaActions) => {
            return this.plasmaEventBusService.sendAction(action).pipe(
                map(result => {
                return new PlasmaAction.SetConfiguration(result);
                })
            );
        })
    );

    @Effect() updateConfiguration$: Observable<PlasmaAction.PlasmaActions> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.UPDATE_CONFIGURATION),
        switchMap((action: PlasmaAction.PlasmaActions) => {
            return this.plasmaEventBusService.sendAction(action).pipe(
                map(result => {
                    return new PlasmaAction.SetConfiguration(action.payload);
                })
            );
        })
    );
}
