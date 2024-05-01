package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.util.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Service
class ProxyStatsService {
    private val logger = getLogger()

    enum class Stats {
        TOTAL, VALID
    }

    private val stats = hashMapOf<Stats, Any>(
        Stats.TOTAL to AtomicLong(0),
        Stats.VALID to AtomicLong(0)
    )

    fun incTotal() {
        (stats[Stats.TOTAL] as AtomicLong).incrementAndGet()
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