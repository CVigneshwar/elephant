import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { EnrollmentService } from '../../../core/services/enrollment.service';

@Component({
  selector: 'app-enroll-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>Enroll in {{ data.courseName }}</h2>

    <mat-dialog-content>
      <div *ngIf="loading" class="loading">Loading available dates...</div>

      <mat-form-field *ngIf="!loading && eligibleDates.length" appearance="outline" class="w-full">
        <mat-label>Select a Date</mat-label>
        <mat-select [(value)]="selectedDate">
          <mat-option *ngFor="let d of eligibleDates" [value]="d">
            {{ d | date:'EEE, MMM d, yyyy' }}
          </mat-option>
        </mat-select>
      </mat-form-field>

      <div *ngIf="!loading && !eligibleDates.length" class="no-dates">
        No available dates — class is at full capacity.
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [disabled]="!selectedDate" (click)="confirmEnroll()">Confirm</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .w-full { width: 100%; }
    .loading, .no-dates { padding: 12px; color: #777; font-style: italic; }
  `]
})
export class EnrollDialogComponent implements OnInit {
  eligibleDates: string[] = [];
  selectedDate = '';
  loading = true;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<EnrollDialogComponent>,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit() {
    this.enrollmentService.getEligibleDates(this.data.sectionId).subscribe({
      next: (dates) => {
        this.eligibleDates = dates;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.eligibleDates = [];
      }
    });
  }

  confirmEnroll() {
    if (!this.selectedDate) return;
    this.dialogRef.close(this.selectedDate);
  }
}
