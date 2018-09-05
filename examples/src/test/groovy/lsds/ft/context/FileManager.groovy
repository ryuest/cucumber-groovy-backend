package lsds.ft.context

import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.HeaderColumnNameMappingStrategy
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.williamhill.lsds.ft.record.TopicUpdate
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
class FileManager {

    private static String TEMP_FOLDER = "tmp"
    private static String EXPECTATIONS_FOLDER = "expectations"
    private static String CSV_FILE_EXTENSION = ".csv"

    def saveExpectationsFileAsCsv(String eventId, List<TopicUpdate> updates) {
        createTempFolder(TEMP_FOLDER)
        def writer = new FileWriter(new StringBuilder(TEMP_FOLDER).append("/")
                .append(createFileName(eventId, CSV_FILE_EXTENSION)).toString())
        def beanToCsv = new StatefulBeanToCsvBuilder(writer).build()
        beanToCsv.write(updates)
        writer.close()
    }

    List<TopicUpdate> loadExpectationsFromCsv(String sport, String matchName) {
        CSVReader reader = new CSVReader(new FileReader(expectationsFileName(sport, matchName)))
        HeaderColumnNameMappingStrategy ms = new HeaderColumnNameMappingStrategy()
        ms.setType(TopicUpdate)
        def csvToBean = new CsvToBean<TopicUpdate>()
        List<TopicUpdate> topics = csvToBean.parse(ms, reader)
        return topics
    }

    private def createFileName = { evId, ext ->
        new StringBuilder(EXPECTATIONS_FOLDER).append("-").append(evId).append(ext).toString()
    }

    private def expectationsFileName = { sport, match ->
        new StringBuilder(EXPECTATIONS_FOLDER).append("/")
                .append(sport).append("/")
                .append(match).append(CSV_FILE_EXTENSION).toString()
    }

    private def createTempFolder = { String s ->
        def dir = new File(s)
        dir.mkdirs()
        return dir
    }
}
