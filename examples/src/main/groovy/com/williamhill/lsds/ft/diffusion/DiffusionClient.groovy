package com.williamhill.lsds.ft.diffusion

import com.pushtechnology.diffusion.client.Diffusion
import com.pushtechnology.diffusion.client.callbacks.ErrorReason
import com.pushtechnology.diffusion.client.features.Topics
import com.pushtechnology.diffusion.client.session.Session
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification
import com.pushtechnology.diffusion.datatype.json.JSON
import com.pushtechnology.diffusion.datatype.json.JSONDelta
import com.williamhill.lsds.ft.record.IncidentRecorder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Slf4j
class DiffusionClient implements Topics.ValueStream<JSON> {

    private Session session
    private Topics topics
    private Integer counter = 0

    @Value('${diffusion.connection.string}')
    private String diffusionUrl

    private List<String> subscribedTopics = []

    private final IncidentRecorder incidentRecorder

    DiffusionClient(@Qualifier("incidentRecorder") final IncidentRecorder incidentRecorder) {
        this.incidentRecorder = incidentRecorder
    }

    @PostConstruct
    void connect() {
        session = Diffusion.sessions()
                .inputBufferSize(200000)
                .outputBufferSize(200000)
                .maximumMessageSize(200000)
                .principal("admin")
                .password("password")
                .open(diffusionUrl);
        log.info("Connected to '{}'", diffusionUrl);
        topics = session.feature(Topics);
    }

    boolean isSubscribed(String topic) {
        subscribedTopics.contains(topic)
    }

    def subscribe(String... topicsToSubscribe) {
        log.info("Subscribing for '$topicsToSubscribe'")
        def topicsSelector = Diffusion.topicSelectors().anyOf(topicsToSubscribe)
        topics.addStream(topicsSelector, JSON, this)
        topics.subscribe(topicsSelector, new Topics.CompletionCallback.Default())
    }

    def unsubscribe(String... topicsToUnSubscribe) {
        log.info("Unsubscribing topics - '$topicsToUnSubscribe'")
        def topicsSelector = Diffusion.topicSelectors().anyOf(topicsToUnSubscribe)
        topics.addStream(topicsSelector, JSON, this)
        topics.unsubscribe(topicsSelector, new Topics.CompletionCallback.Default())
    }

    void onValue(String topic, TopicSpecification topicSpecification, JSON oldJson, JSON newJson) {
        if (oldJson == null) {
//            log.info("------------------------------------ FULL INSERT - $topic -----------------------------------")
//            log.info(newJson.toJsonString())
            incidentRecorder.addTopicUpdate(getTopicName(topic), "insert", "full json", newJson.toJsonString())
        } else {
            JSONDelta diff = newJson.diff(oldJson)

            def inserted = diff.inserted()
            def removed = diff.removed()

            inserted.findAll { k, v -> k != '/clock/currentTime' }.each { path, value ->
                def action = removed.containsKey(path) ? "replace" : "insert"
                incidentRecorder.addTopicUpdate(getTopicName(topic), action, path, value.toJsonString())
            }

            removed.findAll { k, v -> k != '/clock/currentTime' }.each { path, value ->
                if (!inserted.containsKey(path)) {
                    incidentRecorder.addTopicUpdate(getTopicName(topic), "remove", path, value.toJsonString())
                }
            }
        }
        log.info("Update number ${++counter}: ${newJson.toJsonString()}")
    }

    private String getTopicName(String topic) {
        def topicName = topic.split("/").last()
        if (topicName.startsWith("OB_EV")) "event" else topicName
    }

    void onSubscription(String topic, TopicSpecification topicSpecification) {
        log.info("Topic subscribed '$topic'")
        subscribedTopics.add(topic)
    }

    void onUnsubscription(String topic, TopicSpecification topicSpecification, Topics.UnsubscribeReason unsubscribeReason) {
        log.info("Unsubscribing topic '$topic' because of '${unsubscribeReason.toString()}'")
        subscribedTopics.remove(topic)
    }

    void onClose() {

    }

    void onError(ErrorReason errorReason) {

    }

    @PreDestroy
    def disconnect() {
        try {
            session.close()
        } catch (Exception e) {

        }
    }
}


