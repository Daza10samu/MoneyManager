package com.dazai.moneymanager.controllers

import com.dazai.moneymanager.models.Operation
import com.dazai.moneymanager.models.OperationsSumByDay
import com.dazai.moneymanager.models.OperationsWithSum
import com.dazai.moneymanager.services.OperationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.Charset
import java.time.LocalDateTime


@Controller
class OperationsController {
    @Autowired
    val service: OperationService? = null

    @RequestMapping(
        value = ["/putTinkData"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"],
        method = [RequestMethod.PUT]
    )
    fun requestPut(
        @RequestPart("file") file: MultipartFile?
    ): ResponseEntity<List<Operation>>? {
        val entries = service!!.updateFromString(file!!.bytes.toString(charset = Charset.forName("cp1251")))
        return ResponseEntity<List<Operation>>(entries, HttpStatus.OK)
    }

    @RequestMapping(
        value = ["/balance"], produces = ["application/json"], method = [RequestMethod.GET]
    )
    fun getBalanceByDate(
        @RequestParam(
            "date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) date: LocalDateTime?,
        @RequestParam("verbose", required = false, defaultValue = "false") verbose: Boolean?,
    ): ResponseEntity<OperationsWithSum>? {
        val operationsWithSum = service!!.getBalance(date ?: LocalDateTime.now())
        if (verbose != true) {
            operationsWithSum.operations = listOf()
        }
        return ResponseEntity<OperationsWithSum>(operationsWithSum, HttpStatus.OK)
    }

    @RequestMapping(
        value = ["/spending"], produces = ["application/json"], method = [RequestMethod.GET]
    )
    fun getSpending(
        @RequestParam(
            "start_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(
            "end_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
        @RequestParam("categories", required = false, defaultValue = "") categories: List<String>?,
        @RequestParam("verbose", required = false, defaultValue = "false") verbose: Boolean?,
    ): ResponseEntity<OperationsWithSum>? {
        val operationsWithSum = service!!.getSpending(startDate, endDate, categories!!)
        if (verbose != true) {
            operationsWithSum.operations = listOf()
        }
        return ResponseEntity<OperationsWithSum>(operationsWithSum, HttpStatus.OK)
    }

    @RequestMapping(
        value = ["/spendingDays"], produces = ["application/json"], method = [RequestMethod.GET]
    )
    fun getSpendingByDays(
        @RequestParam(
            "start_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(
            "end_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
        @RequestParam("categories", required = false, defaultValue = "") categories: List<String>?,
    ): ResponseEntity<List<OperationsSumByDay>>? {
        val spendingByDays = service!!.getSpendingByDays(startDate, endDate, categories!!)
        return ResponseEntity<List<OperationsSumByDay>>(spendingByDays, HttpStatus.OK)
    }

    @RequestMapping(
        value = ["/balanceDays"], produces = ["application/json"], method = [RequestMethod.GET]
    )
    fun getBalanceByDays(
        @RequestParam(
            "start_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(
            "end_date", required = false
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
    ): ResponseEntity<List<OperationsSumByDay>>? {
        val balanceByDays =
            if (startDate == null) service!!.getBalanceByDays() else service!!.getBalanceByDays(startDate, endDate)
        return ResponseEntity<List<OperationsSumByDay>>(balanceByDays, HttpStatus.OK)
    }

    @RequestMapping(
        value = ["/categories"], produces = ["application/json"], method = [RequestMethod.GET]
    )
    fun getCategories(): ResponseEntity<List<String>>? {
        return ResponseEntity<List<String>>(service!!.getCategories(), HttpStatus.OK)
    }
}