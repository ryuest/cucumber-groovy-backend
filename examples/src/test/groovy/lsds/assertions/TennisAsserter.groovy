package lsds.assertions

import com.williamhill.lsds.ft.record.TopicUpdate
import org.junit.Assert

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This class implements the necessary steps in order to assert the order, number and content
 * of all the incidents for a basketball match.
 */
class TennisAsserter extends IncidentsAsserter implements Assertable {

    private static String msg = "The recorded Tennis incidents lists must be identical in size, order and content"
    private static Tuple2<Pattern, String>[] DEFAULT_TENNIS_RGX = [
            new Tuple2(Pattern.compile("OB_EV(\\d+)"), "OB_EVsomeEventId"),
            new Tuple2(Pattern.compile("timestamp\":(\\d+)"), "timestamp\":someTimestamp"),
            new Tuple2(Pattern.compile("_([a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12})\""), "_someUUID\"")
    ]

    /**
     * Constructor.
     *
     * @param expected the saved/recorded and checked/correct expected incidents for a given match
     * @param actual the recorded incidents for the test run
     */
    TennisAsserter(List<TopicUpdate>  expected, List<TopicUpdate>  actual) {
        super(msg, expected, actual)
        masksList = updateMasks(expected, updateMasks(actual, DEFAULT_TENNIS_RGX))
        properties = [ this.&equality ]
    }

    /**
     * This closure asserts that the expected and the actual
     * incidents are equal.
     *
     * @param args the arguments map
     * @return the equality assertion
     */
    private def equality(Map args) { Assert.assertEquals("The incidents must be identical", args["expected"], args["actual"]) }

//    private def equality(Map args) {
//        Assert.assertTrue("Incidents must be the same or have the same timestamp",
//            args["expected"] == args["actual"] ||
//                    sameTimestamp(args["expected"] as TopicUpdate, args["actual"] as TopicUpdate))
//    }

    private boolean sameTimestamp(TopicUpdate actual, TopicUpdate expected) {

        println "\n\nELMENTS ARENT EQUAL =>"
        println "Actual: $actual"
        println "Expected: $expected"

        final Pattern p = Pattern.compile("timestamp\":(\\d+)")
        final Matcher actMatcher = p.matcher(actual.value)
        final Matcher expMatcher = p.matcher(expected.value)
        final String actTime = actMatcher.find() ? actMatcher.group(1) : "NotFoundActualTimeStamp"
        final String expTime = expMatcher.find() ? expMatcher.group(1) : "NotFoundExpectedTimeStamp"

        println "TIMESTAMP IS => "
        println "Actual Timestamp: $actTime"
        println "Expected Timestamp: $expTime"

        return actTime == expTime
    }

    // sort the lists and compare?
    // override specific assertions and do a default on this?
    // override it all for tennis?
}
