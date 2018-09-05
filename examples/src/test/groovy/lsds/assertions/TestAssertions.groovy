package lsds.assertions

import lsds.ft.context.FileManager
import com.williamhill.lsds.ft.record.TopicUpdate
import org.junit.Assert
import org.junit.Test

class TestAssertions {

    @Test void passWithoutMaskingMatch() {
        Assert.assertTrue("Empty error list",
                new BasketballAsserter([createTopic("value")], [createTopic("value")])
                        .reportAssertionsResult().isEmpty())
        new BasketballAsserter([createTopic("value")], [createTopic("value")]).performAssertions()
    }

    @Test void passMaskingMatch() {
        List<TopicUpdate> l1 = [createTopic("value"), createTopic(eventIdOneExample), createTopic("value")]
        List<TopicUpdate> l2 = [createTopic("value"), createTopic(eventIdTwoExample), createTopic("value")]
        Assert.assertTrue("Empty error list",
                new BasketballAsserter(l1, l2).reportAssertionsResult().isEmpty())

        List<TopicUpdate> l3 = [createTopic(timestampAndEventIdOneExample), createTopic("value"), createTopic("value")]
        List<TopicUpdate> l4 = [createTopic(timestampAndEventIdTwoExample), createTopic("value"), createTopic("value")]
        Assert.assertTrue("Empty error list",
                new BasketballAsserter(l3, l4).reportAssertionsResult().isEmpty())

        List<TopicUpdate> l5 = [createTopic("value"), createTopic("value"), createTopic(timestampOneExample)]
        List<TopicUpdate> l6 = [createTopic("value"), createTopic("value"), createTopic(timestampTwoExample)]
        Assert.assertTrue("Empty error list",
                new BasketballAsserter(l5, l6).reportAssertionsResult().isEmpty())

        new BasketballAsserter(l1, l2).performAssertions()
        new BasketballAsserter(l3, l4).performAssertions()
        new BasketballAsserter(l5, l6).performAssertions()
    }

    @Test void failWrongTopicWithoutMasking() {
        Assert.assertFalse("Non empty error list",
                new BasketballAsserter([createTopic("value")], [diffTopicTopic]).reportAssertionsResult().isEmpty())
    }

    @Test(expected = AssertionError) void failWrongTopicWithoutMaskingAbortsExecution() {
        new BasketballAsserter([createTopic("value")], [diffTopicTopic]).performAssertions()
    }

    @Test void failWrongPathWithoutMasking() {
        Assert.assertFalse("Non empty error list",
                new BasketballAsserter([createTopic("value")], [diffPathTopic]).reportAssertionsResult().isEmpty())
    }

    @Test void failWrongFileWithoutMasking() {
        Assert.assertFalse("Non empty error list",
                new BasketballAsserter([createTopic("value")], [diffFileTopic]).reportAssertionsResult().isEmpty())
    }

    @Test void failWrongActionWithoutMasking() {
        Assert.assertFalse("Non empty error list",
                new BasketballAsserter([createTopic("value")], [diffActionTopic]).reportAssertionsResult().isEmpty())
    }

    @Test void failWrongValueWithoutMasking() {
        Assert.assertFalse("Non empty error list",
                new BasketballAsserter([createTopic("value")], [diffActionTopic]).reportAssertionsResult().isEmpty())
    }

    @Test(expected = AssertionError) void failDifferentSizes() {
        TopicUpdate t = createTopic("value")
        new BasketballAsserter([t], [t, t]).reportAssertionsResult()
    }

    @Test void passSameTimestampIgnoresOrder() {
        List<TopicUpdate> actual = fileManager.loadExpectationsFromCsv("tennis", "actual-julia-stamatova-vs-gebriela-mihaylova")
        List<TopicUpdate> expected = fileManager.loadExpectationsFromCsv("tennis", "julia-stamatova-vs-gebriela-mihaylova")
        def topicComparator = new Comparator<TopicUpdate>() {
            @Override
            int compare(TopicUpdate o1, TopicUpdate o2) {
                if (o1.file <=> o2.file == 0) {
                    if (o1.topic <=> o2.topic == 0) {
                        if (o1.action <=> o2.action == 0) {
                            if (o1.path <=> o2.path == 0) {
                                return o1.value <=> o2.value
                            } else {
                                return o1.path <=> o2.path
                            }
                        } else {
                            return o1.action <=> o2.action
                        }
                    } else {
                        return o1.topic <=> o2.topic
                    }
                } else {
                    return o1.file <=> o2.file
                }
            }
        }



        actual.sort(topicComparator)
        expected.sort(topicComparator)
        new TennisAsserter(expected, actual).performAssertions()
    }

    def fileManager = new FileManager()

    def createTopic = { String v -> new TopicUpdate("action", "file", "path", "topic", v) }
    def diffTopicTopic = new TopicUpdate("action", "file", "path", "diff", "value")
    def diffPathTopic = new TopicUpdate("action", "file", "diff", "topic", "value")
    def diffFileTopic = new TopicUpdate("action", "diff", "path", "topic", "value")
    def diffActionTopic = new TopicUpdate("diff", "file", "path", "topic", "value")

    def eventIdOneExample ="\"insert\",\"input_12782805_20180426_113855_msg0_tid56.xml\",\"full json\",\"event\",\"{\"\"id\":\"OB_EV1419896472\"\",\"\"name\"\":\"\"SSD Treviso Basket U20 vs PMS Basket Moncalieri U20\"\",\"\"type\"\":\"\"BASKETBALL\"\",\"\"startTime\"\":null,\"\"inPlay\"\":false,\"\"teamA\"\":{\"\"id\"\":\"\"A\"\",\"\"name\"\":\"\"SSD Treviso Basket U20\"\"},\"\"teamB\"\":{\"\"id\"\":\"\"B\"\",\"\"name\"\":\"\"PMS Basket Moncalieri U20\"\"},\"\"clock\"\":{\"\"currentTime\"\":{\"\"clockDirection\"\":\"\"DOWN\"\",\"\"period\"\":\"\"NOT_STARTED\"\",\"\"duration\"\":0.0},\"\"ticking\"\":false},\"\"stats\"\":{\"\"TOTAL\"\":{\"\"points\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"onePointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"onePointerAttempts\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"twoPointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"threePointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"fouls\"\":{\"\"home\"\":0,\"\"away\"\":0}}}}\""
    def eventIdTwoExample ="\"insert\",\"input_12782805_20180426_113855_msg0_tid56.xml\",\"full json\",\"event\",\"{\"\"id\":\"OB_EV2347632434\"\",\"\"name\"\":\"\"SSD Treviso Basket U20 vs PMS Basket Moncalieri U20\"\",\"\"type\"\":\"\"BASKETBALL\"\",\"\"startTime\"\":null,\"\"inPlay\"\":false,\"\"teamA\"\":{\"\"id\"\":\"\"A\"\",\"\"name\"\":\"\"SSD Treviso Basket U20\"\"},\"\"teamB\"\":{\"\"id\"\":\"\"B\"\",\"\"name\"\":\"\"PMS Basket Moncalieri U20\"\"},\"\"clock\"\":{\"\"currentTime\"\":{\"\"clockDirection\"\":\"\"DOWN\"\",\"\"period\"\":\"\"NOT_STARTED\"\",\"\"duration\"\":0.0},\"\"ticking\"\":false},\"\"stats\"\":{\"\"TOTAL\"\":{\"\"points\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"onePointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"onePointerAttempts\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"twoPointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"threePointers\"\":{\"\"home\"\":0,\"\"away\"\":0},\"\"fouls\"\":{\"\"home\"\":0,\"\"away\"\":0}}}}\""
    def timestampAndEventIdOneExample = "\"insert\",\"input_12782805_20180426_120401_msg0_tid56.xml\",\"full json\",\"incidents\",\"[{\"\"id\":\"OB_EV1419896472#88360020\"\",\"\"eventId\":\"OB_EV1419896472\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":1526567675740,\"\"type\"\":\"\"warning3Min\"\",\"\"time\"\":null},{\"\"id\":\"OB_EV1419896472#88360167\"\",\"\"eventId\":\"OB_EV1419896472\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":1526567676743,\"\"type\"\":\"\"finalCallToCourtByRefs\"\",\"\"time\"\":null},{\"\"id\":\"OB_EV1419896472#firstQuarterStart\"\",\"\"eventId\":\"OB_EV1419896472\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":1526567677756,\"\"type\"\":\"\"firstQuarterStart\"\",\"\"time\"\":null}]\""
    def timestampAndEventIdTwoExample = "\"insert\",\"input_12782805_20180426_120401_msg0_tid56.xml\",\"full json\",\"incidents\",\"[{\"\"id\":\"OB_EV2347632434#88360020\"\",\"\"eventId\":\"OB_EV2347632434\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":3286452735234,\"\"type\"\":\"\"warning3Min\"\",\"\"time\"\":null},{\"\"id\":\"OB_EV2347632434#88360167\"\",\"\"eventId\":\"OB_EV2347632434\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":7326532423634,\"\"type\"\":\"\"finalCallToCourtByRefs\"\",\"\"time\"\":null},{\"\"id\":\"OB_EV2347632434#firstQuarterStart\"\",\"\"eventId\":\"OB_EV2347632434\"\",\"\"subType\"\":null,\"\"teamType\"\":null,\"\"data\"\":{},\"\"timestamp\":1244543343245,\"\"type\"\":\"\"firstQuarterStart\"\",\"\"time\"\":null}]\""
    def timestampOneExample = "\"\"timestamp\":1526567681780"
    def timestampTwoExample = "\"\"timestamp\":1248239655349"
}
