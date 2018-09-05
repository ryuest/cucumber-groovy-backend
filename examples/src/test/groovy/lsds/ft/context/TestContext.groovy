package lsds.ft.context

import com.williamhill.lsds.ft.diffusion.DiffusionClient

import com.williamhill.lsds.ft.record.IncidentRecorder
import com.williamhill.scoreboards.replayer.properties.Sport
import com.williamhill.scoreboards.replayer.properties.TargetEnvironment
import cucumber.api.Scenario
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

class TestContext {

    String eventId
    Sport sport
    String matchName
    File matchFolder
    Scenario scenario

    final AnnotationConfigApplicationContext context

    TestContext() {
        context = new AnnotationConfigApplicationContext()
        context.register(Config)
        context.refresh()
    }

    TargetEnvironment getTargetEnvironment() {
        context.getBean(TargetEnvironment)
    }

    DiffusionClient diffusionClient() {
        context.getBean(DiffusionClient)
    }

    IncidentRecorder incidentRecorder() {
        context.getBean(IncidentRecorder)
    }

    FileManager fileManager() {
        context.getBean(FileManager)
        println ("HERE")
    }

    def log(String logMessage) {
        if (scenario != null)
            scenario.write logMessage + '\n'
    }

}
