import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from "rxjs";
import {User} from "../interfaces/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  private apiUrl = 'http://localhost:8081/api/user';
  private userinfoUrl = 'http://localhost:8080/realms/meditrack/protocol/openid-connect/userinfo';
  private realmUrl = "http://localhost:8080/realms/meditrack";


  getAllUsers(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getAllUserFromTeam(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/team`);
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: User): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  deleteUser(user: User): Observable<NonNullable<unknown>> {
    return this.http.delete(`${this.apiUrl}/${user.id}`);
  }

  updateUser(user: User): Observable<User> {
    user.username = undefined;
    return this.http.put<User>(this.apiUrl, user);
  }
}
