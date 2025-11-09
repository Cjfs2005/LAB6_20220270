package com.example.lab6_20220270.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.model.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FirestoreRepository {
    private static final String TAG = "FirestoreRepository";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private CollectionReference getVehiclesCollection() {
        String uid = getUserId();
        if (uid == null) return null;
        return db.collection("users").document(uid).collection("vehicles");
    }

    private CollectionReference getRecordsCollection() {
        String uid = getUserId();
        if (uid == null) return null;
        return db.collection("users").document(uid).collection("records");
    }

    public void addVehicle(Vehicle vehicle, OnCompleteListener<Void> listener) {
        CollectionReference ref = getVehiclesCollection();
        if (ref == null) return;
        vehicle.setCreatedAt(System.currentTimeMillis());
        ref.add(vehicle).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Vehicle added");
            } else {
                Log.e(TAG, "Error adding vehicle", task.getException());
            }
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
        });
    }

    public void updateVehicle(String documentId, Vehicle vehicle, OnCompleteListener<Void> listener) {
        CollectionReference ref = getVehiclesCollection();
        if (ref == null) return;
        ref.document(documentId).set(vehicle).addOnCompleteListener(listener);
    }

    public void deleteVehicle(String documentId, OnCompleteListener<Void> listener) {
        CollectionReference ref = getVehiclesCollection();
        if (ref == null) return;
        ref.document(documentId).delete().addOnCompleteListener(listener);
    }

    public void getVehicles(MutableLiveData<List<Vehicle>> liveData) {
        CollectionReference ref = getVehiclesCollection();
        if (ref == null) return;
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Vehicle> vehicles = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Vehicle v = doc.toObject(Vehicle.class);
                    if (v != null) {
                        v.setDocumentId(doc.getId());
                        vehicles.add(v);
                    }
                }
                liveData.setValue(vehicles);
            } else {
                Log.e(TAG, "Error getting vehicles", task.getException());
            }
        });
    }

    public String generateUniqueRecordId() {
        Random random = new Random();
        int id = 10000 + random.nextInt(90000);
        return String.valueOf(id);
    }

    public void isRecordIdUnique(String recordId, OnCompleteListener<Boolean> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) {
            Task<Boolean> failedTask = com.google.android.gms.tasks.Tasks.forResult(false);
            listener.onComplete(failedTask);
            return;
        }
        ref.whereEqualTo("recordId", recordId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean unique = task.getResult().isEmpty();
                Task<Boolean> resultTask = com.google.android.gms.tasks.Tasks.forResult(unique);
                listener.onComplete(resultTask);
            } else {
                Task<Boolean> failedTask = com.google.android.gms.tasks.Tasks.forResult(false);
                listener.onComplete(failedTask);
            }
        });
    }

    public void getLastOdometerForVehicle(String vehicleId, OnCompleteListener<Long> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) {
            Task<Long> failedTask = com.google.android.gms.tasks.Tasks.forResult(0L);
            listener.onComplete(failedTask);
            return;
        }
        ref.whereEqualTo("vehicleId", vehicleId)
                .orderBy("odometer", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        FuelRecord record = doc.toObject(FuelRecord.class);
                        long odometer = record != null ? record.getOdometer() : 0L;
                        Task<Long> resultTask = com.google.android.gms.tasks.Tasks.forResult(odometer);
                        listener.onComplete(resultTask);
                    } else {
                        Task<Long> resultTask = com.google.android.gms.tasks.Tasks.forResult(0L);
                        listener.onComplete(resultTask);
                    }
                });
    }

    public void addRecord(FuelRecord record, OnCompleteListener<Void> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        record.setCreatedAt(System.currentTimeMillis());
        ref.add(record).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Record added");
            } else {
                Log.e(TAG, "Error adding record", task.getException());
            }
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
        });
    }

    public void updateRecord(String documentId, FuelRecord record, OnCompleteListener<Void> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        ref.document(documentId).set(record).addOnCompleteListener(listener);
    }

    public void deleteRecord(String documentId, OnCompleteListener<Void> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        ref.document(documentId).delete().addOnCompleteListener(listener);
    }

    public void getRecords(MutableLiveData<List<FuelRecord>> liveData) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        ref.orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<FuelRecord> records = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    FuelRecord r = doc.toObject(FuelRecord.class);
                    if (r != null) {
                        r.setDocumentId(doc.getId());
                        records.add(r);
                    }
                }
                liveData.setValue(records);
            } else {
                Log.e(TAG, "Error getting records", task.getException());
            }
        });
    }

    public void getRecordsFiltered(String vehicleId, Long startDate, Long endDate, MutableLiveData<List<FuelRecord>> liveData) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        Query query = ref.orderBy("date", Query.Direction.DESCENDING);
        if (vehicleId != null && !vehicleId.isEmpty()) {
            query = query.whereEqualTo("vehicleId", vehicleId);
        }
        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("date", startDate);
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("date", endDate);
        }
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<FuelRecord> records = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    FuelRecord r = doc.toObject(FuelRecord.class);
                    if (r != null) {
                        r.setDocumentId(doc.getId());
                        records.add(r);
                    }
                }
                liveData.setValue(records);
            } else {
                Log.e(TAG, "Error getting filtered records", task.getException());
            }
        });
    }
}
