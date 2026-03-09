package com.aashu.privatesuite.util

import java.security.SecureRandom
import java.util.Date

/**
 * Utility to generate MongoDB-compatible ObjectIds.
 * Format: 4-byte timestamp + 5-byte random value + 3-byte counter.
 * Total: 12 bytes, represented as a 24-character hex string.
 */
object ObjectId {
    private val random = SecureRandom()
    private var counter = random.nextInt(16777216) // 3-byte counter start

    @Synchronized
    fun generate(): String {
        val timestamp = (Date().time / 1000).toInt()
        val randomValue = random.nextInt(1048576) // 5 bytes is hard to just int, we approximate with 5 random bytes
        // Actually, let's just generate a compliant 24-char hex string.
        // Timestamp (8 chars) + Random (16 chars) is often enough for uniqueness if not strictly ObjectId spec.
        // But let's try to be close to spec: 4 byte timestamp, 5 byte random, 3 byte counter.
        
        val buffer = StringBuilder()
        
        // 1. Timestamp (4 bytes -> 8 hex chars)
        buffer.append(String.format("%08x", timestamp))
        
        // 2. Random (5 bytes -> 10 hex chars)
        // We can just use random bytes
        val randomBytes = ByteArray(5)
        random.nextBytes(randomBytes)
        for (b in randomBytes) {
            buffer.append(String.format("%02x", b))
        }
        
        // 3. Counter (3 bytes -> 6 hex chars)
        counter = (counter + 1) % 16777216
        buffer.append(String.format("%06x", counter))
        
        return buffer.toString()
    }
}
