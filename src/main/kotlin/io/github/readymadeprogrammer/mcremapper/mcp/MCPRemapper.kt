package io.github.readymadeprogrammer.mcremapper.mcp

import io.github.readymadeprogrammer.mcremapper.FieldInfo
import io.github.readymadeprogrammer.mcremapper.Mapping
import io.github.readymadeprogrammer.mcremapper.MethodInfo
import org.objectweb.asm.commons.Remapper

class MCPRemapper(
        val fieldMappings: Set<Mapping<FieldInfo>>,
        val methodMappings: Set<Mapping<MethodInfo>>
) : Remapper() {
    override fun mapFieldName(owner: String, name: String, desc: String): String {
        return fieldMappings.find { it.from.name == name }?.mapped ?: name
    }

    override fun mapMethodName(owner: String, name: String, desc: String): String {
        return methodMappings.find { it.from.name == name }?.mapped ?: name
    }
}