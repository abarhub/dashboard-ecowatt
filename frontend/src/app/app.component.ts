import { Component } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EMPTY, Observable} from "rxjs";
import {FormControl, FormGroup} from "@angular/forms";
import {empty} from "rxjs/internal/Observer";

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

  constructor(private http: HttpClient) {

  }

  load() {

    console.log('identForm',this.identForm.value);
    const secretKey=this.identForm.value.secretKey;
    console.log('secretKey',secretKey);
    if(secretKey) {
      console.log('appel getOAuth2');
      this.getOAuth2(secretKey).subscribe(x => {
        console.log('oauth2',x);
        const token = x.access_token;
        if (token) {
          console.log('appel ecowatt');
          this.getEcoWatt().subscribe(x=>{
            console.log('ecowat:',x);
          });
        }
      });
    }
  }

  private getOAuth2(secretKey:string):Observable<OAuth2>{
    if(secretKey) {
      const urlOAuth2 = 'https://digital.iservices.rte-france.com/token/oauth/';
      const httpOptions = {
        headers: new HttpHeaders({
          'Content-Type': 'application/x-www-form-urlencoded',
          Authorization: 'Basic ' + secretKey
        })
      };
      return this.http.post<OAuth2>(urlOAuth2, null, httpOptions);
    } else {
      return EMPTY;
    }
  }

  private getEcoWatt(){
    const urlOAuth2='https://digital.iservices.rte-france.com/open_api/ecowatt/v4/sandbox/signals';
    return this.http.get<OAuth2>(urlOAuth2);
  }
}

export class OAuth2{
  access_token:string='';
  token_type:string='';
  expires_in:number=0;
}
