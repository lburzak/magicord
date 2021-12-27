package com.github.ligmalabs.magicord.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration

interface ClassProcessor {
    fun process(classDeclaration: KSClassDeclaration)
}