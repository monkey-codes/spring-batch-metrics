package codes.monkey.batchstats

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.slf4j.LoggerFactory

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsEventsGrabber {
    private ListAppender<ILoggingEvent> listAppender

    StatsEventsGrabber() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory()
        listAppender = new ListAppender<>()
        listAppender.setName("testAppender")
        listAppender.context = lc
        listAppender.start()
        ((Logger) LoggerFactory.getLogger(StatsListener.name)).addAppender(listAppender)
    }

    Map<String, String> lastEvent(String name) {
        def loggingEvent = Collections.unmodifiableList(listAppender.list).reverse()
                .find { it.formattedMessage.contains("name=$name,") }
        if (loggingEvent == null) return ["key":"nullMap","count":"0"]
        def properties = new Properties()
        properties.load(new StringReader(loggingEvent.formattedMessage.replace(', ', '\n')))
        new HashMap<>(properties)

    }

    void stop() {
        ((Logger) LoggerFactory.getLogger(StatsListener.name)).detachAppender(listAppender)
        listAppender.stop()
    }

    static <T> Matcher<StatsEventsGrabber> lastEvent(String name, Matcher<T> valueMatcher) {
        new TypeSafeMatcher<StatsEventsGrabber>() {
            @Override
            protected boolean matchesSafely(StatsEventsGrabber item) {
                return valueMatcher.matches(item.lastEvent(name))
            }

            @Override
            void describeTo(Description description) {
                description.appendText("stats event ").appendText(name).appendText(" ")
                valueMatcher.describeTo(description)
            }

            @Override
            protected void describeMismatchSafely(StatsEventsGrabber item, Description mismatchDescription) {
                mismatchDescription.appendText("stats event ")
                        .appendText(name).appendText(" ")
                valueMatcher.describeMismatch(item.lastEvent(name), mismatchDescription)
            }
        }
    }

    static <T> Matcher<StatsEventsGrabber> combineLastEvents(final String names, Matcher<T> valueMatcher) {
        new TypeSafeMatcher<StatsEventsGrabber>() {
            @Override
            protected boolean matchesSafely(StatsEventsGrabber item) {


                return valueMatcher.matches(combine(item))
            }

            private Map<String, String> combine(item) {
                names.split(",")
                        .collect {
                    item.lastEvent(it.trim())
                }.inject([:]) { Map<String, String>result, i ->
                    i.each { k, v ->
                        if (!v.isNumber()) return result
                        def accumelator = new BigDecimal((result[k] ?: '0') as String)
                        accumelator = accumelator + new BigDecimal(v)
                        result[k] = accumelator.toPlainString()
                    }
                    result
                }
            }

            @Override
            void describeTo(Description description) {
                description.appendText("stats event ").appendText(names).appendText(" ")
                valueMatcher.describeTo(description)
            }

            @Override
            protected void describeMismatchSafely(StatsEventsGrabber item, Description mismatchDescription) {
                mismatchDescription.appendText("stats event ")
                        .appendText(names).appendText(" ")
                valueMatcher.describeMismatch(combine(item), mismatchDescription)
            }
        }
    }
}
