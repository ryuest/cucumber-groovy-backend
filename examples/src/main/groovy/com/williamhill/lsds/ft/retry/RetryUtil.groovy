package com.williamhill.lsds.ft.retry

import groovy.util.logging.Slf4j
import java.util.concurrent.TimeUnit

import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
class RetryUtil {

    static void retry(String taskName,
                      long loops = 7,
                      long delayTime = 3,
                      TimeUnit timeUnit = SECONDS,
                      double backoff = 1.5d,
                      boolean initialDelay = true,
                      Closure retryableTask) {

        if (initialDelay) timeUnit.sleep(delayTime)
        try {
            retryableTask.run()
        } catch (Throwable throwable) {
            if (loops > 0){
                long loopsLeft = loops - 1
                long newDelayTime = (long) delayTime * backoff
                log.warn("Failed to perform task $taskName - $loopsLeft attempts left. Retrying in $delayTime ${timeUnit.name()}")
                timeUnit.sleep(delayTime)
                retry(taskName, loopsLeft, newDelayTime, timeUnit, backoff, false, retryableTask)
            } else {
                log.error("Failed to perform task $taskName because: ${throwable.getMessage()}")
                throw throwable
            }
        }
    }
}
