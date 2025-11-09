package com.example.lab6_20220270.ui.records;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.repository.FirestoreRepository;
import com.example.lab6_20220270.util.DateUtils;
import com.example.lab6_20220270.viewmodel.RecordsViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditRecordDialog extends DialogFragment {

    private EditText etDate, etLiters, etOdometer, etPrice;
    private Spinner spinnerVehicle, spinnerFuelType;
    private Button btnSave;
    private FuelRecord recordToEdit;
    private List<Vehicle> vehiclesList;
    private RecordsViewModel viewModel;
    private FirestoreRepository repository;
    private long selectedDate = 0;
    private String selectedRecordId = null;

    public static AddEditRecordDialog newInstance(FuelRecord record, List<Vehicle> vehicles) {
        AddEditRecordDialog dialog = new AddEditRecordDialog();
        Bundle args = new Bundle();
        if (record != null) {
            args.putString("documentId", record.getDocumentId());
            args.putString("recordId", record.getRecordId());
            args.putString("vehicleId", record.getVehicleId());
            args.putLong("date", record.getDate());
            args.putDouble("liters", record.getLiters());
            args.putLong("odometer", record.getOdometer());
            args.putDouble("totalPrice", record.getTotalPrice());
            args.putString("fuelType", record.getFuelType());
        }
        args.putSerializable("vehicles", (ArrayList<Vehicle>) vehicles);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_edit_record, null);
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle);
        etDate = view.findViewById(R.id.etRecordDate);
        etLiters = view.findViewById(R.id.etRecordLiters);
        etOdometer = view.findViewById(R.id.etRecordOdometer);
        etPrice = view.findViewById(R.id.etRecordPrice);
        spinnerFuelType = view.findViewById(R.id.spinnerFuelType);
        btnSave = view.findViewById(R.id.btnSaveRecord);
        viewModel = new ViewModelProvider(requireParentFragment()).get(RecordsViewModel.class);
        repository = new FirestoreRepository();
        vehiclesList = (List<Vehicle>) getArguments().getSerializable("vehicles");
        setupVehicleSpinner();
        setupFuelTypeSpinner();
        if (getArguments() != null && getArguments().containsKey("documentId")) {
            recordToEdit = new FuelRecord();
            recordToEdit.setDocumentId(getArguments().getString("documentId"));
            recordToEdit.setRecordId(getArguments().getString("recordId"));
            recordToEdit.setVehicleId(getArguments().getString("vehicleId"));
            recordToEdit.setDate(getArguments().getLong("date"));
            recordToEdit.setLiters(getArguments().getDouble("liters"));
            recordToEdit.setOdometer(getArguments().getLong("odometer"));
            recordToEdit.setTotalPrice(getArguments().getDouble("totalPrice"));
            recordToEdit.setFuelType(getArguments().getString("fuelType"));
            selectedRecordId = recordToEdit.getRecordId();
            selectedDate = recordToEdit.getDate();
            etDate.setText(DateUtils.formatDateTime(recordToEdit.getDate()));
            etLiters.setText(String.valueOf(recordToEdit.getLiters()));
            etOdometer.setText(String.valueOf(recordToEdit.getOdometer()));
            etPrice.setText(String.valueOf(recordToEdit.getTotalPrice()));
            for (int i = 0; i < vehiclesList.size(); i++) {
                if (vehiclesList.get(i).getId().equals(recordToEdit.getVehicleId())) {
                    spinnerVehicle.setSelection(i);
                    break;
                }
            }
            String[] fuelTypes = {"Gasolina", "GLP", "GNV"};
            for (int i = 0; i < fuelTypes.length; i++) {
                if (fuelTypes[i].equals(recordToEdit.getFuelType())) {
                    spinnerFuelType.setSelection(i);
                    break;
                }
            }
        } else {
            selectedDate = System.currentTimeMillis();
            etDate.setText(DateUtils.formatDateTime(selectedDate));
        }
        etDate.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveRecord());
        builder.setView(view)
                .setTitle(recordToEdit != null ? "Editar Registro" : "Agregar Registro")
                .setNegativeButton("Cancelar", (dialog, which) -> dismiss());
        return builder.create();
    }

    private void setupVehicleSpinner() {
        List<String> vehicleNames = new ArrayList<>();
        for (Vehicle v : vehiclesList) {
            vehicleNames.add(v.getId() + " - " + v.getPlate());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, vehicleNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicle.setAdapter(adapter);
    }

    private void setupFuelTypeSpinner() {
        String[] fuelTypes = {"Gasolina", "GLP", "GNV"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fuelTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFuelType.setAdapter(adapter);
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    showTimePicker(selected);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar dateCalendar) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(selectedDate);
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    dateCalendar.set(Calendar.MINUTE, minute);
                    dateCalendar.set(Calendar.SECOND, 0);
                    dateCalendar.set(Calendar.MILLISECOND, 0);
                    selectedDate = dateCalendar.getTimeInMillis();
                    etDate.setText(DateUtils.formatDateTime(selectedDate));
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    /*
    Modelo: Claude Sonnet 4.5 (Integrado en Github Copilot en modo ask para que reciba contexto)
    Prompt: Eres un programador experto en Android. Necesito mejorar la validación de kilometraje para que
    verifique tanto el registro anterior como el posterior según la fecha, no solo el máximo global. Debe
    validar que el kilometraje esté entre el registro inmediatamente anterior y posterior cronológicamente.
    Correcciones: En base al código entregado, tuve que ajustar los mensajes Toast para que mostraran la
    fecha completa con hora usando formatDateTime en lugar de solo formatDate, y también tuve que manejar
    mejor los callbacks anidados para evitar problemas de sincronización.
    */
    private void saveRecord() {
        if (vehiclesList.isEmpty()) {
            Toast.makeText(getContext(), "Debe crear un vehículo primero", Toast.LENGTH_SHORT).show();
            return;
        }
        int vehiclePosition = spinnerVehicle.getSelectedItemPosition();
        String vehicleId = vehiclesList.get(vehiclePosition).getId();
        String litersStr = etLiters.getText().toString().trim();
        String odometerStr = etOdometer.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String fuelType = spinnerFuelType.getSelectedItem().toString();
        if (litersStr.isEmpty() || odometerStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        double liters = Double.parseDouble(litersStr);
        long odometer = Long.parseLong(odometerStr);
        double price = Double.parseDouble(priceStr);
        String excludeDocId = recordToEdit != null ? recordToEdit.getDocumentId() : null;
        repository.getPreviousRecordByDate(vehicleId, selectedDate, excludeDocId, prevTask -> {
            FuelRecord previousRecord = prevTask.isSuccessful() ? prevTask.getResult() : null;
            repository.getNextRecordByDate(vehicleId, selectedDate, excludeDocId, nextTask -> {
                FuelRecord nextRecord = nextTask.isSuccessful() ? nextTask.getResult() : null;
                if (previousRecord != null && odometer <= previousRecord.getOdometer()) {
                    Toast.makeText(getContext(), "El kilometraje debe ser mayor al registro anterior (" + previousRecord.getOdometer() + " km el " + DateUtils.formatDateTime(previousRecord.getDate()) + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                if (nextRecord != null && odometer >= nextRecord.getOdometer()) {
                    Toast.makeText(getContext(), "El kilometraje debe ser menor al registro posterior (" + nextRecord.getOdometer() + " km el " + DateUtils.formatDateTime(nextRecord.getDate()) + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                if (recordToEdit != null) {
                    recordToEdit.setVehicleId(vehicleId);
                    recordToEdit.setDate(selectedDate);
                    recordToEdit.setLiters(liters);
                    recordToEdit.setOdometer(odometer);
                    recordToEdit.setTotalPrice(price);
                    recordToEdit.setFuelType(fuelType);
                    viewModel.updateRecord(recordToEdit);
                    Toast.makeText(getContext(), "Registro actualizado", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    generateAndSaveNewRecord(vehicleId, liters, odometer, price, fuelType);
                }
            });
        });
    }

    private void generateAndSaveNewRecord(String vehicleId, double liters, long odometer, double price, String fuelType) {
        String recordId = repository.generateUniqueRecordId();
        repository.isRecordIdUnique(recordId, task -> {
            if (task.isSuccessful() && task.getResult()) {
                FuelRecord newRecord = new FuelRecord(recordId, vehicleId, selectedDate, liters, odometer, price, fuelType);
                viewModel.addRecord(newRecord);
                Toast.makeText(getContext(), "Registro guardado con ID: " + recordId, Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                generateAndSaveNewRecord(vehicleId, liters, odometer, price, fuelType);
            }
        });
    }
}
