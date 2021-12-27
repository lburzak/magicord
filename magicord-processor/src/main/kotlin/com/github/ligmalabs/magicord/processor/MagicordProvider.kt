package com.github.ligmalabs.magicord.processor

import com.github.ligmalabs.magicord.processor.internal.BotProcessor
import com.github.ligmalabs.magicord.processor.internal.PrefixCommandProcessor
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@AutoService(SymbolProcessorProvider::class)
class MagicordProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MagicordProcessor(
            environment.logger,
            BotProcessor(
                prefixCommandProcessor = PrefixCommandProcessor(),
                environment.codeGenerator
            )
        )
    }
}