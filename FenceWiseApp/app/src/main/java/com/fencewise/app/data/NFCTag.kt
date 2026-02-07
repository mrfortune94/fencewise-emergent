package com.fencewise.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NFCTag(
    val id: String,
    val name: String,
    val uid: String,
    val technology: String,
    val data: ByteArray,
    val dataHex: String,
    val timestamp: Long
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NFCTag

        if (id != other.id) return false
        if (name != other.name) return false
        if (uid != other.uid) return false
        if (technology != other.technology) return false
        if (!data.contentEquals(other.data)) return false
        if (dataHex != other.dataHex) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + technology.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + dataHex.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
