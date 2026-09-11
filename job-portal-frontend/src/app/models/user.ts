export type UserRole = 'CANDIDATE' | 'RECRUITER' | 'ADMIN';

export interface User {
  id?: number;
  name?: string;
  email: string;
  role: UserRole;
  password?: string;
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: UserRole;
}