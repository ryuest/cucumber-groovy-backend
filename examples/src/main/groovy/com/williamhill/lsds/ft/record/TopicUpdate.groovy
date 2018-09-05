package com.williamhill.lsds.ft.record

import com.opencsv.bean.CsvBindByName


class TopicUpdate {

    @CsvBindByName
    String file
    @CsvBindByName
    String topic
    @CsvBindByName
    String action
    @CsvBindByName
    String path
    @CsvBindByName
    String value

    TopicUpdate() {}
    TopicUpdate(file, topic, action, path, value) {
        this.file = file
        this.topic = topic
        this.action = action
        this.path = path
        this.value = value
    }

    @Override
    String toString() {
        return new StringBuilder("TopicUpdate(")
                .append(file).append(",")
                .append(topic).append(",")
                .append(action).append(",")
                .append(path).append(",")
                .append(value).append(")").toString()
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TopicUpdate that = (TopicUpdate) o

        if (action != that.action) return false
        if (file != that.file) return false
        if (path != that.path) return false
        if (topic != that.topic) return false
        if (value != that.value) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = (file != null ? file.hashCode() : 0)
        result = 31 * result + (topic != null ? topic.hashCode() : 0)
        result = 31 * result + (action != null ? action.hashCode() : 0)
        result = 31 * result + (path != null ? path.hashCode() : 0)
        result = 31 * result + (value != null ? value.hashCode() : 0)
        return result
    }
}
