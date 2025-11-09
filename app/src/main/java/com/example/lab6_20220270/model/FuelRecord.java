package com.example.lab6_20220270.model;

import com.google.firebase.firestore.Exclude;

public class FuelRecord {
    @Exclude
    private String documentId;
    private String recordId;
    private String vehicleId;
    private long date;
    private double liters;
    private long odometer;
    private double totalPrice;
    private String fuelType;
    private long createdAt;

    public FuelRecord() {
    }

    public FuelRecord(String recordId, String vehicleId, long date, double liters, long odometer, double totalPrice, String fuelType) {
        this.recordId = recordId;
        this.vehicleId = vehicleId;
        this.date = date;
        this.liters = liters;
        this.odometer = odometer;
        this.totalPrice = totalPrice;
        this.fuelType = fuelType;
        this.createdAt = System.currentTimeMillis();
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    @Exclude
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public long getOdometer() {
        return odometer;
    }

    public void setOdometer(long odometer) {
        this.odometer = odometer;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
