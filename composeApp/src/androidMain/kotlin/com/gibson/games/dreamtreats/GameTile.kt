package com.gibson.games.dreamtreats

enum class TreatType { CUPCAKE, COOKIE, DONUT, ICECREAM, CANDY }

data class GameTile(
    val id: Int,
    val type: TreatType,
    var isMatched: Boolean = false
)
