package com.dazai.moneymanager.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Operation(
    val id: Long?,
    val operationDate: LocalDateTime,
    val paymentDate: LocalDate?,
    val state: String,
    val cardNumber: String,
    val operationSum: Double,
    val paymentSum: Double,
    val currency: String,
    val cachback: Int?,
    val category: String,
    val mcc: String,
    val description: String,
    val bonus: Double
)