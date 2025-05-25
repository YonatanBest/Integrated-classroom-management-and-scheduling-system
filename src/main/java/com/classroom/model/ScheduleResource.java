package com.classroom.model;

public class ScheduleResource {
    private int scheduleId;
    private int resourceId;
    private int quantityNeeded;

    public ScheduleResource(int scheduleId, int resourceId, int quantityNeeded) {
        this.scheduleId = scheduleId;
        this.resourceId = resourceId;
        this.quantityNeeded = quantityNeeded;
    }

    // Getters and setters
    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getQuantityNeeded() {
        return quantityNeeded;
    }

    public void setQuantityNeeded(int quantityNeeded) {
        this.quantityNeeded = quantityNeeded;
    }
}