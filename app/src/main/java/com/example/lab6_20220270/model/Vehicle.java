package com.example.lab6_20220270.model;

public class Vehicle {
    private String documentId;
    private String id;
    private String plate;
    private String marcaModelo;
    private int year;
    private long lastTechnicalRevision;
    private long createdAt;
    private long updatedAt;

    public Vehicle() {
    }

    public Vehicle(String id, String plate, String marcaModelo, int year, long lastTechnicalRevision) {
        this.id = id;
        this.plate = plate;
        this.marcaModelo = marcaModelo;
        this.year = year;
        this.lastTechnicalRevision = lastTechnicalRevision;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public void setMarcaModelo(String marcaModelo) {
        this.marcaModelo = marcaModelo;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getLastTechnicalRevision() {
        return lastTechnicalRevision;
    }

    public void setLastTechnicalRevision(long lastTechnicalRevision) {
        this.lastTechnicalRevision = lastTechnicalRevision;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
