package com.example.aistudio.inference

import android.content.Context
import com.example.aistudio.viewmodels.InferenceSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

enum class Accelerator {
    CPU, GPU, NNAPI
}

class LlmInference(
    context: Context,
    modelPath: String,
    tokenizerPath: String,
    accelerator: Accelerator,
    private val settings: InferenceSettings
) : Closeable {

    private var interpreter: Interpreter? = null
    private lateinit var tokenizer: Tokenizer

    init {
        try {
            val modelByteBuffer = loadModelFile(modelPath)
            val options = Interpreter.Options()
            when (accelerator) {
                Accelerator.NNAPI -> options.addDelegate(NnApiApiDelegate())
                Accelerator.GPU -> options.addDelegate(GpuDelegate())
                else -> options.setNumThreads(4)
            }
            interpreter = Interpreter(modelByteBuffer, options)
            tokenizer = Tokenizer(tokenizerPath)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize LlmInference: ${e.message}")
        }
    }

    private fun loadModelFile(modelPath: String): ByteBuffer {
        val fileInputStream = FileInputStream(modelPath)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
    }

    fun generate(prompt: String): Flow<String> = callbackFlow {
        if (interpreter == null) {
            throw IllegalStateException("Interpreter is not initialized.")
        }

        val inputTokenIds = tokenizer.encode(prompt)
        val inputBuffer = ByteBuffer.allocateDirect(inputTokenIds.size * 4).apply {
            order(ByteOrder.nativeOrder())
            asIntBuffer().put(inputTokenIds.toIntArray())
        }
        
        val inputs = arrayOf<Any>(inputBuffer)
        val outputs = mutableMapOf<Int, Any>()
        var nextTokenId = -1
        val endTokenId = tokenizer.endId
        var generatedTokens = 0

        while (nextTokenId != endTokenId && generatedTokens < settings.maxNewTokens) {
            val outputBuffer = ByteBuffer.allocateDirect(4).apply { order(ByteOrder.nativeOrder()) }
            outputs[0] = outputBuffer

            interpreter!!.runForMultipleInputsOutputs(inputs, outputs)

            outputBuffer.rewind()
            nextTokenId = outputBuffer.asIntBuffer().get()
            generatedTokens++

            if (nextTokenId != endTokenId) {
                val decodedToken = tokenizer.decode(nextTokenId)
                trySend(decodedToken).isSuccess
            }
            
            inputBuffer.rewind()
            inputBuffer.asIntBuffer().put(nextTokenId)
        }

        channel.close()
        awaitClose { /* Cleanup */ }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}
