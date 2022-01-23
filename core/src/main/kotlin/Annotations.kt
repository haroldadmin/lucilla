package com.haroldadmin.lucilla.core

/**
 * Marks a field of a document to be excluded from the FTS index
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
public annotation class Ignore

/**
 * Marks the ID field of a document
 *
 * The ID field must be a property/method that returns an [Int]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FUNCTION)
public annotation class Id