import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableDataSource } from '@angular/material/table';
import { ScheduleService } from '../../../core/services/schedule.service';

@Component({
  selector: 'app-weekly-timetable',
  standalone: true,
  imports: [
    CommonModule,            
    MatTableModule,         
    MatSortModule,           
    MatFormFieldModule,      
    MatInputModule           
  ],
  templateUrl: './weekly-timetable.component.html',
  styleUrls: ['./weekly-timetable.component.scss']
})
export class WeeklyTimetableComponent implements OnInit {
  displayedColumns = [
    'dayOfWeek',
    'startTime',
    'endTime',
    'courseCode',
    'courseName',
    'teacherName',
    'roomName',
    'duration'
  ];

  dataSource = new MatTableDataSource<any>([]);
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private scheduleService: ScheduleService) {}

  ngOnInit(): void {
    this.scheduleService.list().subscribe((data) => {
      this.dataSource.data = this.sortTimetable(data);
      this.dataSource.sort = this.sort;

    // ✅ Custom filter for all fields
    this.dataSource.filterPredicate = (row: any, filter: string): boolean => {
      const term = filter.toLowerCase();

    

      return (
        row.courseCode?.toLowerCase().includes(term) ||
        row.courseName?.toLowerCase().includes(term) ||
        row.teacherName?.toLowerCase().includes(term) ||
        row.roomName?.toLowerCase().includes(term) ||
        row.dayOfWeek?.toLowerCase().includes(term) ||
        row.startTime?.toLowerCase().includes(term) ||
        row.endTime?.toLowerCase().includes(term)
      );
    };
    });

  }

  calculateDuration(start: string, end: string): number {
    const [sh, sm] = start.split(':').map(Number);
    const [eh, em] = end.split(':').map(Number);
    return (eh * 60 + em - (sh * 60 + sm)) / 60;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = filterValue;
  }

  
  // ✅ Custom sorting: Day → Time → Duration
  sortTimetable(data: any[]): any[] {
    const dayOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
  
    return data.sort((a, b) => {
      const dayCompare = dayOrder.indexOf(a.dayOfWeek) - dayOrder.indexOf(b.dayOfWeek);
      if (dayCompare !== 0) return dayCompare;
  
      const timeCompare = (a.startTime || '').localeCompare(b.startTime || '');
      if (timeCompare !== 0) return timeCompare;
  
      const durationA = this.calculateDuration(a.startTime, a.endTime);
      const durationB = this.calculateDuration(b.startTime, b.endTime);
      return durationA - durationB;
    });
  }
}
