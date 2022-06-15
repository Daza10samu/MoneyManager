package com.dazai.moneymanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MoneyManagerApplication

fun main(args: Array<String>) {
    runApplication<MoneyManagerApplication>(*args)
}
