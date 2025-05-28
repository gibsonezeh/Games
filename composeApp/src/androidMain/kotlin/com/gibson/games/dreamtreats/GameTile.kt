package com.gibson.games.dreamtreats

enum class TreatType { CUPCAKE, COOKIE, DONUT, ICECREAM, CANDY, CAKE, CHOCOLATE, SHAVED_ICE, LOLLIPOP, PIE }

data class GameTile(
    val id: Int,
    val type: TreatType,
    var isMatched: Boolean = false
)
