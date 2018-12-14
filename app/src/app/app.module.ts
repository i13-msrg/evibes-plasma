import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { plasmaReducer } from 'src/plasma/plasma.reducer';
import { HomeComponent } from './home/home.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './material-module';
import { SettingsComponent } from './settings/settings.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { PlasmaEffects } from 'src/plasma/plasma.effects';
import { FormsModule } from '@angular/forms';
import { reducers } from './app.reducer';
import { SnackbarComponent } from './snackbar/snackbar.component';



@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SettingsComponent,
    SnackbarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    StoreModule.forRoot(reducers),
    EffectsModule.forRoot([PlasmaEffects]),
    StoreDevtoolsModule.instrument({
      name: 'Plasma Simulator'
    }),
    BrowserAnimationsModule,
    MaterialModule,
    FlexLayoutModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
