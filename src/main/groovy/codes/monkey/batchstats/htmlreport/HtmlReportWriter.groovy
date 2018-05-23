package codes.monkey.batchstats.htmlreport

import java.util.stream.Stream

import static java.util.stream.Stream.concat
import static java.util.stream.Stream.of

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class HtmlReportWriter {
    public static final String DEFAULT_PLACEHOLDER = "\$reportData"
    public static final String DEFAULT_CHARSET = "UTF-8"

    private String dataPlaceholderToken = DEFAULT_PLACEHOLDER
    private String charsetName = DEFAULT_CHARSET
    private String prefix = ""
    private String suffix = ""

    HtmlReportWriter(String prefix, String suffix) {
        this(DEFAULT_PLACEHOLDER, DEFAULT_CHARSET, prefix, suffix)
    }

    HtmlReportWriter(String dataPlaceholderToken, String charsetName, String prefix, String suffix) {
        this.dataPlaceholderToken = dataPlaceholderToken
        this.charsetName = charsetName
        this.prefix = prefix
        this.suffix = suffix
    }

    def merge(InputStream templateStream, InputStream dataStream, OutputStream outputStream) {
        def writer = new BufferedWriter(new OutputStreamWriter(outputStream, charsetName))
        toStream(templateStream).flatMap({ line ->
            line.contains(dataPlaceholderToken) ?
                    concat(of(prefix),
                            concat(toStream(dataStream).map({row -> row + ","})
                                    , of(suffix))) : of(line)
        }).forEach({ line ->
            writer.writeLine(line)
        })
        writer.flush()
    }

    protected Stream<String> toStream(InputStream templateStream) {
        new BufferedReader(new InputStreamReader(templateStream, charsetName)).lines()
    }
}
