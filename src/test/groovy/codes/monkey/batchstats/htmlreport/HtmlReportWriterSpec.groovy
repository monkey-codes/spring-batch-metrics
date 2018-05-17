package codes.monkey.batchstats.htmlreport

import spock.lang.Specification

import java.nio.charset.Charset

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class HtmlReportWriterSpec extends Specification {

    public static final String CHARSET = "UTF-8"
    private HtmlReportWriter writer
    private String dataPlaceholderToken = "\$reportData"
    private String prefix = "var data = ["
    private String suffix = "];"
    private String template = """
            |<html>
            |<script>
            |$dataPlaceholderToken
            |</script>
            | <body>
            |    hello world
            | </body>
            |</html>""".stripMargin()
    private String data = """{"ts":1525040227956, "type":"COUNTER", "name":"job.step1.process.error", "count":1}
            |{"ts":1525040227966, "type":"COUNTER", "name":"job.step1.read.error", "count":1}
            |{"ts":1525040227968, "type":"TIMER", "name":"job.step1.process", "count":14, "min":124.81230099999999, "max":204.086431, "mean":167.5347116721129, "stddev":25.53937163951993, "median":166.611604, "p75":185.09159599999998, "p95":204.086431, "p98":204.086431, "p99":204.086431, "p999":204.086431, "mean_rate":4.414780509690453, "m1":0.0, "m5":0.0, "m15":0.0, "rate_unit":"events/second", "duration_unit":"milliseconds"}
            |{"ts":1525040227968, "type":"TIMER", "name":"job.step1.read", "count":13, "min":107.229098, "max":204.698182, "mean":132.36303466579005, "stddev":31.12520354361541, "median":120.051154, "p75":138.22561199999998, "p95":204.698182, "p98":204.698182, "p99":204.698182, "p999":204.698182, "mean_rate":2.817223469897177, "m1":0.0, "m5":0.0, "m15":0.0, "rate_unit":"events/second", "duration_unit":"milliseconds"}
            |{"ts":1525040227969, "type":"TIMER", "name":"job.step1.write", "count":1, "min":144.576314, "max":144.576314, "mean":144.576314, "stddev":0.0, "median":144.576314, "p75":144.576314, "p95":144.576314, "p98":144.576314, "p99":144.576314, "p999":144.576314, "mean_rate":1.4294393351263686, "m1":0.0, "m5":0.0, "m15":0.0, "rate_unit":"events/second", "duration_unit":"milliseconds"}""".stripMargin()


    def setup() {
        writer = new HtmlReportWriter(prefix, suffix)
    }

    def "it should merge html and data"() {
        given:
        InputStream htmlStream = toInputStream(template)
        InputStream dataStream = toInputStream(data)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        when:
        writer.merge(htmlStream, dataStream, outputStream)
        def output = outputStream.toString(CHARSET)

        then:
        output.trim() == template.replace(dataPlaceholderToken, "$prefix\n$data\n$suffix").trim()
    }

    protected ByteArrayInputStream toInputStream(String s) {
        new ByteArrayInputStream(s.getBytes(Charset.forName(CHARSET)))
    }
}
