package com.sinaungoding.lemsnordic;

public class SensorData {
    private int co2;
    private float pm1;
    private float pm25;
    private float pm4;
    private float pm10;
    private float temp;
    private int humidity;
    private String timestamp;

    public SensorData(int co2, float pm1, float pm25, float pm4, float pm10, float temp, int humidity, String timestamp) {
        this.co2 = co2;
        this.pm1 = pm1;
        this.pm25 = pm25;
        this.pm4 = pm4;
        this.pm10 = pm10;
        this.temp = temp;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public SensorData() {
    }

    public int getCo2() {
        return co2;
    }

    public void setCo2(int co2) {
        this.co2 = co2;
    }

    public float getPm1() {
        return pm1;
    }

    public void setPm1(float pm1) {
        this.pm1 = pm1;
    }

    public float getPm25() {
        return pm25;
    }

    public void setPm25(float pm25) {
        this.pm25 = pm25;
    }

    public float getPm4() {
        return pm4;
    }

    public void setPm4(float pm4) {
        this.pm4 = pm4;
    }

    public float getPm10() {
        return pm10;
    }

    public void setPm10(float pm10) {
        this.pm10 = pm10;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "co2=" + co2 +
                ", pm1=" + pm1 +
                ", pm25=" + pm25 +
                ", pm4=" + pm4 +
                ", pm10=" + pm10 +
                ", temp=" + temp +
                ", humidity=" + humidity +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
