import _ from 'lodash';
import './style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.min';
import 'keen-dataviz/dist/keen-dataviz.min.css';
import Keen from 'keen-dataviz';

function component() {
  var element = document.createElement('div');

  // Lodash, now imported by this script
  element.innerHTML = _.join(['Hello', 'webpack'], ' ');
  element.classList.add('hello');

  return element;
}

document.body.appendChild(component());

function line(){
  //window.Keen = Keen;
  console.log('here');
  window.TryThis = Keen;
  console.log(Keen);
  //console.log(Keen.Dataviz);
  //console.log(Keen.Dataviz);
  //var chart = new Keen.Dataviz()
  var chart = new Keen()
    .el('.test')
    .colors(['red', 'orange', 'green'])
    .height(180)
    .title('New Customers per Week')
    .chartType('line')
    .prepare();

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
      ['2016-08-01T07:00:00.000', 6],
      ['2016-08-01T07:00:30.000', 2],
      ['2016-08-01T07:01:00.000', 1],
      ['2016-08-01T07:01:30.000', 269]
    ]
  };

  chart
    .data(data)
    .render();
}
setTimeout(function(){
  line()
}, 2000)

