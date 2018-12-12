import { Component, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { PlasmaState } from '../../plasma/plasma.state';
import { selectPlasmaConfiguration } from '../../plasma/plasma.selectors';
import { Observable } from 'rxjs';
import { Configuration } from 'src/plasma/models/configuration';
import { AppState } from '../app.state';


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  plasmaConfiguration$: Observable<Configuration>;

  constructor(private store: Store<AppState>) {
    this.plasmaConfiguration$ = this.store.pipe(select(selectPlasmaConfiguration));
   }

  ngOnInit() {
  }
  doSth() {
  }
}
