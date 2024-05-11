package idk.bluecross.proxyd.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory


@Configuration
@EnableScheduling
class SchedulingTaskConfig {
    @Bean
    fun proxyProviderServiceScheduler(): TaskScheduler = ThreadPoolTaskScheduler().apply {
        setPoolSize(2)
        setThreadNamePrefix("proxyProviderServiceScheduler - ")
        setBeanName("proxyProviderServiceScheduler")
        initialize()
    }
}