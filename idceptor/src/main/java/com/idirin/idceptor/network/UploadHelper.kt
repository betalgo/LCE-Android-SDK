package com.idirin.idceptor.network

import android.app.Application
import android.os.Build
import com.idirin.idceptor.db.DbHelper
import com.idirin.idceptor.models.HttpTransaction
import com.idirin.idceptor.models.isRequest
import com.idirin.idceptor.models.network.*
import com.idirin.idceptor.utils.*
import com.idirin.idceptor.utils.AppUtil.getDeviceId
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.await
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

object UploadHelper: KoinComponent {

    private val app: Application by inject()
    private val api: IdApiInterface by inject()
    private val scope: CoroutineScope by inject()
    private val syncThread: ExecutorCoroutineDispatcher by inject()

    private var isRequesting: AtomicBoolean = AtomicBoolean(true)

    private const val RETRY_DELAY = 60_000L

    fun init() {
        isRequesting.set(true)
        postAppInfo()
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
                requestPackage = RequestPackageModel(
                    id = transaction.transactionId,
                    timeStamp = transaction.requestDate!!,
                    url = transaction.url!!,
                    headers = transaction.getRequestHeaders().map { "${it.name} -  ${it.value}" },
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
                        headers = transaction.getResponseHeaders().map { "${it.name} -  ${it.value}" },
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

            val request = AppInitRequest(
                    operatingSystem = "Android",
                    name = "",
                    environment = "",
                    version = getVersionName(app),
                    buildNumber = getVersionCode(app).toString(),
                    device = DeviceModel(
                            name = Build.MODEL,
                            userFriendlyName = Build.PRODUCT,
                            operatingSystem = "Android",
                            uuid = getDeviceId(),
                            osVersion = Build.VERSION.SDK_INT.toString()
                    )
            )

            api.initApp(request).await()
            upload()
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