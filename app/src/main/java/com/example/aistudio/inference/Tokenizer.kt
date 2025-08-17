package com.example.aistudio.inference

import com.google.sentencepiece.SentencePieceProcessor

class Tokenizer(private val tokenizerPath: String) {

    private val processor = SentencePieceProcessor()

    val startId: Int
    val endId: Int

    init {
        processor.load(tokenizerPath)
        startId = processor.bosId()
        endId = processor.eosId()
    }

    fun encode(text: String): List<Int> {
        val ids = processor.encode(text)
        return listOf(startId) + ids
    }

    fun decode(tokenId: Int): String {
        return processor.decode(intArrayOf(tokenId))
    }
    
    fun decode(tokenIds: List<Int>): String {
        return processor.decode(tokenIds.toIntArray())
    }
}
