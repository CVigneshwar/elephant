import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';

Chart.register(...registerables);

@Component({
  selector: 'app-utilization-dashboard',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './utilization-dashboard.component.html',
  styleUrls: ['./utilization-dashboard.component.scss']
})
export class UtilizationDashboardComponent implements OnInit, AfterViewInit {
  utilization: any;
  baseUrl = 'http://localhost:8080/api/utilization';
  private chartsInitialized = false;
  private chartInstances: Chart[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get(this.baseUrl).subscribe({
      next: (data: any) => {
        this.utilization = data;
        if (this.chartsInitialized) this.renderCharts(); // re-render if view ready
      },
      error: (err) => console.error('Failed to load utilization data', err)
    });
  }

  ngAfterViewInit(): void {
    this.chartsInitialized = true;
    if (this.utilization) this.renderCharts();
  }

  // ----------- Chart management helpers -----------

  destroyCharts(): void {
    this.chartInstances.forEach((c) => c.destroy());
    this.chartInstances = [];
  }

  renderCharts(): void {
    this.destroyCharts();
    setTimeout(() => {
      const charts = [
        this.renderTeacherChart(),
        this.renderRoomChart(),
        this.renderDayChart(),
        this.renderTimeSlotChart()
      ].filter((c): c is Chart => !!c); // ✅ keep only valid Chart objects
  
      this.chartInstances.push(...charts);
    }, 100);
  }
  

  // ----------- Individual Charts -----------

  renderTeacherChart(): Chart | void {
    const canvas = document.getElementById('teacherChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.teacherUtilization.map((t: any) => t.name),
        datasets: [{
          label: 'Teacher Utilization (%)',
          data: this.utilization.teacherUtilization.map((t: any) => t.utilizationPercent),
          backgroundColor: '#4C9AFF'
        }]
      },
      options: {
        plugins: { title: { display: true, text: 'Teacher Utilization (%)' } },
        scales: { y: { beginAtZero: true, max: 100, title: { display: true, text: '%' } } },
        maintainAspectRatio: false
      }
    });
  }

  renderRoomChart(): Chart | void {
    const canvas = document.getElementById('roomChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.roomUtilization.map((r: any) => r.name),
        datasets: [{
          label: 'Room Utilization (%)',
          data: this.utilization.roomUtilization.map((r: any) => r.utilizationPercent),
          backgroundColor: '#FFD56B'
        }]
      },
      options: {
        plugins: { title: { display: true, text: 'Room Utilization (%)' } },
        scales: { y: { beginAtZero: true, max: 100, title: { display: true, text: '%' } } },
        maintainAspectRatio: false
      }
    });
  }

  renderDayChart(): Chart | void {
    const canvas = document.getElementById('dayChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization) return;

    return new Chart(canvas, {
      type: 'pie',
      data: {
        labels: this.utilization.dayUtilization.map((d: any) => d.dayOfWeek),
        datasets: [{
          label: 'Day Utilization (%)',
          data: this.utilization.dayUtilization.map((d: any) => d.utilizationPercent),
          backgroundColor: ['#B5E48C', '#99D98C', '#76C893', '#52B69A', '#34A0A4']
        }]
      },
      options: {
        plugins: { title: { display: true, text: 'Day Utilization (%)' }, legend: { position: 'bottom' } },
        maintainAspectRatio: false
      }
    });
  }

  // ✅ UPDATED — show hours instead of percentage
  renderTimeSlotChart(): Chart | void {
    const canvas = document.getElementById('timeChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.timeSlotUtilization.map((t: any) => t.slot),
        datasets: [{
          label: 'Time Slot Utilized Hours',
          data: this.utilization.timeSlotUtilization.map((t: any) => t.totalHoursScheduled),
          backgroundColor: '#6EA8FE'
        }]
      },
      options: {
        plugins: {
          title: { display: true, text: 'Time Slot Utilized Hours per Week' },
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            title: { display: true, text: 'Hours' }
          },
          x: {
            title: { display: true, text: 'Time Slots' }
          }
        },
        maintainAspectRatio: false
      }
    });
  }
}
