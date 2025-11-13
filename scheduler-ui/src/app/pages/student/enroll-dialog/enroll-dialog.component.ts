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
  templateUrl: './enroll-dialog.component.html',
  styleUrls: ['./enroll-dialog.component.scss']
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