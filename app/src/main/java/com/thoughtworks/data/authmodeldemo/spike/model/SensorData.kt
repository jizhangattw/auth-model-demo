package com.thoughtworks.data.authmodeldemo.spike.model

data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    constructor(value: FloatArray) : this(value[0], value[1], value[2])

    override fun toString(): String {
        return "{x: $x, y: $y, z: $z}"
    }
}

data class SensorData(
    val timestamp: Long,
    val accelerometer: Vector3,
    val gravity: Vector3
) {
    override fun toString(): String {
        return "{timestamp: $timestamp, accelerometer: $accelerometer, gravity: $gravity}"
    }
}