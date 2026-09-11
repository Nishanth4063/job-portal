import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { ApplicationService } from '../../services/application/application.service';
import { Application } from '../../models/application';

@Component({
  selector: 'app-candidate-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './candidate-dashboard.html',
  styleUrl: './candidate-dashboard.scss',
})
export class CandidateDashboardComponent implements OnInit {

  myApplications: Application[] = [];
  totalAppliedCount: number = 0;
  totalResponsesCount: number = 0;
  currentCandidateId!: number;

  constructor(
    private applicationService: ApplicationService, 
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const savedId = sessionStorage.getItem('userId');
    const savedRole = sessionStorage.getItem('role');
    const routeParamId = this.route.snapshot.paramMap.get('id');

    if (!savedId || savedRole !== 'CANDIDATE' || savedId !== routeParamId) {
      alert('Access Denied: Security validation mismatch. Ejecting session context.');
      sessionStorage.clear();
      this.router.navigate(['/login']);
      return;
    }

    this.currentCandidateId = parseInt(savedId, 10);
    this.loadCandidateApplicationHistory();
  }

  loadCandidateApplicationHistory(): void {
    this.applicationService.getApplicationsByCandidate(this.currentCandidateId).subscribe({
      next: (data: Application[]) => {
        this.myApplications = data;
        this.totalAppliedCount = data.length;
        this.totalResponsesCount = data.filter(
          app => app.status === 'ACCEPTED' || app.status === 'REJECTED'
        ).length;
      },
      error: (err) => {
        console.error('Failed to load candidate applications:', err);
      }
    });
  }
}