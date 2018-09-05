package lsds.assertions

import com.williamhill.lsds.ft.record.TopicUpdate
import org.junit.Assert

import java.util.regex.Pattern

/**
 * This class implements the necessary steps in order to assert the order, number and content
 * of all the incidents for a basketball match.
 */
class FootballAsserter extends IncidentsAsserter implements Assertable {

    private static String msg = "The recorded Football incidents lists must be identical in size, order and content"
    private static Tuple2<Pattern, String>[] DEFAULT_FOOTBALL_RGX = [
            new Tuple2(Pattern.compile("OB_EV(\\d+)"), "OB_EVsomeEventId"),
            new Tuple2(Pattern.compile("timestamp\":(\\d+)"), "timestamp\":someTimestamp")
    ]

    /**
     * Constructor.
     *
     * @param expected the saved/recorded and checked/correct expected incidents for a given match
     * @param actual the recorded incidents for the test run
     */
    FootballAsserter(List<TopicUpdate>  expected, List<TopicUpdate>  actual) {
        super(msg, expected, actual)
        masksList = updateMasks(expected, updateMasks(actual, DEFAULT_FOOTBALL_RGX))
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
}
