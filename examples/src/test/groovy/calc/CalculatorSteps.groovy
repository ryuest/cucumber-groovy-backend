package calc

import cucumber.api.PendingException
import cucumber.api.Scenario
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Paths

import static com.williamhill.scoreboards.replayer.properties.InputMode.MANUAL
import static com.williamhill.scoreboards.replayer.properties.TransmitMode.JMS
import static java.util.concurrent.TimeUnit.SECONDS
import lsds.assertions.BasketballAsserter
import lsds.assertions.FootballAsserter
import lsds.assertions.IncidentsAsserter
import lsds.assertions.TennisAsserter
import lsds.ft.context.FileManager
import lsds.ft.context.TestContext
import com.williamhill.lsds.ft.record.IncidentRecorder
import com.williamhill.lsds.ft.record.TopicUpdate
import com.williamhill.scoreboards.replayer.client.ReplayerServiceClient
import com.williamhill.scoreboards.replayer.client.builder.ScoreboardsReplayerClientBuilder
import com.williamhill.scoreboards.replayer.properties.Sport
import com.williamhill.scoreboards.replayer.properties.TargetEnvironment

// Add functions to register hooks and steps to this script.
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

// Define a world that represents the test environment.
// Hooks can set up and tear down the environment and steps
// can change its state, e.g. store values used by later steps.

// Create a fresh new world object as the test environment for each scenario.
// Hooks and steps will belong to this object so can access its properties
// and methods directly.
World {
    //   new CustomWorld()
    new TestContext()
}

def sportsTopics = { eventId -> "scoreboards/v1/OB_EV$eventId scoreboards/v1/OB_EV$eventId/summary scoreboards/v1/OB_EV$eventId/incidents".split() }

// This closure gets run before each scenario
// and has direct access to the new world object
// but can also make use of script variables.

Before { Scenario testScenario ->
    scenario = testScenario
    logger = LoggerFactory.getLogger('TestSteps')
    logger.info("Starting Scenario: ${scenario.name}")
    calc = new Calculator() // belongs to this script
}

// Register step definition using Groovy syntax for regex patterns.
// If you use slashes to quote your regexes, you don't have to escape backslashes. 
// Any Given/When/Then function can be used, the name is just to indicate the kind of step.
Given(~/I have entered (\d+) into .* calculator/) { int number ->
    calc.push number
}

// Remember to still include "->" if there are no parameters.
Given(~/\d+ into the/) { ->
    throw new RuntimeException("should never get here since we're running with --guess")
}

// This step calls a Calculator function specified in the step
// and saves the result in the current world object.
When(~/I press (\w+)/) { String opname ->
    result = calc."$opname"()
}

// Use the world object to get any result from a previous step.
// The expected value in the step is converted to the required type.
Then(~/the stored result should be (.*)/) { double expected ->
    assert expected == result
}

Before { Scenario testScenario ->
    scenario = testScenario
    logger = LoggerFactory.getLogger('TestSteps')
    logger.info("Starting Scenario: ${scenario.name}")
}

Given(~/A client subscribe for event, summary and incident topics of (.+) match "(.+)" to Diffusion/) {
    String sportString, String subscribedMatch ->
        sport = Sport.get(sportString)
        matchName = subscribedMatch
        matchFolder = new File(getClass().getClassLoader().getResource("matches/$sportString").getFile(),
                matchNameToFolder(subscribedMatch))
        eventId = generateRandomEventId(sportString)
        diffusionClient().subscribe(sportsTopics(eventId))
}

When(~/(?:Subscribed match is played|a match is finished)/) { ->
    logger.info("Going to replay '$sport' match '$matchName' with id '$eventId' against '$targetEnvironment'")
    replayMatch(eventId, sport, targetEnvironment, matchFolder, incidentRecorder())
    logger.info("Match '$matchName' with id '$eventId' has been replayed")
}


After { Scenario scenario ->
    if (this.hasProperty('eventId') && eventId != null) {
        logger.info("Unsubscribing for '$eventId'")
        diffusionClient().unsubscribe(sportsTopics(eventId))
        diffusionClient().disconnect()
    }
}

private IncidentsAsserter getSportAssertions(Sport sportName, List<TopicUpdate> expected, List<TopicUpdate> actual) {
    switch (sportName) {
        case Sport.BASKETBALL: return new BasketballAsserter(expected, actual)
        case Sport.FOOTBALL: return new FootballAsserter(expected, actual)
        case Sport.TENNIS: return new TennisAsserter(expected, actual)
        default: throw new Exception("Sport not found: ${sportName.name}")
    }
}

private static String generateRandomEventId(String sport) {
    try {
        BufferedReader eventIDList = null;

        try {
            eventIDList = Files.newBufferedReader(Paths.get("EventData" + "/" + sport.toLowerCase().trim() + "/" + "EventIDList.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        String result = null;
        Random rand = new Random();
        int n = 0;

        for (Scanner sc = new Scanner(eventIDList); sc.hasNext();) {
            ++n;
            String line = sc.nextLine();
            if (rand.nextInt(n) == 0)
                result = line;
        }
        println(result)
        return result;
        // (new Random().nextInt(MAX_VALUE) + 1).toString()
    } catch (NullPointerException e) {
        println("Event update skipped");
    }
}


private static String matchNameToFolder(String match) {
    return match.replace(" ", "-").toLowerCase().trim()
}

private static void replayMatch(String eventId, Sport sport, TargetEnvironment targetEnvironment,
                                File matchDirectory, IncidentRecorder incidentRecorder) {

    ScoreboardsReplayerClientBuilder builder = new ScoreboardsReplayerClientBuilder()
    ReplayerServiceClient client = builder
            .setEventId(eventId as Integer)
            .setSport(sport)
            .setTargetEnvironment(targetEnvironment)
            .setInputMode(MANUAL)
            .setTransmitMode(JMS)
            .build()

    SECONDS.sleep(10)

    def sortedIncidentFileList = matchDirectory.listFiles().findAll { it.isFile() }.sort { it.name }
    sortedIncidentFileList.each { File incidentFile ->
//        logger.info("===========================================================================================")
//        logger.info("Replaying incident - ${incidentFile.name}")

        incidentRecorder.setNewFile(incidentFile.getName())
        client.replayFile(incidentFile)
        SECONDS.sleep(1)

        incidentRecorder.push()
//        def dataTabelString = incidentRecorder.toString()
//        logger.info("\n" + dataTabelString + "\n")
    }

    SECONDS.sleep(20)
}
