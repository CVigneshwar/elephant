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
        if (this.chartsInitialized) this.renderCharts();
      },
      error: (err) => console.error('Failed to load utilization data', err)
    });
  }

  ngAfterViewInit(): void {
    this.chartsInitialized = true;
    if (this.utilization) this.renderCharts();
  }

  destroyCharts(): void {
    this.chartInstances.forEach(chart => chart.destroy());
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
      ].filter((c): c is Chart => !!c);

      this.chartInstances.push(...charts);
    }, 150);
  }

  // ----------------- TEACHER UTILIZATION -----------------
  renderTeacherChart(): Chart | void {
    const canvas = document.getElementById('teacherChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization?.teacherUsage) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.teacherUsage.map((t: any) => t.name),
        datasets: [{
          label: 'Teacher Utilization (%)',
          data: this.utilization.teacherUsage.map((t: any) => t.percent),
          backgroundColor: '#4C9AFF'
        }]
      },
      options: {
        plugins: { title: { display: true, text: 'Teacher Utilization (%)' } },
        scales: {
          y: { beginAtZero: true, max: 100, title: { display: true, text: '%' } }
        },
        maintainAspectRatio: false
      }
    });
  }

  // ----------------- ROOM UTILIZATION -----------------
  renderRoomChart(): Chart | void {
    const canvas = document.getElementById('roomChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization?.roomUsage) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.roomUsage.map((r: any) => r.name),
        datasets: [{
          label: 'Room Utilization (%)',
          data: this.utilization.roomUsage.map((r: any) => r.percent),
          backgroundColor: '#FFD56B'
        }]
      },
      options: {
        plugins: { title: { display: true, text: 'Room Utilization (%)' } },
        scales: {
          y: { beginAtZero: true, max: 100, title: { display: true, text: '%' } }
        },
        maintainAspectRatio: false
      }
    });
  }

  // ----------------- DAY UTILIZATION -----------------
  renderDayChart(): Chart | void {
    const canvas = document.getElementById('dayChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization?.dayUsage) return;

    return new Chart(canvas, {
      type: 'pie',
      data: {
        labels: this.utilization.dayUsage.map((d: any) => d.day),
        datasets: [{
          label: 'Day Utilization (%)',
          data: this.utilization.dayUsage.map((d: any) => d.percent),
          backgroundColor: ['#B5E48C', '#99D98C', '#76C893', '#52B69A', '#34A0A4']
        }]
      },
      options: {
        plugins: {
          title: { display: true, text: 'Day Utilization (%)' },
          legend: { position: 'bottom' }
        },
        maintainAspectRatio: false
      }
    });
  }

  // ----------------- TIME SLOT UTILIZATION -----------------
  renderTimeSlotChart(): Chart | void {
    const canvas = document.getElementById('timeChart') as HTMLCanvasElement;
    if (!canvas || !this.utilization?.timeSlotUsage) return;

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.utilization.timeSlotUsage.map((t: any) => t.slot),
        datasets: [{
          label: 'Hours Used',
          data: this.utilization.timeSlotUsage.map((t: any) => t.used),
          backgroundColor: '#6EA8FE'
        }]
      },
      options: {
        plugins: {
          title: { display: true, text: 'Time Slot Utilized Hours per Week' },
          legend: { display: false }
        },
        scales: {
          y: { beginAtZero: true, title: { display: true, text: 'Hours' } },
          x: { title: { display: true, text: 'Time Slot' } }
        },
        maintainAspectRatio: false
      }
    });
  }
}
