import { Injectable } from '@angular/core';
import { PlasmaEventBusService } from './services/plasma.eventbus.service';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { tap, map, switchMap, catchError } from 'rxjs/operators';
import * as PlasmaAction from './plasma.actions';
import { CommonService } from './services/common.service';

@Injectable()
export class PlasmaEffects {

    constructor(private actions$: Actions,
                private plasmaEventBusService: PlasmaEventBusService,
                private commonService: CommonService) { }

    @Effect({dispatch: false}) connect$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.CONNECT),
        tap((action: PlasmaAction.PlasmaActions) => {
            this.plasmaEventBusService.connect();
        })
    );

    @Effect() getConfiguration$ = this.actions$.pipe(
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
                    this.commonService.openSnackBar('Configuration successfully saved!', 'Close');
                    return new PlasmaAction.SetConfiguration(action.payload);
                }),
                catchError(error => {
                    this.commonService.openSnackBar('Configuration could not be saved. Check your connection', 'Close');
                    return of({type: 'ERROR', payload: null});
                })
            );
        })
    );

    @Effect() startSimulation$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.START_SIMULATION),
        switchMap((action: PlasmaAction.PlasmaActions) => {
            return this.plasmaEventBusService.sendAction(action).pipe(
                switchMap(result => {
                    console.log('simulation started: ' + result);
                    return [new PlasmaAction.SubscribeToNewBlock(), new PlasmaAction.SimulationStarted(null)];
                })
            );
        })
    );

    @Effect() stopSimulation$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.STOP_SIMULATION),
        switchMap((action: PlasmaAction.PlasmaActions) => {
            return this.plasmaEventBusService.sendAction(action).pipe(
                switchMap(result => {
                    console.log('simulation stopped: ' + result);
                    return [new PlasmaAction.UnsubscribeToNewBlock(), new PlasmaAction.SimulationStopped(null)];
                })
            );
        })
    );

    @Effect({dispatch: false}) subscribeToNewBlock$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.SUBSCRIBE_NEW_BLOCK),
        tap((action) => {
            this.plasmaEventBusService.subscribeToAction(PlasmaAction.PlasmaActionTypes.ADD_NEW_BLOCK);
        })
    );

    @Effect({dispatch: false}) unsubscribeToNewBlock$: Observable<Action> = this.actions$.pipe(
        ofType(PlasmaAction.PlasmaActionTypes.UNSUBSCRIBE_NEW_BLOCK),
        tap((action) => {
            this.plasmaEventBusService.unsubscribeFromAction(PlasmaAction.PlasmaActionTypes.ADD_NEW_BLOCK);
        })
    );
}
