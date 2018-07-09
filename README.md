An example of how to [extract performance metrics out of a Spring Batch Job using Dropwizard Metrics](https://blog.monkey.codes/how-to-gather-performance-metrics-in-spring-batch/).

![Sample HTML Report](https://res.cloudinary.com/monkey-codes/image/upload/v1527829338/batch_stats_html_report.png)

## Usage

The gradle build will expect node 8.9.1 and yarn to be installed on the system.

```
$ git clone https://github.com/monkey-codes/spring-batch-metrics.git
$ cd spring-batch-metrics
# if you are using nvm
$ nvm use 8.9.1
$ ./gradlew clean build
$ ./gradlew bootRun -Dspring.batch.job.names=single.thread.decorator.driven.job -Dcodes.monkey.metric.output.dir=$(pwd)/build
# to view the html report generated in the tmp folder
$ open ./build/report.html
```
