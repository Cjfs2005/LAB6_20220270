package com.example.lab6_20220270.ui.records;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.util.DateUtils;
import com.example.lab6_20220270.viewmodel.RecordsViewModel;
import com.example.lab6_20220270.viewmodel.VehiclesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecordAdapter adapter;
    private RecordsViewModel recordsViewModel;
    private VehiclesViewModel vehiclesViewModel;
    private FloatingActionButton fabAdd;
    private Spinner spinnerVehicle;
    private EditText etStartDate, etEndDate;
    private Button btnFilter;
    private List<Vehicle> vehiclesList = new ArrayList<>();
    private String selectedVehicleId = null;
    private Long startDate = null, endDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewRecords);
        fabAdd = view.findViewById(R.id.fabAddRecord);
        spinnerVehicle = view.findViewById(R.id.spinnerVehicleFilter);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnFilter = view.findViewById(R.id.btnFilter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordAdapter(new ArrayList<>(), getContext(), new RecordAdapter.OnRecordClickListener() {
            @Override
            public void onRecordEdit(FuelRecord record) {
                AddEditRecordDialog dialog = AddEditRecordDialog.newInstance(record, vehiclesList);
                dialog.show(getChildFragmentManager(), "edit_record");
            }

            @Override
            public void onRecordDelete(FuelRecord record) {
                recordsViewModel.deleteRecord(record.getDocumentId());
            }
        });
        recyclerView.setAdapter(adapter);
        recordsViewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        vehiclesViewModel = new ViewModelProvider(this).get(VehiclesViewModel.class);
        recordsViewModel.getRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                adapter.updateData(records);
            }
        });
        vehiclesViewModel.getVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            if (vehicles != null) {
                vehiclesList = vehicles;
                setupSpinner(vehicles);
            }
        });
        etStartDate.setOnClickListener(v -> showDatePickerForStart());
        etEndDate.setOnClickListener(v -> showDatePickerForEnd());
        btnFilter.setOnClickListener(v -> applyFilter());
        fabAdd.setOnClickListener(v -> {
            AddEditRecordDialog dialog = AddEditRecordDialog.newInstance(null, vehiclesList);
            dialog.show(getChildFragmentManager(), "add_record");
        });
        vehiclesViewModel.loadVehicles();
        recordsViewModel.loadRecords();
        return view;
    }

    private void setupSpinner(List<Vehicle> vehicles) {
        List<String> vehicleNames = new ArrayList<>();
        vehicleNames.add("Todos");
        for (Vehicle v : vehicles) {
            vehicleNames.add(v.getId() + " - " + v.getPlate());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, vehicleNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicle.setAdapter(spinnerAdapter);
        spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedVehicleId = null;
                } else {
                    selectedVehicleId = vehiclesList.get(position - 1).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedVehicleId = null;
            }
        });
    }

    private void showDatePickerForStart() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    startDate = selected.getTimeInMillis();
                    etStartDate.setText(DateUtils.formatDate(startDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showDatePickerForEnd() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 23, 59, 59);
                    endDate = selected.getTimeInMillis();
                    etEndDate.setText(DateUtils.formatDate(endDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void applyFilter() {
        recordsViewModel.loadRecordsFiltered(selectedVehicleId, startDate, endDate);
    }
}
