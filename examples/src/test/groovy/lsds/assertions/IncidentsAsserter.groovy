package lsds.assertions

import com.williamhill.lsds.ft.record.TopicUpdate
import groovy.util.logging.Slf4j
import org.junit.Assert

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This class provides a the means to assert the results of a test run for a replayed match for a given
 * sport against previously collected data.
 *
 * - The data passed to the class is the checked/stored/correct data from a recorded
 * match.
 * - The expected and actual incidents list MUST have the same size.
 * - The masks are provided in order to allow comparing only strings and avoid using any other type of parsing
 * to build/create objects and therefore limit the possibility of missing problems due to framework introduced code,
 * they enable the child class to assert equality ignoring different but not meaningful values like ids or time
 * dependant data.
 * - The globalAssertions method may be used to assert something on the full lists state and break the execution
 * (use it by overriding it), before inspecting each element on the lists. In case of failure, an
 * {@link AssertionError} will be raised.
 * - The properties closure list will be used to assert each element on the expected list against the correspondent
 * element on the actual list, on the same index. Every closure passed to the {@link IncidentsAsserter}
 * constructor MUST define a msg, an expected {@link TopicUpdate} and an actual {@link TopicUpdate}, as these will
 * be the arguments passed to the closure by the underlying super class.
 */
@Slf4j
abstract class IncidentsAsserter implements Assertable {

    Tuple2<Pattern, String>[] masksList
    List<Closure> properties
    final List<TopicUpdate>  expected
    final List<TopicUpdate>  actual
    final String msg

    /**
     * Constructor.
     *
     * @param msg the assertion message
     * @param expected the expected incidents list
     * @param actual the actual incidents list
     */
    IncidentsAsserter(msg, expected, actual) {
        this.expected = expected
        this.actual = actual
        this.msg = msg
    }

    /**
     * This method will provide the template implementation for asserting
     * properties on all and each incidents recorded on a test run for a
     * sport. It fails with an {@link AssertionError} if some property fails.
     */
    @Override void performAssertions() {
        log.debug("Performing assertions...")
        def errors = reportAssertionsResult()
        StringBuilder sb = new StringBuilder("\n\nAssertions Report:\n\n- $msg, but ${errors.size()} errors were found!\n\n")
        if (errors) {
            errors.each { sb.append(it.toString()).append("\n") }
            Assert.fail(sb.toString())
        } else {
            log.info("All assertions passed")
        }
    }

    /**
     * This method will provide the template implementation for asserting
     * properties on all and each incidents recorded on a test run for a
     * sport. It collects all the failed assertions messages as the result.
     *
     * @return the identifying message(s) for the {@link AssertionError}(s) or empty list if there are none
     */
    @Override List<String> reportAssertionsResult() {
        log.debug("Creating the assertions report...")
        try {
            Assert.assertEquals("$msg => Topics lists aren't the same size", expected.size(), actual.size())
            globalAssertions() // we stop immediately because we are asserting properties on the entire lists
        }
        catch (AssertionError e) {
            log.info(e.message)
            log.info("Continuing...")
        }

        final String[] errors = []
        int index = 0
        [expected, actual].transpose().each { TopicUpdate expectedTopic, TopicUpdate actualTopic ->
            maskData(expectedTopic, actualTopic)
            errors += assertProperties(index, expectedTopic, actualTopic)
            index++
        }
        errors
    }

    /**
     * This method should be overridden to provide any other
     * global assertion necessary. The default implementation
     * does nothing.
     */
    void globalAssertions() {}

    /**
     * This closure invokes all closures (the properties contained in
     * the properties list) to the given expected and actual incidents.
     * In the case of failure, the failed assertion(s) message(s) are
     * returned as the result.
     *
     * @param idx the index of the current topics in the incidents list for debug purposes
     * @param expected the expected {@link TopicUpdate} incident
     * @param actual the actual {@link TopicUpdate} incident
     * @return the list of failed assertions/properties messages
     */
    String[] assertProperties(idx, TopicUpdate expected, TopicUpdate actual) {
        String[] result = []
        properties.each {
            try { it(msg: "", expected: expected, actual: actual) }
            catch (AssertionError e) { result += "#$idx => ${e.message}" }
        }
        result
    }


    /**
     * This method will update the masks to use with the one
     * which will match exactly and only the event id, if found
     * in one of the incidents, otherwise, it returns an empty
     * string.
     *
     * @param expected the incidents list
     * @param masks the known sport masks list
     * @return the string containing the event id or empty
     */
     Tuple2<Pattern, String>[] updateMasks(List<TopicUpdate> topics, Tuple2<Pattern, String>[] masks) {
        Tuple2<Pattern, String>[] l = masks.clone()
        final String expectedEvIdOnly = findEventId(topics, masks)
        if (!expectedEvIdOnly.isEmpty()) l += [new Tuple2(Pattern.compile(expectedEvIdOnly), "someEventId")]
        return l
    }

    /**
     * This method will look for the open bet event id and
     * return only the id part without the literal prefix.
     *
     * @param topics the incidents to where to perform the lookup
     * @param masks the known sport masks list
     * @return the event id without any literal prefix
     */
    private String findEventId(List<TopicUpdate> topics, Tuple2<Pattern, String>[] masks) {
        final Pattern p = masks.first().first
        for (TopicUpdate t: topics) {
            final Matcher m = p.matcher(t.value)
            if (m.find()) return m.group(1)
        }
        return ""
    }

    /**
     * This closure applies the list of masks to the given topics.
     */
    def maskData = { TopicUpdate a, TopicUpdate b -> masksList.each { m -> [a, b].each { maskTopicValue(it, m) } } }

    /**
     * This method will mask the matching parts of the value field
     * inside the topic with its replacer.
     *
     * @param t the {@link TopicUpdate} to mask
     * @param mask the tuple containing a pattern (first) and its replacer (second)
     */
    def maskTopicValue = { TopicUpdate t, Tuple2<Pattern, String> mask ->  t.value = t.value.replaceAll(mask.first, mask.second) }
}
