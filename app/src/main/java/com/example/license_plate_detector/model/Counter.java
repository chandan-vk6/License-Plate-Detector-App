package com.example.license_plate_detector.model;

public class Counter {
    private Long value;

    public Counter() {
        // Default constructor required for Firestore
    }

    public Counter(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
