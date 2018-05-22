import _ from 'lodash';
import './style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.min';
import 'keen-dataviz/dist/keen-dataviz.min.css';
import Keen from 'keen-dataviz';
import events from './test-data.js';
import moment from  'moment';
import downSampler from 'downsample-lttb';


function line(){
  //console.log(events[0]);
  const results = _.chain(events)
    .map(event => _.assign(event, {time: moment.unix(event.ts/1000).format("YYYY-MM-DDThh:mm:ss")}))
    .filter(event => event.type === "TIMER")
    .groupBy(event => event.name)
    .forIn((events, name) => {
      const id = _.replace(name, /\./g, '_');
      const chartEl = $('.template').clone().removeClass('hidden template');
      $('.chart-title', chartEl).html(name);
      $('.graph', chartEl).attr('id', id);
      chartEl.appendTo('.row');

      const chart = new Keen()
            .el(`#${id}`)
      .colors(['red', 'orange', 'green'])
            .height(180)
      //.title('New Customers per Week')
            .chartType('line')
            .prepare();

      const data = _.chain(events)
        .map(event => [event.ts, event.mean])
        .value()
      const downSampledData = _.chain(downSampler.processData(data, 10))
        .map(event => [moment.unix(event[0]/1000).format("YYYY-MM-DDThh:mm:ss"), event[1]])
        .value();

      console.log(downSampledData);
      chart.data({result: downSampledData})
          .render();

    })
  .value();
  //console.log(results);
  const chart = new Keen()
    .el('.test')
    .colors(['red', 'orange', 'green'])
    .height(180)
    .title('')
    .chartType('line')
    .prepare()

  var data2 = {
    result: [{
      value: 6,
      timeframe: {start: "2016-08-01T07:00:00.000Z", end: "2016-08-08T07:00:00.000Z"}
    }, {
      value: 2,
      timeframe: {start: "2016-08-08T07:00:00.000Z", end: "2016-08-15T07:00:00.000Z"}
    }, {
      value: 1,
      timeframe: {start: "2016-08-15T07:00:00.000Z", end: "2016-08-22T07:00:00.000Z"}
    }, {
      value: 2,
      timeframe: {start: "2016-08-22T07:00:00.000Z", end: "2016-08-29T07:00:00.000Z"}
    }, {
      value: 269,
      timeframe: {start: "2016-08-29T07:00:00.000Z", end: "2016-09-05T07:00:00.000Z"}
    }, {value: 14, timeframe: {start: "2016-09-05T07:00:00.000Z", end: "2016-09-12T07:00:00.000Z"}}]
  };

  var data = {
    result: [
      ['2016-08-01T07:00:00', 6.23423423423],
      ['2016-08-01T07:00:30', 2],
      ['2016-08-01T07:01:00', 1],
      ['2016-08-01T07:01:30', 269]
    ]
  };

  chart
    .data(data)
    .render();
}
line();

