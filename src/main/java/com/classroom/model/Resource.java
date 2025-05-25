package com.classroom.model;

public class Resource {
    private int resourceId;
    private String room;
    private String resourceType;
    private int quantity;
    private String status;
    private String lastChecked;

    // Constructor
    public Resource(int resourceId, String room, String resourceType, int quantity, String status, String lastChecked) {
        this.resourceId = resourceId;
        this.room = room;
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.status = status;
        this.lastChecked = lastChecked;
    }

    // Getters and setters
    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }
}