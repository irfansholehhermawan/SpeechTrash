package com.d3iftelu.gooddayteam.speechtrash.model;

import com.d3iftelu.gooddayteam.speechtrash.ProcessingHelper;

public class History {

    private String startDate;
    private String finishDate;

    public History() {

    }

    public History(String startDate, String finishDate) {
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public int ConvertToHasil(){
        ProcessingHelper convert = new ProcessingHelper();

        long dateStart = Long.parseLong(getStartDate());
        String timeStart = convert.changeToDate(dateStart);
        String[] arrayStart = timeStart.split(" ");
        String startDate = arrayStart[0];

        long dateFinish = Long.parseLong(getFinishDate());
        String timeFinish = convert.changeToDate(dateFinish);
        String[] arrayFinish = timeFinish.split(" ");
        String finishDate = arrayFinish[0];

        int hasil = ((Integer.parseInt(finishDate)) - (Integer.parseInt(startDate)));

        return hasil;
    }
}
