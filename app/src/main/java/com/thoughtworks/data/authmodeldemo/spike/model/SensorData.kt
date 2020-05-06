package com.thoughtworks.data.authmodeldemo.spike.model

data class SensorData(
    val timestamp: Long,
    val accelerometer: Vector3,
    val gravity: Vector3,
    val magneticField: Vector3
) {
    constructor() : this(0, Vector3(0f, 0f, 0f), Vector3(0f, 0f, 0f), Vector3(0f, 0f, 0f))

    operator fun plus(value: SensorData): SensorData {
        return SensorData(
            value.timestamp,
            accelerometer + value.accelerometer,
            gravity + value.gravity,
            magneticField = value.magneticField
        )
    }

    operator fun div(value: Long): SensorData {
        return SensorData(timestamp, accelerometer / value, gravity / value, magneticField / value)
    }

    override fun toString(): String {
        return "{timestamp: $timestamp, accelerometer: $accelerometer, gravity: $gravity, magneticField: $magneticField}"
    }
}