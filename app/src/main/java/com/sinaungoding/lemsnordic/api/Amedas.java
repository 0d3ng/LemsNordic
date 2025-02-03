package com.sinaungoding.lemsnordic.api;

public class Amedas {
    private String timestamp;
    private float temperature;
    private String wind_direction;
    private float wind_speed;
    private float humidity;
    private float pressure;
    private float sea_level_pressure;
    private float horizontal_visibility;

    public Amedas() {
    }

    public Amedas(String timestamp, float temperature, String wind_direction, float wind_speed, float humidity, float pressure, float sea_level_pressure, float horizontal_visibility) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.wind_direction = wind_direction;
        this.wind_speed = wind_speed;
        this.humidity = humidity;
        this.pressure = pressure;
        this.sea_level_pressure = sea_level_pressure;
        this.horizontal_visibility = horizontal_visibility;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getWind_direction() {
        return wind_direction;
    }

    public void setWind_direction(String wind_direction) {
        this.wind_direction = wind_direction;
    }

    public float getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(float wind_speed) {
        this.wind_speed = wind_speed;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getSea_level_pressure() {
        return sea_level_pressure;
    }

    public void setSea_level_pressure(float sea_level_pressure) {
        this.sea_level_pressure = sea_level_pressure;
    }

    public float getHorizontal_visibility() {
        return horizontal_visibility;
    }

    public void setHorizontal_visibility(float horizontal_visibility) {
        this.horizontal_visibility = horizontal_visibility;
    }
}
