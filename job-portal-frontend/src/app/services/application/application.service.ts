import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application } from '../../models/application';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  
  private readonly apiUrl = '/api/applications';

  constructor(private http: HttpClient) { }

  /**
   * POST: Apply to a job with physical PDF resume
   * /api/applications/apply/{userId}/{jobId}
   */
  applyToJob(userId: number, jobId: number, file: File): Observable<Application> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<Application>(`${this.apiUrl}/apply/${userId}/${jobId}`, formData);
  }

  /**
   * GET: Retrieve applications for a specific candidate
   * /api/applications/candidate/{userId}
   */
  getApplicationsByCandidate(userId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/candidate/${userId}`);
  }

  /**
   * GET: Retrieve applications for a specific job listing
   * /api/applications/job/{jobId}
   */
  getApplicationsByJob(jobId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/job/${jobId}`);
  }

  /**
   * GET: Retrieve all applications across all jobs owned by a recruiter
   * /api/applications/recruiter/{recruiterId}
   */
  getApplicationsByRecruiter(recruiterId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/recruiter/${recruiterId}`);
  }

  /**
   * PUT: Update candidate application status
   * /api/applications/{applicationId}/status?status=ACCEPTED&employerId=1
   */
  updateApplicationStatus(applicationId: number, status: string, employerId: number): Observable<Application> {
    const params = new HttpParams()
      .set('status', status)
      .set('employerId', employerId.toString());

    return this.http.put<Application>(`${this.apiUrl}/${applicationId}/status`, {}, { params });
  }
}