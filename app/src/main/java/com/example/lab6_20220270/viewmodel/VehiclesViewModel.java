package com.example.lab6_20220270.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.repository.FirestoreRepository;
import java.util.List;

public class VehiclesViewModel extends ViewModel {

    private FirestoreRepository repository;
    private MutableLiveData<List<Vehicle>> vehicles;

    public VehiclesViewModel() {
        repository = new FirestoreRepository();
        vehicles = new MutableLiveData<>();
    }

    public MutableLiveData<List<Vehicle>> getVehicles() {
        return vehicles;
    }

    public void loadVehicles() {
        repository.getVehicles(vehicles);
    }

    public void addVehicle(Vehicle vehicle) {
        repository.addVehicle(vehicle, task -> {
            if (task.isSuccessful()) {
                loadVehicles();
            }
        });
    }

    public void updateVehicle(Vehicle vehicle) {
        repository.updateVehicle(vehicle.getDocumentId(), vehicle, task -> {
            if (task.isSuccessful()) {
                loadVehicles();
            }
        });
    }

    public void deleteVehicle(String documentId) {
        repository.deleteVehicle(documentId, task -> {
            if (task.isSuccessful()) {
                loadVehicles();
            }
        });
    }
}
