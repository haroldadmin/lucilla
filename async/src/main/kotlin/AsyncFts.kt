package com.haroldadmin.lucilla.async

import com.haroldadmin.lucilla.core.FtsIndex
//import com.haroldadmin.lucilla.core.Pipeline
import com.haroldadmin.lucilla.core.useFts
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//public class AsyncFtsIndex<DocType: Any>(
//    private val pipeline: Pipeline
//) {
//
//    private val index = useFts<DocType>()
//
//    public suspend fun add(doc: DocType) {
//        val docProperties = withContext(Dispatchers.Default) {
//
//        }
//    }
//}