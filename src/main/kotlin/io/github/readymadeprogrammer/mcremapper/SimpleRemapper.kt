package io.github.readymadeprogrammer.mcremapper

import org.objectweb.asm.commons.Remapper

class SimpleRemapper(
    val mapping: Set<ClassMapping>,
    val hierarchy: TypeHierarchyResolveVisitor
) : Remapper() {
    override fun map(typeName: String): String {
        return mapping.find { it.classMapping.from.value == typeName }?.classMapping?.mapped
            ?: typeName
    }

    override fun mapFieldName(owner: String, name: String, desc: String): String {
        val clazz = mapping.find { it.classMapping.from.value == owner } ?: return name
        return clazz.fieldMappings.find { it.from.name == name }?.mapped ?: name
    }

    override fun mapMethodName(owner: String, name: String, desc: String): String {
        val clazz = mapping.find { it.classMapping.from.value == owner } ?: return name
        return clazz.methodMappings.find { it.from.name == name && it.from.toMethodDescriptor() == desc }?.mapped ?: name
    }
}