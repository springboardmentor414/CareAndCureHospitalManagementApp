package com.cac.model;

import java.time.LocalDate;

public class SelectSearchDate {

    private LocalDate startDate;
    private LocalDate endDate;

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate starDate) {
        this.startDate = starDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
}
