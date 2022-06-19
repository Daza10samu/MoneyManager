package com.dazai.moneymanager.dao

import com.dazai.moneymanager.models.Operation
import com.dazai.moneymanager.tables.Operations.OPERATIONS
import com.dazai.moneymanager.tables.records.OperationsRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OperationDao {
    companion object {
        fun recordToOperation(record: OperationsRecord): Operation {
            return Operation(
                record.id,
                record.operationDate,
                record.paymentDate,
                record.state,
                record.cardNumber,
                record.operationSum,
                record.paymentSum,
                record.currency,
                record.cachback,
                record.category,
                record.mcc,
                record.description,
                record.bonus
            )
        }
    }

    @Autowired
    private var create: DSLContext? = null

    @Transactional
    fun batchUpsert(entries: Collection<Operation>) {
        val queries = entries.map { entry ->
            create!!.insertInto(
                OPERATIONS,
                OPERATIONS.OPERATION_DATE,
                OPERATIONS.PAYMENT_DATE,
                OPERATIONS.STATE,
                OPERATIONS.CARD_NUMBER,
                OPERATIONS.OPERATION_SUM,
                OPERATIONS.PAYMENT_SUM,
                OPERATIONS.CURRENCY,
                OPERATIONS.CACHBACK,
                OPERATIONS.CATEGORY,
                OPERATIONS.MCC,
                OPERATIONS.DESCRIPTION,
                OPERATIONS.BONUS
            )
                .values(
                    entry.operationDate,
                    entry.paymentDate,
                    entry.state,
                    entry.cardNumber,
                    entry.operationSum,
                    entry.paymentSum,
                    entry.currency,
                    entry.cachback,
                    entry.category,
                    entry.mcc,
                    entry.description,
                    entry.bonus
                )
                .onConflict(
                    OPERATIONS.OPERATION_DATE,
                    OPERATIONS.OPERATION_SUM,
                    OPERATIONS.CURRENCY,
                    OPERATIONS.CARD_NUMBER
                )
                .doUpdate()
                .set(OPERATIONS.STATE, entry.state)
                .set(OPERATIONS.CACHBACK, entry.cachback)
                .set(OPERATIONS.CATEGORY, entry.category)
                .set(OPERATIONS.MCC, entry.mcc)
                .set(OPERATIONS.DESCRIPTION, entry.description)
                .set(OPERATIONS.BONUS, entry.bonus)
        }

        create!!.batch(queries).execute()
    }

    private fun selectByConditions(
        conditions: org.jooq.Condition,
        categories: List<String> = listOf()
    ): List<Operation> {
        var localConditions = conditions
        if (categories.isNotEmpty()) {
            localConditions = conditions.and(OPERATIONS.CATEGORY.`in`(categories))
        }
        return create!!.selectFrom(OPERATIONS)
            .where(localConditions)
            .and(OPERATIONS.PAYMENT_DATE.isNotNull)
            .and(OPERATIONS.STATE.eq("OK"))
            .orderBy(OPERATIONS.OPERATION_DATE.desc())
            .map { recordToOperation(it) }
    }

    @Transactional(readOnly = true)
    fun getOperationsBeforeDate(
        date: LocalDateTime,
        categories: List<String> = listOf()
    ) = selectByConditions(OPERATIONS.OPERATION_DATE.lessOrEqual(date), categories = categories)

    @Transactional(readOnly = true)
    fun getOperationsBetweenDates(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        categories: List<String> = listOf()
    ) = selectByConditions(OPERATIONS.OPERATION_DATE.between(startDate, endDate), categories = categories)

    @Transactional(readOnly = true)
    fun getAll(): List<Operation> {
        return create!!.selectFrom(OPERATIONS)
            .orderBy(OPERATIONS.OPERATION_DATE.desc())
            .map { recordToOperation(it) }
    }

    @Transactional(readOnly = true)
    fun getCategories(): List<String> {
        return create!!.selectDistinct(OPERATIONS.CATEGORY)
            .from(OPERATIONS)
            .where(OPERATIONS.CATEGORY.notEqual(""))
            .map { it.value1() }
    }
}