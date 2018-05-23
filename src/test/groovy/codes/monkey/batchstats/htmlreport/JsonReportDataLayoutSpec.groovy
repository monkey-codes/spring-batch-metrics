package codes.monkey.batchstats.htmlreport

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import java.time.Instant
import java.time.format.DateTimeFormatter


class JsonReportDataLayoutSpec extends Specification {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private JsonReportDataLayout layout
    private ObjectMapper mapper
    private String testDate

    def setup() {
        layout = new JsonReportDataLayout()
        mapper = new ObjectMapper()
        testDate = "2018-04-27T04:50:47.096+0000"

    }

    def "it should convert counter log message into JSON"() {
        given:
        def msg = "type=COUNTER, name=job.step1.process.error, count=2"
//        def msg = "type=TIMER, name=job.step1.write, count=7, min=119.22373999999999, max=208.207086, mean=153.81313766867297, stddev=34.63112617307733, median=137.923466, p75=202.96713699999998, p95=208.207086, p98=208.207086, p99=208.207086, p999=208.207086, mean_rate=0.26738483959008247, m1=0.3592973791888932, m5=0.39040710624200003, m15=0.39671260489594073, rate_unit=events/second, duration_unit=milliseconds"
        def ts = Instant.from(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).parse(testDate)).toEpochMilli()

        when:
        def result = mapper.readValue(layout.toJSON(ts, msg), Map)

        then:
        result['ts'] == ts
        result['name'] == 'job.step1.process.error'
        result['count'] == 2
        result['type'] == 'COUNTER'
    }
}
