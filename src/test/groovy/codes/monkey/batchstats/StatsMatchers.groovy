package codes.monkey.batchstats

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsMatchers {

    static Closure exceptionOn(... errorOnItem) {
        { item ->
            if (item instanceof List) {
                if (item.any { errorOnItem.contains(it) })
                    throw new RuntimeException("fake writer error")
                return item
            }
            if (errorOnItem.contains(item))
                throw new RuntimeException("fake error")
            item
        }
    }

    static Matcher<Map<? extends String, ? extends String>> hasCount(Matcher<Integer> valueMatcher) {
        def convertToInt = new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String item) {
                valueMatcher.matches(Integer.valueOf(item))
            }

            @Override
            void describeTo(Description description) {
                valueMatcher.describeTo(description)
            }
        }
        Matchers.hasEntry(Matchers.is('count'), convertToInt)
    }

    static Matcher<Map<? extends String, ? extends String>> hasCount(int count) {
        Matchers.hasEntry('count', String.valueOf(count))
    }
}
