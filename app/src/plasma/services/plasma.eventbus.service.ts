import { Injectable } from '@angular/core';
import * as  EventBus from 'vertx3-eventbus-client';
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

      //this.eventBusService.connect(this.eventBusHost);
    }

  // initializeEventBus() {
  //   if(!this.eventBus) {
  //     this.eventBus = new EventBus(this.eventBusHost);
  //     this.eventBus.enableReconnect(true);
  //     console.log('EventBus initialized');
  //   }
  // }

  // startListening() {
  //   const eb = this.eventBus;
  //   eb.onopen = function () {
  //       eb.registerHandler('PUSH_ALL_ADDRESSES', (error, message) => {
  //           console.log('received a message: ' + JSON.stringify(message));
  //       });
  //   };
  // }
  get connected(): boolean {
    return this.eventBusService.connected;
  }

  startListening() {
    this.eventBusService.registerHandler('PUSH_ALL_ADDRESSES', (error, message) => {
      console.log('received a message: ' + JSON.stringify(message));
    });
  }

  configureSimulation(){
    console.log('Configuring simulation...')
    let config = { instances : 3, numberOfPlasmaClients : 8, amountPerClient: 10 };
    this.eventBusService.send('CONFIGURE_SIMULATION', config, (error, message) => {
      if(message && message.body) {
        console.log(message);
      }
    });
  }

  connect() {
    this.eventBusService.open.subscribe(() => {
      console.log('Connection opened');
      this.store.dispatch(new plasmaAction.ConnectionOpened());
      this.store.dispatch(new plasmaAction.GetConfiguration());
    });

    this.eventBusService.connect(this.eventBusHost);
  }


  publishAction(action: plasmaAction.PlasmaActions) {
    console.log('here1');
    if (!this.connected) {
      return;
    }

    this.eventBusService.publish(action.type, action.payload ? action.payload : 'no-msg');
  }

  sendAction(action: plasmaAction.PlasmaActions): Observable<any> {
    console.log('here2');
    console.log(action.type);
    if (!this.connected) {
      return;
    }
    return Observable.create( observer => {
      this.eventBusService.send(action.type, action.payload ? action.payload : 'no-msg', (error, message) => {
        console.log('here3');
        // TODO: dispatch action
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
    console.log('here4');
    if (!this.connected) {
      return;
    }
    this.eventBusService.registerHandler(actionType, (error, message) => {
      if (error) {
        console.error('[PlasmaEventBusService] handle action of type ${action.type}', error);
      }
      if (!message.body) {
        console.error('[PlasmaEventBusService] - body is empty', error);
      }
      this.store.dispatch({
        type: actionType,
        payload: message.body
      });
    });
  }

  unsubscribeFromAction(actionType: string) {
    console.log('here5');
    if (!this.connected) {
      return;
    }
    this.eventBusService.unregister(actionType);
  }


}
