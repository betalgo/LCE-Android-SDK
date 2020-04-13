package com.idirin.idceptor.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.idirin.idceptor.models.HttpTransaction

/**
 * Created by
 * idirin on 2020-03-30...
 */

@Dao
interface RestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(httpTransaction: HttpTransaction)

    @Update
    fun updateTransaction(httpTransaction: HttpTransaction)

    @Query("select * from transactions where lastUploadedTime <= lastUpdatedTime")
    fun observePendingTransactions(): LiveData<List<HttpTransaction>>

    @Query("select * from transactions where lastUploadedTime <= lastUpdatedTime")
    fun getPendingTransactions(): List<HttpTransaction>

    @Query("update transactions set lastUploadedTime =:uploadTime where transactionId=:transactionId")
    fun updateUploadTime(transactionId: String, uploadTime: Long)


}