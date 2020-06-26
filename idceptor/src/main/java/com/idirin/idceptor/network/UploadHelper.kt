package com.idirin.idceptor.network

import com.google.gson.Gson
import com.idirin.idceptor.db.DbHelper
import com.idirin.idceptor.models.HttpTransaction
import com.idirin.idceptor.models.isRequest
import com.idirin.idceptor.models.network.PostApiRequest
import com.idirin.idceptor.models.network.RequestPackageModel
import com.idirin.idceptor.models.network.ResponsePackageModel
import com.idirin.idceptor.utils.AppUtil
import com.idirin.idceptor.utils.DeviceUtil
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.await
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

object UploadHelper: KoinComponent {

    private val api: IdApiInterface by inject()
    private val scope: CoroutineScope by inject()
    private val syncThread: ExecutorCoroutineDispatcher by inject()

    private var isRequesting: AtomicBoolean = AtomicBoolean(true)

    private val gson by lazy { Gson() }

    private const val RETRY_DELAY = 60_000L

    fun init() {
        isRequesting.set(true)
        postDeviceInfo()
        DbHelper.clean()

        scope.launch(Dispatchers.Main) {
            DbHelper.observePendingTransactions().observeForever { transactions ->
                runSync {
                    // Return if there is no item
                    if (transactions.isEmpty()) return@runSync

                    // Return if uploading
                    if (isRequesting.get()) return@runSync

                    upload(transactions)
                }
            }
        }
    }

    private fun upload(transactions: List<HttpTransaction>) {
        if (transactions.isEmpty()) {
            isRequesting.set(false)
            return
        }
        isRequesting.set(true)
        postTransaction(transactions.first())
    }

    private fun upload() {
        val transactions = DbHelper.getPendingTransactions()
        upload(transactions)
    }

    private fun postTransaction(transaction: HttpTransaction) {
        coroutine(async = {
            val uploadTime = System.currentTimeMillis()
            val request = PostApiRequest(
                connectionId = transaction.transactionId,
                applicationId = AppUtil.appId,
                deviceId = DeviceUtil.deviceId,
                requestPackage = RequestPackageModel(
                    id = transaction.transactionId,
                    timeStamp = transaction.requestDate!!,
                    url = transaction.url!!,
                    header = gson.toJson(transaction.getRequestHeaders()),
                    body = transaction.requestBody,
                    methodType = transaction.method!!
                ),
                responsePackage = if (transaction.isRequest) {
                    // There is no response yet
                    null
                } else {
                    ResponsePackageModel(
                        id = transaction.transactionId,
                        timeStamp = transaction.responseDate!!,
                        statusCode = transaction.responseCode!!,
                        header = gson.toJson(transaction.getResponseHeaders()),
                        body = transaction.responseBody
                    )
                }
            )
            api.postApi(request).await()
            runSync {
                if (transaction.isRequest && transaction.error == null) {
                    DbHelper.updateUploadTime(transaction.transactionId, uploadTime)
                } else {
                    DbHelper.delete(transaction)
                }
                upload()
            }
        })
    }

    private fun postAppInfo() {
        coroutine(async = {
            // TODO post AppInfo
            // api.postApi(request).await()
            upload()
        })
    }

    private fun postDeviceInfo() {
        coroutine(async = {
            // TODO post Device Info
            // api.postApi(request).await()
            postAppInfo()
        })
    }

    private fun <T, R> T.runSync(action: T.() -> R): R = runBlocking(syncThread) {
        action()
    }

    private fun <T> T.coroutine(async: suspend T.() -> Unit, onError: (suspend (e: Exception) -> Unit)? = null) = scope.launch(Dispatchers.IO) {
        try {
            async()
        } catch (e: Exception) {
            if (onError == null) {
                onError?.invoke(e)
            } else {
                // Default error behaviour is retry in every 1 min
                e.printStackTrace()
                delay(RETRY_DELAY)
                async()
            }
        }
    }


}