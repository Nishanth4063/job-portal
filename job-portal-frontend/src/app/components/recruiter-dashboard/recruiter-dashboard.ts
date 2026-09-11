import { Component, OnInit, OnDestroy } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router, NavigationEnd } from '@angular/router'; 
import { Subscription, interval } from 'rxjs'; 
import { filter } from 'rxjs/operators';
import { JobService, JobResponseDTO } from '../../services/job/job.service';
import { ApplicationService } from '../../services/application/application.service';
import { Application } from '../../models/application';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './recruiter-dashboard.html',
  styleUrl: './recruiter-dashboard.scss'
})
export class RecruiterDashboardComponent implements OnInit, OnDestroy { 
  
  newJob: Partial<JobResponseDTO> = {
    title: '',
    location: '',
    description: ''
  };

  incomingApplications: Application[] = [];
  myPostedJobs: JobResponseDTO[] = [];
  currentRecruiterId!: number;

  private routerSubscription!: Subscription;
  private pollingSubscription!: Subscription;

  constructor(
    private jobService: JobService,
    private applicationService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const savedId = sessionStorage.getItem('userId');
    const savedRole = sessionStorage.getItem('role');

    if (!savedId || savedRole !== 'RECRUITER') {
      alert('Access Denied: This dashboard is reserved strictly for authenticated Recruiter profiles.');
      this.router.navigate(['/login']);
      return;
    }

    this.currentRecruiterId = parseInt(savedId, 10);
    this.loadAllDashboardData();

    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.loadAllDashboardData();
    });

    this.pollingSubscription = interval(5000).subscribe(() => {
      this.loadIncomingApplications();
    });
  }

  private loadAllDashboardData(): void {
    this.loadIncomingApplications();
    this.loadMyPostedJobs();
  }

  loadIncomingApplications(): void {
    this.applicationService.getApplicationsByRecruiter(this.currentRecruiterId).subscribe({
      next: (data: Application[]) => {
        this.incomingApplications = data;
      },
      error: (err) => {
        console.error('Failed to load incoming applications:', err);
      }
    });
  }

  loadMyPostedJobs(): void {
    this.jobService.getJobsByRecruiter(this.currentRecruiterId).subscribe({
      next: (data: JobResponseDTO[]) => {
        this.myPostedJobs = data;
      },
      error: (err) => {
        console.error('Failed to load recruiter job vacancies:', err);
      }
    });
  }

  onCreateJob(): void {
    if (!this.newJob.title || !this.newJob.location || !this.newJob.description) {
      alert('Please fill out all job posting fields.');
      return;
    }

    this.jobService.postJob(this.newJob, this.currentRecruiterId).subscribe({
      next: (createdJob) => {
        alert(`Job "${createdJob.title}" published successfully!`);
        this.newJob = { title: '', location: '', description: '' };
        this.loadMyPostedJobs(); 
      },
      error: (err) => {
        console.error('Failed to publish vacancy listing:', err);
        alert('Failed to post job. Check backend server console.');
      }
    });
  }

  onUpdateStatus(applicationId: number, status: 'ACCEPTED' | 'REJECTED'): void {
    this.applicationService.updateApplicationStatus(applicationId, status, this.currentRecruiterId).subscribe({
      next: () => {
        alert(`Application status successfully updated to: ${status}`);
        this.loadIncomingApplications();
      },
      error: (err) => {
        console.error('Status modification process failed:', err);
        alert('Failed to modify application status.');
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }
}