// Response.kt
package com.ly.type

data class Response(
    var status: Long = 0,
    var data: Long = 0,
    var data_buf: ByteArray? = null,
    var data_len: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (status != other.status) return false
        if (data != other.data) return false
        if (data_len != other.data_len) return false
        if (data_buf != null) {
            if (other.data_buf == null) return false
            if (!data_buf.contentEquals(other.data_buf)) return false
        } else if (other.data_buf != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + data_len.hashCode()
        result = 31 * result + (data_buf?.contentHashCode() ?: 0)
        return result
    }
}