import { Component, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { selectPlasmaConfiguration } from '../../plasma/plasma.selectors';
import { Observable } from 'rxjs';
import { Configuration } from 'src/plasma/models/configuration';
import { AppState } from '../app.state';
import * as PlasmaAction from '../../plasma/plasma.actions';
import { CommonService } from 'src/plasma/services/common.service';


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  plasmaConfiguration$: Observable<Configuration>;
  currentConfiguration: Configuration = null;

  constructor(private store: Store<AppState>,
              private commonService: CommonService) {
    this.store.dispatch(new PlasmaAction.GetConfiguration());
   }

  ngOnInit() {
    this.store.pipe(select(selectPlasmaConfiguration)).subscribe(configuration => {
      if (configuration) {
        console.log('New configuration arrived');
        this.currentConfiguration = configuration;
      }
    });
  }

  save() {
    this.store.dispatch(new PlasmaAction.UpdateConfiguration(this.currentConfiguration));
  }

}
