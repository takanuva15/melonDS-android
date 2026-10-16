package me.magnum.melonds.ui.cheats.model

data class OpenScreenEvent(
    val newTitle: String?,
    val targetCheatId: Long? = null,
    val targetCheatCode: String? = null,
)
