package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.util.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ProxyStatsService {
    private val logger = getLogger()

    enum class Stats {
        PROCESSED, VALID, FOUND
    }

    private val stats = hashMapOf<Stats, Any>(
        Stats.PROCESSED to AtomicLong(0),
        Stats.VALID to AtomicLong(0),
        Stats.FOUND to AtomicLong(0)
    )

    fun incProcessed() {
        (stats[Stats.PROCESSED] as AtomicLong).incrementAndGet()
    }
    fun incFound(){
        (stats[Stats.FOUND] as AtomicLong).incrementAndGet()
    }

    fun incValid() {
        (stats[Stats.VALID] as AtomicLong).incrementAndGet()
    }

    fun setValid(n: Long) {
        stats[Stats.VALID] = AtomicLong(n)
    }

    fun getStats() = stats.clone() as Map<Stats, *>

    @Scheduled(fixedRate = 60000)
    fun printStats() {
        logger.info(stats.toString())
    }
}