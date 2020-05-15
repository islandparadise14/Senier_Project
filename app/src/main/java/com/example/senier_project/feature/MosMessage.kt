package com.example.senier_project.feature

enum class MosMessage(val text: String) {
    MOS_SHORT("MosShort"), MOS_LONG("MosLong"), BLANK("Blank");

    companion object {
        fun stringToMos(string: String?): MosMessage = when (string) {
            "MosShort" -> MOS_SHORT
            "MosLong" -> MOS_LONG
            else -> BLANK
        }
    }
}