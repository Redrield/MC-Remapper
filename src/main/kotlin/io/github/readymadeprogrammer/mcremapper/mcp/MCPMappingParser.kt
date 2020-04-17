package io.github.readymadeprogrammer.mcremapper.mcp

import io.github.readymadeprogrammer.mcremapper.FieldInfo
import io.github.readymadeprogrammer.mcremapper.Mapping
import io.github.readymadeprogrammer.mcremapper.MethodInfo
import io.github.readymadeprogrammer.mcremapper.TypeDescriptor
import java.io.File

fun parseMCPFieldMappings(file: File): Set<Mapping<FieldInfo>> {
    val contents = file.readLines()

    val mappings = mutableSetOf<Mapping<FieldInfo>>()

    for(line in contents.subList(1, contents.lastIndex)) {
        val csv = line.split(",")
        mappings += Mapping(FieldInfo(TypeDescriptor(""), csv[0]), csv[1])
    }

    return mappings
}

fun parseMCPMethodMappings(file: File): Set<Mapping<MethodInfo>> {
    val contents = file.readLines()

    val mappings = mutableSetOf<Mapping<MethodInfo>>()

    for(line in contents.subList(1, contents.lastIndex)) {
        val csv = line.split(",")
        mappings += Mapping(MethodInfo(TypeDescriptor(""), listOf(), csv[0]), csv[1])
    }

    return mappings
}