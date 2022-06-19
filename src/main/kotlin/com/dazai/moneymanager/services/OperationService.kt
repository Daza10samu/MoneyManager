package com.dazai.moneymanager.services

import com.dazai.moneymanager.dao.OperationDao
import com.dazai.moneymanager.models.Operation
import com.dazai.moneymanager.models.OperationsSumByDay
import com.dazai.moneymanager.models.OperationsWithSum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class OperationService {
    companion object {
        fun entriesToOperationsWithSum(entries: List<Operation>): OperationsWithSum =
            OperationsWithSum(entries.sumOf { it.paymentSum }, entries)

        fun entriesToOperationsSumByDay(
            entries: List<Operation>, startSum: Double = 0.0, dropPrevious: Boolean = true
        ): List<OperationsSumByDay> {
            val result = mutableListOf<OperationsSumByDay>()
            var date = entries[0].operationDate.toLocalDate()
            var currSum = startSum

            val addToResult = { newDate: LocalDate ->
                result.add(OperationsSumByDay(date, currSum))
                date = newDate
                if (dropPrevious) currSum = 0.0
            }

            entries.forEach { entry ->
                if (entry.operationDate.toLocalDate() != date) {
                    addToResult(entry.operationDate.toLocalDate())
                }

                currSum += entry.paymentSum
            }
            if (entries.isNotEmpty()) addToResult(entries.last().operationDate.toLocalDate())

            return result.toList()
        }
    }

    @Autowired
    val operationDao: OperationDao? = null

    fun updateFromString(str: String): List<Operation> {
        val entries = str.trim('\n').split("\n").drop(1).map { row ->
                row.split(";").map { it.trim('"').replace(",", ".") }
            }.map { row ->
                val (operationDateStr, operationTimeStr) = row[0].split(" ")
                val operationDate = LocalDateTime.of(
                    operationDateStr.split(".")[2].toInt(),
                    operationDateStr.split(".")[1].toInt(),
                    operationDateStr.split(".")[0].toInt(),
                    operationTimeStr.split(":")[0].toInt(),
                    operationTimeStr.split(":")[1].toInt(),
                    operationTimeStr.split(":")[2].toInt()
                )
                val paymentDate = if (row[1].isNotBlank()) LocalDate.of(
                    row[1].split(".")[2].toInt(), row[1].split(".")[1].toInt(), row[1].split(".")[0].toInt()
                ) else null

                Operation(
                    null,
                    operationDate,
                    paymentDate,
                    row[3],
                    row[2],
                    row[4].toDouble(),
                    row[6].toDouble(),
                    row[5],
                    if (row[8].isNotBlank()) row[8].toInt() else null,
                    row[9],
                    row[10],
                    row[11],
                    row[12].toDouble()
                )
            }

        operationDao!!.batchUpsert(entries)

        return operationDao!!.getAll()
    }

    fun getBalance(date: LocalDateTime = LocalDateTime.now()): OperationsWithSum {
        return entriesToOperationsWithSum(operationDao!!.getOperationsBeforeDate(date))
    }

    fun getSpending(
        startDate: LocalDateTime? = null, endDate: LocalDateTime? = null, categories: List<String> = listOf()
    ): OperationsWithSum {
        val entriesWithSum = entriesToOperationsWithSum(operationDao!!.getOperationsBetweenDates(
            startDate ?: LocalDateTime.now().minusMonths(1),
            endDate ?: (startDate ?: LocalDateTime.now().minusMonths(1)).plusMonths(1),
            categories = categories
        ).filter { it.paymentSum < 0 && it.description != "Перевод между счетами" })
        entriesWithSum.sum *= -1
        return entriesWithSum
    }

    fun getSpendingByDays(
        startDate: LocalDateTime? = null, endDate: LocalDateTime? = null, categories: List<String> = listOf()
    ): List<OperationsSumByDay> {
        return entriesToOperationsSumByDay(operationDao!!.getOperationsBetweenDates(
            startDate ?: LocalDateTime.now().minusMonths(1),
            endDate ?: (startDate ?: LocalDateTime.now().minusMonths(1)).plusMonths(1),
            categories = categories
        ).filter { it.paymentSum < 0 && it.description != "Перевод между счетами" }).map {
                it.sum *= -1
                it
            }
    }

    fun getBalanceByDays(
        startDate: LocalDateTime = LocalDateTime.now().minusMonths(1), endDate: LocalDateTime? = null
    ): List<OperationsSumByDay> {
        val entries = operationDao!!.getOperationsBeforeDate(endDate ?: startDate.plusMonths(1)).reversed()
        return entriesToOperationsSumByDay(entries.filter { it.operationDate >= startDate },
            entries.sumOf { if (it.operationDate >= startDate) 0.0 else it.paymentSum },
            false
        )
    }

    fun getCategories(): List<String> {
        return operationDao!!.getCategories()
    }
}