import _ from 'lodash';
import './style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.min';
//import events from './test-data.js';
import moment from  'moment';
import downSampler from 'downsample-lttb';
import Chart from 'chart.js';

const events = window.events || []
const chartColors = {
  red: 'rgb(255, 99, 132)',
  orange: 'rgb(255, 159, 64)',
  yellow: 'rgb(255, 205, 86)',
  green: 'rgb(75, 192, 192)',
  blue: 'rgb(54, 162, 235)',
  purple: 'rgb(153, 102, 255)'
};

function lineGraph(events, name, attribute, unit_attribute){
  const title = `${name} ${attribute}`;
  const id = _.replace(`${name}.${attribute}`, /\./g, '_');
  const chartEl = $('.template').clone().removeClass('hidden template');
  $('.chart-title', chartEl).html(title);
  $('.graph', chartEl).attr('id', id);
  chartEl.appendTo('.row');

  const durationUnit = events[0][unit_attribute];

  const data = _.chain(events)
    .map(event => [event.ts, event[attribute]])
    .value()

  const requiredSamples = 20;

  const downSampledData = (data.length < requiredSamples)? data : downSampler.processData(data, requiredSamples)

  const graphData = _.chain(downSampledData)
    .map(event => [new Date(event[0]), event[1]])
    .map(event => ({x: event[0], y: event[1]}))
    .value();

  const chart = new Chart(document.getElementById(id).getContext('2d'), {
    type: 'line',
    data: {
      datasets: [
        {
          label: title,
          borderColor: chartColors.blue,
          fill: false,
          data: graphData
        }
      ]
    },
    options: {
      responsive: true,
      legend: {
        display: true
      },
      title: {
        display: false
      },
      scales: {
        xAxes: [{
          type: 'time',
          display: true,
          scaleLabel: {
            display: true
          },
          ticks: {
            major: {
              fontStyle: 'bold',
              fontColor: '#FF0000'
            }
          }
        }],
        yAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: `${attribute} (${durationUnit})`
          }
        }]
      }
    }
  });
}

function metric(events, name, attribute){
  const chartEl = $('.metric-template').clone().removeClass('hidden metric-template');
  const lastEvent = _.last(events);
  const title = `${name} ${attribute}`;
  $('.chart-title', chartEl).html(title);
  $('.metric', chartEl).html(_.last(events)[attribute]);
  $('.metric-label', chartEl).html(title);
  chartEl.appendTo('.row');
}

const results = _.chain(events)
  .map(event => _.assign(event, {time: moment.unix(event.ts/1000).format("YYYY-MM-DDThh:mm:ss")}))
  .groupBy(event => event.name)
  .forIn((events, name) => {
    if(events.length < 1 || _.last(events).count == 0) return;
    switch(events[0].type) {
      case 'TIMER':
        lineGraph(events, name, 'mean', 'duration_unit');
        lineGraph(events, name, 'mean_rate', 'rate_unit');
        metric(events, name, 'count');
        break;
      case 'COUNTER':
        metric(events, name, 'count')
        break;
    }
  }).value();

