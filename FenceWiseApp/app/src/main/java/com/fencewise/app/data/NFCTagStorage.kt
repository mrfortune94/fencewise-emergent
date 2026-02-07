package com.fencewise.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NFCTagStorage(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("nfc_tags", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTags(tags: List<NFCTag>) {
        val json = gson.toJson(tags)
        sharedPreferences.edit().putString("tags", json).apply()
    }

    fun loadTags(): List<NFCTag> {
        val json = sharedPreferences.getString("tags", null) ?: return emptyList()
        val type = object : TypeToken<List<NFCTag>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveTag(tag: NFCTag) {
        val tags = loadTags().toMutableList()
        tags.add(tag)
        saveTags(tags)
    }

    fun deleteTag(tagId: String) {
        val tags = loadTags().filter { it.id != tagId }
        saveTags(tags)
    }

    fun updateTagName(tagId: String, newName: String) {
        val tags = loadTags().map { 
            if (it.id == tagId) it.copy(name = newName) else it 
        }
        saveTags(tags)
    }

    fun getActiveTag(): NFCTag? {
        val tagId = sharedPreferences.getString("active_tag_id", null) ?: return null
        return loadTags().find { it.id == tagId }
    }

    fun setActiveTag(tagId: String?) {
        sharedPreferences.edit().putString("active_tag_id", tagId).apply()
    }
}
