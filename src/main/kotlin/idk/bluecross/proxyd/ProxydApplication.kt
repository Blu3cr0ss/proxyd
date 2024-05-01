package idk.bluecross.proxyd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ProxydApplication

fun main(args: Array<String>) {
	runApplication<ProxydApplication>(*args)
}
