package com.thoughtworks.data.authmodeldemo.spike.model

data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    constructor() : this(0f, 0f, 0f)
    constructor(value: FloatArray) : this(value[0], value[1], value[2])

    operator fun plus(value: Vector3): Vector3 {
        return Vector3(x + value.x, y + value.y, z + value.z)
    }

    operator fun div(value: Long): Vector3 {
        return Vector3(x / value, y / value, z / value)
    }

    override fun toString(): String {
        return "{x: $x, y: $y, z: $z}"
    }
}