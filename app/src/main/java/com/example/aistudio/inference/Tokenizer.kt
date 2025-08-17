package com.example.aistudio.inference

import org.tensorflow.lite.support.text.tokenization.SentencePieceTokenizer
import java.io.FileInputStream

/**
 * 使用官方 TensorFlow Lite Support Library 重写的最终版 Tokenizer.
 */
class Tokenizer(private val tokenizerPath: String) {

    private val tokenizer: SentencePieceTokenizer

    val startId: Int
    val endId: Int

    init {
        // Support Library 的 Tokenizer 需要一个 InputStream
        val inputStream = FileInputStream(tokenizerPath)
        tokenizer = SentencePieceTokenizer(inputStream)
        inputStream.close()
        
        startId = tokenizer.bosId
        endId = tokenizer.eosId
    }

    fun encode(text: String): List<Int> {
        // 官方库的 tokenize 方法返回的是 IntArray，我们将其转换为 List<Int>
        return tokenizer.tokenize(text).toList()
    }

    fun decode(tokenId: Int): String {
        return tokenizer.detokenize(intArrayOf(tokenId))
    }
    
    fun decode(tokenIds: List<Int>): String {
        return tokenizer.detokenize(tokenIds.toIntArray())
    }
}