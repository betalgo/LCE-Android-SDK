package com.idirin.idceptor.db

import androidx.lifecycle.LiveData
import com.idirin.idceptor.models.HttpTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

object DbHelper: KoinComponent {

    private val restDao: RestDao by inject()
    private val scope: CoroutineScope by inject()
    private val syncThread: ExecutorCoroutineDispatcher by inject()

    fun generateGUID(): String {
        return UUID.randomUUID().toString()
    }

    fun create(transaction: HttpTransaction) {
        val date = System.currentTimeMillis()
        transaction.lastUpdatedTime = date
        restDao.insertTransaction(transaction)
    }

    fun update(transaction: HttpTransaction) {
        val date = System.currentTimeMillis()
        transaction.lastUpdatedTime = date
        restDao.updateTransaction(transaction)
    }

    fun observePendingTransactions(): LiveData<List<HttpTransaction>> {
        return restDao.observePendingTransactions()
    }

    fun getPendingTransactions(): List<HttpTransaction> {
        return restDao.getPendingTransactions()
    }

    fun updateUploadTime(transactionId: String, uploadTime: Long) {
        restDao.updateUploadTime(transactionId, uploadTime)
    }

}