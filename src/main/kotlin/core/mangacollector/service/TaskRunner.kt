package core.mangacollector.service

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class TaskRunner(val collector: Collector) {
    val logger = LoggerFactory.getLogger(TaskRunner::class.java)

    @Value("\${task.latest.manga.collector.page}")
    private val page: Int = 0

    @PostConstruct
    fun init() {
        logger.info("TaskRunner initialized")
        BCC_FULL()
        BUC_FULL()
        logger.info("TaskRunner initialized finished")

    }

    fun BCC_FULL() {
        logTaskRunner("BCC_FULL") {
            collector.brokenChapterCollector()
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun LMC_FULL() {
        logTaskRunner("LMC_FULL") {
            val initDoc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=1").get()
            val lastPageString = initDoc.select(".page_last").text()
                    .toString()
                    .replace(Regex("[A-z]"), "")
                    .replace(Regex("\\("), "")
                    .replace(Regex("\\)"), "")
            val lastPage = lastPageString.toInt()
            collector.latestMangaCollector(lastPage)
        }
    }

    @Scheduled(cron = "0 */5 * ? * *")
    fun TASK_5_MIN() {
        LMC_5_MIN()
        MDC_FULL()
    }

    fun LMC_5_MIN() {
        logTaskRunner("LMC_5_MIN") {
            collector.latestMangaCollector(page)
        }
    }

    fun MDC_FULL() {
        logTaskRunner("MDC_FULL") {
            collector.mangaDetailsCollector()
        }
    }

    @Scheduled(cron = "0 0 */4 ? * *")
    fun BUC_FULL() {
        logTaskRunner("BUC_FULL") {
            collector.brokenUrlFixer()
        }
    }


    fun logTaskRunner(taskName: String, function: () -> Unit) {
        val start = Date();
        logger.info("Running $taskName at $start")
        function.invoke()
        val end = Date();
        logger.info("Finished running $taskName at $end")
        val diffInMillisec: Long = end.getTime() - start.getTime()
        val diffInMin = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec)
        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec)
        logger.info("Time took to finish $diffInSec seconds or  $diffInMin minutes")
    }
}