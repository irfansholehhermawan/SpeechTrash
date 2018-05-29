package com.d3iftelu.gooddayteam.speechtrash.model;

public class Prediction {
    private String startDate;
    private String finishDate;
    private int prediksi;
    private String idPrediction;

    public Prediction() {
    }

    public Prediction(String startDate, String finishDate) {
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public Prediction(Prediction prediction, String idPrediction) {
        this.startDate = prediction.getStartDate();
        this.finishDate = prediction.getFinishDate();
        this.prediksi = prediction.getPrediksi();
        this.idPrediction = idPrediction;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public int getPrediksi() {
        return prediksi;
    }

    public String getIdPrediction() {
        return idPrediction;
    }
}
