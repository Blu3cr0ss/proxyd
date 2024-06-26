package idk.bluecross.proxyd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
class ProxydApplication

fun main(args: Array<String>) {
	runApplication<ProxydApplication>(*args)
}
