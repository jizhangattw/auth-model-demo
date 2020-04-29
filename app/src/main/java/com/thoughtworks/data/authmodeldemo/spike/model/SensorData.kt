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

data class SensorData(
    val timestamp: Long,
    val accelerometer: Vector3,
    val gravity: Vector3
) {
    constructor() : this(0, Vector3(0f, 0f, 0f), Vector3(0f, 0f, 0f))

    operator fun plus(value: SensorData): SensorData {
        return SensorData(
            value.timestamp,
            accelerometer + value.accelerometer,
            gravity + value.gravity
        )
    }

    operator fun div(value: Long): SensorData {
        return SensorData(timestamp, accelerometer / value, gravity / value)
    }

    override fun toString(): String {
        return "{timestamp: $timestamp, accelerometer: $accelerometer, gravity: $gravity}"
    }
}