package com.example.lab6_20220270.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.repository.FirestoreRepository;
import java.util.List;

public class RecordsViewModel extends ViewModel {

    private FirestoreRepository repository;
    private MutableLiveData<List<FuelRecord>> records;

    public RecordsViewModel() {
        repository = new FirestoreRepository();
        records = new MutableLiveData<>();
    }

    public MutableLiveData<List<FuelRecord>> getRecords() {
        return records;
    }

    public void loadRecords() {
        repository.getRecords(records);
    }

    public void loadRecordsFiltered(String vehicleId, Long startDate, Long endDate) {
        repository.getRecordsFiltered(vehicleId, startDate, endDate, records);
    }

    public void addRecord(FuelRecord record) {
        repository.addRecord(record, task -> {
            if (task.isSuccessful()) {
                loadRecords();
            }
        });
    }

    public void updateRecord(FuelRecord record) {
        repository.updateRecord(record.getDocumentId(), record, task -> {
            if (task.isSuccessful()) {
                loadRecords();
            }
        });
    }

    public void deleteRecord(String documentId) {
        repository.deleteRecord(documentId, task -> {
            if (task.isSuccessful()) {
                loadRecords();
            }
        });
    }
}
