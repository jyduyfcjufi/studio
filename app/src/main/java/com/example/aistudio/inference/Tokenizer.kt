package com.example.aistudio.inference

import com.google.sentencepiece.SentencePieceProcessor
import java.io.File

/**
 * 使用官方 SentencePiece JNI 库重写的专业 Tokenizer.
 * 这个实现可以直接加载和处理 .model 文件.
 */
class Tokenizer(private val tokenizerPath: String) {

    private val processor = SentencePieceProcessor()

    // 特殊 token 的 ID 通常需要从 processor 获取
    val startId: Int
    val endId: Int

    init {
        // 从给定的文件路径加载 .model 文件
        processor.load(tokenizerPath)
        
        // 从加载的模型中获取特殊 token 的 ID
        startId = processor.bosId() // BOS = Beginning Of Sentence
        endId = processor.eosId()   // EOS = End Of Sentence
    }

    /**
     * 将输入的字符串编码为 token ID 列表.
     * @param text 用户输入的文本.
     * @return 返回 token ID 组成的列表.
     */
    fun encode(text: String): List<Int> {
        // SentencePiece 库通常会自动处理起始符，但具体行为取决于模型训练方式。
        // 一个常见的做法是手动添加起始符。
        val ids = processor.encode(text)
        return listOf(startId) + ids
    }

    /**
     * 将单个 token ID 解码为字符串.
     * @param tokenId 要解码的 token ID.
     * @return 返回解码后的文本.
     */
    fun decode(tokenId: Int): String {
        return processor.decode(intArrayOf(tokenId))
    }
    
    /**
     * 将 token ID 列表解码为完整的字符串.
     * @param tokenIds 要解码的 token ID 列表.
     * @return 返回解码后的文本.
     */
    fun decode(tokenIds: List<Int>): String {
        return processor.decode(tokenIds.toIntArray())
    }
}
