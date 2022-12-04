import { Component } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EMPTY, Observable} from "rxjs";
import {FormControl, FormGroup} from "@angular/forms";
import {Dashboard} from "./dto/dashboard";
import {Statut} from "./dto/statut";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'dashboard-ecowatt';
  identForm = new FormGroup({
    secretKey: new FormControl('')
  });
  dashboard:Dashboard=new Dashboard();

  public readonly Statut : typeof Statut = Statut;

  constructor(private http: HttpClient) {

  }
  load() {
    this.getEcowatt().subscribe(x=>{
      console.log('ecowatt',x);
      this.dashboard=x;
    });
  }

  getEcowatt():Observable<Dashboard> {
    return this.http.get<Dashboard>('api/main');
  }

}

