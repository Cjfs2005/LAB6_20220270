package com.example.lab6_20220270.ui.vehicles;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.viewmodel.VehiclesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class VehiclesFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter adapter;
    private VehiclesViewModel viewModel;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        fabAdd = view.findViewById(R.id.fabAddVehicle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VehicleAdapter(new ArrayList<>(), getContext(), new VehicleAdapter.OnVehicleClickListener() {
            @Override
            public void onVehicleClick(Vehicle vehicle) {
                Intent intent = new Intent(getContext(), VehicleDetailActivity.class);
                intent.putExtra("vehicleDocId", vehicle.getDocumentId());
                intent.putExtra("vehiclePlate", vehicle.getPlate());
                intent.putExtra("vehicleId", vehicle.getId());
                intent.putExtra("vehicleMarcaModelo", vehicle.getMarcaModelo());
                intent.putExtra("vehicleYear", vehicle.getYear());
                intent.putExtra("vehicleLastRevision", vehicle.getLastTechnicalRevision());
                startActivity(intent);
            }

            @Override
            public void onVehicleEdit(Vehicle vehicle) {
                AddEditVehicleDialog dialog = AddEditVehicleDialog.newInstance(vehicle);
                dialog.show(getChildFragmentManager(), "edit_vehicle");
            }

            @Override
            public void onVehicleDelete(Vehicle vehicle) {
                viewModel.deleteVehicle(vehicle.getDocumentId());
                Toast.makeText(getContext(), "Vehículo eliminado", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(VehiclesViewModel.class);
        viewModel.getVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            if (vehicles != null) {
                adapter.updateData(vehicles);
            }
        });
        fabAdd.setOnClickListener(v -> {
            AddEditVehicleDialog dialog = AddEditVehicleDialog.newInstance(null);
            dialog.show(getChildFragmentManager(), "add_vehicle");
        });
        viewModel.loadVehicles();
        return view;
    }
}
