package codes.monkey.batchstats.htmlreport

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class HtmlReportJobExecutionListener implements JobExecutionListener{

    String dataPath
    String outputPath

    HtmlReportJobExecutionListener(
            String dataPath, String outputPath){
        this.dataPath = dataPath
        this.outputPath = outputPath
    }

    @Override
    void beforeJob(JobExecution jobExecution) {

    }

    @Override
    void afterJob(JobExecution jobExecution) {
        def replaceToken = '<!-- $reportData -->'
        def writer = new HtmlReportWriter(replaceToken, HtmlReportWriter.DEFAULT_CHARSET,
                """<script>
var events = [""", """];
</script>""")
        new File(dataPath).withInputStream { dataStream ->
            new File(outputPath).withOutputStream { ostream ->
                def templateStream = getClass().getClassLoader().getResourceAsStream("static/report.html") ?:
                        new ByteArrayInputStream("""
                            |<html>
                            |    <head>
                            |    ${replaceToken}
                            |    </head>
                            |    <body>Dummmy report template</body>
                            |</html>
                            |""".stripMargin().getBytes("UTF-8"))

                writer.merge(templateStream, dataStream, ostream )
                templateStream.close()
            }
        }

    }
}
