package com.idirin.idceptor.di

import kotlinx.coroutines.*
import org.koin.core.module.Module
import org.koin.dsl.module

val idConcurrencyModule: Module = module{
    single(definition = { getSyncThread<ExecutorCoroutineDispatcher>() })
    single(definition = { getCoroutineScope<CoroutineScope>() })
}

inline fun <reified T> getSyncThread(): T {
    return newSingleThreadContext("SyncThread") as T
}

inline fun <reified T> getCoroutineScope(): T {
    return GlobalScope as T
}