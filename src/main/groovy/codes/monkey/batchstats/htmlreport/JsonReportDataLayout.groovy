package codes.monkey.batchstats.htmlreport

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase


class JsonReportDataLayout extends LayoutBase<ILoggingEvent> {

    @Override
    String doLayout(ILoggingEvent event) {
        toJSON(event.getTimeStamp(), event.getFormattedMessage()) as String
    }

    String toJSON(long timeStamp, String msg) {
        [
                [/(type|name|rate_unit|duration_unit)=([a-zA-Z0-9.\/]+)/, '$1=\"$2\"'],
                [/([a-zA-Z_0-9]+)=/, '"$1"='],
                [/=/, ':'],
                [/(.+)/, '{$1}\n']
        ].inject("ts=$timeStamp, $msg") { m, replacement -> m.replaceAll(replacement[0], replacement[1]) }
    }
}
