package com.eduardo.alrededor.domain.model

enum class RelativeDirection {
    FRONT, RIGHT, BEHIND, LEFT;

    companion object {
        /** El ángulo es relativo a hacia dónde mira el usuario (0 = al frente). */
        fun fromAngle(angle: Double): RelativeDirection = when {
            angle < 45.0 || angle >= 315.0 -> FRONT
            angle < 135.0 -> RIGHT
            angle < 225.0 -> BEHIND
            else -> LEFT
        }
    }
}
