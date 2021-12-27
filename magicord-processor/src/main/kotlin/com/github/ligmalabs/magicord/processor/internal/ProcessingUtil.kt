package com.github.ligmalabs.magicord.processor.internal

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass

internal fun <T: Any> KSAnnotated.isAnnotatedWith(annotation: KClass<T>): Boolean =
    annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == annotation.qualifiedName }

internal fun KSClassDeclaration.guessClass(): ClassName =
    ClassName.bestGuess(qualifiedName?.asString() ?: "")

internal fun FileSpec.writeTo(codeGenerator: CodeGenerator, dependencies: Dependencies = Dependencies(false)) {
    val file = codeGenerator.createNewFile(dependencies, packageName, name)
    OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
}