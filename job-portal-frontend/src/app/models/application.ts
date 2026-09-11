export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  resumeUrl: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  appliedDate: string;
}