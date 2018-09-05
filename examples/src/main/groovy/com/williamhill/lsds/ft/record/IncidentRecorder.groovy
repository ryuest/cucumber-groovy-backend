package com.williamhill.lsds.ft.record

import cucumber.api.DataTable
import org.springframework.stereotype.Component

import static cucumber.api.DataTable.create
import static java.util.Locale.ENGLISH

@Component
class IncidentRecorder {

    private Map<String, List<TopicUpdate>> recordedTopicUpdates = new LinkedHashMap<>()

    private String currentFile
    private List<TopicUpdate> topicUpdates = []

    def setNewFile(String fileName) {
        push()
        currentFile = fileName
    }

    def addTopicUpdate(String topicName, String action, String path, String value) {
        topicUpdates = topicUpdates + new TopicUpdate(file: currentFile, topic: topicName, action: action, path: path, value: value)
    }

    def push() {
        if (currentFile != null) {
            recordedTopicUpdates."$currentFile" = topicUpdates.sort {it.topic}
            currentFile = null
            topicUpdates = []
        }
    }

    List<TopicUpdate> getIncidentsList() {
        return recordedTopicUpdates.collect { key, value -> value }.flatten() as List<TopicUpdate>
    }

    @Override
    String toString() {
        def rows = recordedTopicUpdates.collect { key, value -> value }.flatten()
        DataTable dataTable = create(rows, ENGLISH, ["file", "topic", "action", "path", "value"] as String[])
        dataTable.toString()
    }
}
