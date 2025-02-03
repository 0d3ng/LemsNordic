package com.sinaungoding.lemsnordic.api;

public class CombinedData {
    private String timestamp;
    private Data data;

    public CombinedData() {
    }

    public CombinedData(String timestamp, Data data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    // Getters and setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private SensorData btevs1;
        private Amedas amedas;

        public Data(SensorData btevs1, Amedas amedas) {
            this.btevs1 = btevs1;
            this.amedas = amedas;
        }

        // Getters and setters
        public SensorData getBtevs1() {
            return btevs1;
        }

        public void setBtevs1(SensorData btevs1) {
            this.btevs1 = btevs1;
        }

        public Amedas getAmedas() {
            return amedas;
        }

        public void setAmedas(Amedas amedas) {
            this.amedas = amedas;
        }
    }
}
