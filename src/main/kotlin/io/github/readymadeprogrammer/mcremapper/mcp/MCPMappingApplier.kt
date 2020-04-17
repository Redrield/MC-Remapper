package io.github.readymadeprogrammer.mcremapper.mcp

import io.github.readymadeprogrammer.mcremapper.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.LongAdder
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

fun applyMCPMapping(
        fieldMappings: Set<Mapping<FieldInfo>>,
        methodMappings: Set<Mapping<MethodInfo>>
) {
    val process = ThreadPoolExecutor(
            app.thread,
            app.thread,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
    )

    val totalSize = LongAdder()
    val processedSize = LongAdder()

    val zipInput = ZipFile(app.output)
    val zipOutput = app.mcpOutput.let {
        if (app.mcpOutput.exists()) app.mcpOutput.delete()
        ZipOutputStream(FileOutputStream(it))
    }

    val resultQueue = LinkedBlockingQueue<Pair<ZipEntry, ByteArray>>()

    println("Read from input file")

    for (entry in zipInput.entries()) {
        if (entry.name.endsWith(".class")) {
            val bytes = zipInput.getInputStream(entry).readBytes()
            totalSize.increment()
            process.execute {
                val reader = ClassReader(bytes)
                val classNode = ClassNode()
                val classRemapper = SimpleClassRemapper(classNode, MCPRemapper(fieldMappings, methodMappings))
                reader.accept(classRemapper, ClassReader.EXPAND_FRAMES)
                val writer = ClassWriter(0)
                classNode.accept(writer)
                resultQueue.add(
                        ZipEntry("${classNode.name.replace('.', '/')}.class")
                                to writer.toByteArray()
                )
                processedSize.increment()
            }
        } else {
            zipOutput.putNextEntry(ZipEntry(entry.name))
            ByteArrayInputStream(zipInput.getInputStream(entry).readBytes()).copyTo(zipOutput)
            zipOutput.closeEntry()
        }
    }
    zipInput.close()

    while (totalSize.sum() != processedSize.sum()) {
        println("Apply mappings... (${processedSize.sum()}/${totalSize.sum()})")
        Thread.sleep(1000)
    }
    println("Apply mappings... (${processedSize.sum()}/${totalSize.sum()})")

    println("Write to output file")
    while (resultQueue.isNotEmpty()) {
        val (entry, bytes) = resultQueue.poll()
        zipOutput.putNextEntry(entry)
        zipOutput.write(bytes)
        zipOutput.closeEntry()
    }

    if(!app.keepSrg) {
        app.output.delete()
    }
    zipOutput.close()
}