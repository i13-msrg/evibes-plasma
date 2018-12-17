import { Injectable } from '@angular/core';
import { of, Observable } from 'rxjs';
import { EventBusService } from './eventbus/eventbus.service';
import * as plasmaAction from '../plasma.actions';
import {Store} from '@ngrx/store';
import { AppState } from 'src/app/app.state';


@Injectable({
  providedIn: 'root',
})
export class PlasmaEventBusService {
  eventBusHost = 'http://localhost:8080/eventbus/';

  constructor(private eventBusService: EventBusService,
              private store: Store<AppState>) {
    this.eventBusService.enableReconnect(true);
  }

  get connected(): boolean {
    return this.eventBusService.connected;
  }

  connect() {
    this.eventBusService.open.subscribe(() => {
      this.store.dispatch(new plasmaAction.ConnectionOpened());
      this.store.dispatch(new plasmaAction.GetConfiguration());
    });

    this.eventBusService.close.subscribe(() => {
      this.store.dispatch(new plasmaAction.ConnectionClosed());
    });

    this.eventBusService.connect(this.eventBusHost);
  }

  disconnect() {
    this.eventBusService.disconnect();
  }


  publishAction(action: plasmaAction.PlasmaActions) {
    if (!this.connected) {
      return;
    }

    this.eventBusService.publish(action.type, action.payload ? action.payload : 'no-msg');
  }

  sendAction(action: plasmaAction.PlasmaActions): Observable<any> {
    console.log(action.type);
    if (!this.connected) {
      console.log('not connected');
      return;
    }
    return Observable.create( observer => {
      console.log(action.payload);
      this.eventBusService.send(action.type, action.payload ? action.payload : 'no-msg', (error, message) => {
        if (error) {
          console.log('Error: ${error}');
          return observer.error(error);
        }
        if (message && message.body) {
          console.log('Message: ' + JSON.stringify(message));
          observer.next(message.body);
          observer.complete();
        } else {
          observer.next('message body is empty');
        observer.complete();
        }
      });
    });
  }

  subscribeToAction(actionType: string) {
    console.log('Subscribing to ' + actionType);
    if (!this.connected) {
      console.log('not connected');
      return;
    }
    this.eventBusService.registerHandler(actionType, (error, message) => {
      if (error) {
        console.error('[PlasmaEventBusService] handle action of type ${action.type}', error);
      }
      if (!message.body) {
        console.error('[PlasmaEventBusService] - body is empty', error);
      }

      console.log(actionType);
      console.log(message.body);

      this.store.dispatch({
        type: actionType,
        payload: message.body
      });
    });
  }

  unsubscribeFromAction(actionType: string) {
    if (!this.connected) {
      return;
    }
    this.eventBusService.unregister(actionType);
  }


}
