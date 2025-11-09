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

    public void isVehicleIdUniqueGlobally(String vehicleId, String excludeDocumentId, OnCompleteListener<Boolean> listener) {
        db.collectionGroup("vehicles")
                .whereEqualTo("id", vehicleId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean unique = true;
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (excludeDocumentId == null || !doc.getId().equals(excludeDocumentId)) {
                                unique = false;
                                break;
                            }
                        }
                        Task<Boolean> resultTask = com.google.android.gms.tasks.Tasks.forResult(unique);
                        listener.onComplete(resultTask);
                    } else {
                        Log.e(TAG, "Error checking vehicle ID uniqueness", task.getException());
                        Task<Boolean> failedTask = com.google.android.gms.tasks.Tasks.forResult(false);
                        listener.onComplete(failedTask);
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

    /*
    Modelo: Claude Sonnet 4.5 (Integrado en Github Copilot en modo ask para que reciba contexto)
    Prompt: Eres un programador de aplicaciones en Android. Necesito que simplifiques las queries de Firestore
    para evitar crear índices compuestos. En lugar de usar orderBy en Firestore, trae todos los documentos
    con whereEqualTo y ordena en memoria.
    Correcciones: En base al código entregado, tuve que agregar el import de java.util.Calendar en algunos
    archivos donde se usaban comparaciones de fechas.
    */
    public void getLastOdometerForVehicle(String vehicleId, OnCompleteListener<Long> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) {
            Task<Long> failedTask = com.google.android.gms.tasks.Tasks.forResult(0L);
            listener.onComplete(failedTask);
            return;
        }
        ref.whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            FuelRecord latestRecord = null;
                            long latestDate = 0;
                            for (DocumentSnapshot doc : task.getResult()) {
                                FuelRecord record = doc.toObject(FuelRecord.class);
                                if (record != null && record.getDate() > latestDate) {
                                    latestDate = record.getDate();
                                    latestRecord = record;
                                }
                            }
                            if (latestRecord != null) {
                                long odometer = latestRecord.getOdometer();
                                Task<Long> resultTask = com.google.android.gms.tasks.Tasks.forResult(odometer);
                                listener.onComplete(resultTask);
                                return;
                            }
                        }
                        Task<Long> resultTask = com.google.android.gms.tasks.Tasks.forResult(0L);
                        listener.onComplete(resultTask);
                    } else {
                        Task<Long> resultTask = com.google.android.gms.tasks.Tasks.forResult(0L);
                        listener.onComplete(resultTask);
                    }
                });
    }

    public void getPreviousRecordByDate(String vehicleId, long date, String excludeDocId, OnCompleteListener<FuelRecord> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
            return;
        }
        ref.whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FuelRecord previousRecord = null;
                        long closestDate = 0;
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (excludeDocId != null && doc.getId().equals(excludeDocId)) {
                                continue;
                            }
                            FuelRecord record = doc.toObject(FuelRecord.class);
                            if (record != null && record.getDate() < date && record.getDate() > closestDate) {
                                closestDate = record.getDate();
                                previousRecord = record;
                                previousRecord.setDocumentId(doc.getId());
                            }
                        }
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(previousRecord));
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                    }
                });
    }

    public void getNextRecordByDate(String vehicleId, long date, String excludeDocId, OnCompleteListener<FuelRecord> listener) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
            return;
        }
        ref.whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FuelRecord nextRecord = null;
                        long closestDate = Long.MAX_VALUE;
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (excludeDocId != null && doc.getId().equals(excludeDocId)) {
                                continue;
                            }
                            FuelRecord record = doc.toObject(FuelRecord.class);
                            if (record != null && record.getDate() > date && record.getDate() < closestDate) {
                                closestDate = record.getDate();
                                nextRecord = record;
                                nextRecord.setDocumentId(doc.getId());
                            }
                        }
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(nextRecord));
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
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
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<FuelRecord> records = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    FuelRecord r = doc.toObject(FuelRecord.class);
                    if (r != null) {
                        r.setDocumentId(doc.getId());
                        records.add(r);
                    }
                }
                records.sort((r1, r2) -> Long.compare(r2.getDate(), r1.getDate()));
                liveData.setValue(records);
            } else {
                Log.e(TAG, "Error getting records", task.getException());
            }
        });
    }

    public void getRecordsFiltered(String vehicleId, Long startDate, Long endDate, MutableLiveData<List<FuelRecord>> liveData) {
        CollectionReference ref = getRecordsCollection();
        if (ref == null) return;
        Query query = ref;
        if (vehicleId != null && !vehicleId.isEmpty()) {
            query = query.whereEqualTo("vehicleId", vehicleId);
        }
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<FuelRecord> records = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    FuelRecord r = doc.toObject(FuelRecord.class);
                    if (r != null) {
                        boolean includeRecord = true;
                        if (startDate != null && r.getDate() < startDate) {
                            includeRecord = false;
                        }
                        if (endDate != null && r.getDate() > endDate) {
                            includeRecord = false;
                        }
                        if (includeRecord) {
                            r.setDocumentId(doc.getId());
                            records.add(r);
                        }
                    }
                }
                records.sort((r1, r2) -> Long.compare(r2.getDate(), r1.getDate()));
                liveData.setValue(records);
            } else {
                Log.e(TAG, "Error getting filtered records", task.getException());
            }
        });
    }
}
