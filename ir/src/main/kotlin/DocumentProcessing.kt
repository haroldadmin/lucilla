package com.haroldadmin.lucilla.ir

import com.haroldadmin.lucilla.annotations.Id
import com.haroldadmin.lucilla.annotations.Ignore
import com.haroldadmin.lucilla.pipeline.Pipeline
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

private val IntType = typeOf<Int>()
private val StringType = typeOf<String>()

/**
 * Extracts the value of the [Id] annotated member from a document
 *
 * - A member can be a property or a method.
 * - The document must contain one and only one [Id] annotated member
 * - The annotated member must return a non-null [Int] value
 *
 * @param T The document type
 * @param doc The document to extract the ID of
 * @return Value of the ID annotated property
 */
public fun <T : Any> extractDocumentId(doc: T): Int {
    val docName = doc::class.simpleName ?: "Document"
    val idProps = doc::class.declaredMembers.filter { member -> member.hasAnnotation<Id>() }

    require(idProps.isNotEmpty()) {
        "$docName does not contain any property/method annotated with @Id"
    }

    require(idProps.size == 1) {
        "$docName has multiple @Id annotated properties/methods"
    }

    val (idProp) = idProps
    require(idProp.returnType.isSubtypeOf(IntType)) {
        "$docName's @Id annotated property/method does not return an Int"
    }

    val id = idProp.call(doc) as? Int
    requireNotNull(id) {
        "$docName's @Id annotated property/method returned a null value"
    }

    return id
}

/**
 * Extracts the values of properties that return a [String] from the given document.
 *
 * - Only looks at properties in the class, not methods
 * - Ignores properties annotated with [Id] or [Ignore]
 * - Ignores properties that do not return a [String]
 * - Ignores properties that return a null value
 *
 * @param T The document type
 * @param doc The document to extract properties from
 * @return Values of extracted properties from the document
 */
public fun <T : Any> extractProperties(doc: T): List<String> {
    return doc::class.declaredMemberProperties
        .filterNot { prop -> prop.hasAnnotation<Id>() || prop.hasAnnotation<Ignore>() }
        .filter { prop -> prop.returnType.isSubtypeOf(StringType) }
        .mapNotNull { prop -> prop.call(doc) as? String }
}

/**
 * Extracts tokens that can be indexed from the given data
 *
 * @param data The data to extract tokens from
 * @param pipeline The text processing pipeline to run this data through
 * @return Tokens extracted from the given data
 */
public fun extractTokens(data: String, pipeline: Pipeline): List<String> {
    return pipeline.process(data)
}
