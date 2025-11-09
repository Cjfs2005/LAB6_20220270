package com.example.lab6_20220270.ui.vehicles;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.repository.FirestoreRepository;
import com.example.lab6_20220270.util.DateUtils;
import com.example.lab6_20220270.viewmodel.VehiclesViewModel;
import java.text.ParseException;
import java.util.Calendar;

public class AddEditVehicleDialog extends DialogFragment {

    private EditText etId, etPlate, etMarcaModelo, etYear, etLastRevision;
    private Button btnSave;
    private Vehicle vehicleToEdit;
    private VehiclesViewModel viewModel;
    private FirestoreRepository repository;
    private long selectedRevisionDate = 0;

    public static AddEditVehicleDialog newInstance(Vehicle vehicle) {
        AddEditVehicleDialog dialog = new AddEditVehicleDialog();
        Bundle args = new Bundle();
        if (vehicle != null) {
            args.putString("documentId", vehicle.getDocumentId());
            args.putString("id", vehicle.getId());
            args.putString("plate", vehicle.getPlate());
            args.putString("marcaModelo", vehicle.getMarcaModelo());
            args.putInt("year", vehicle.getYear());
            args.putLong("lastRevision", vehicle.getLastTechnicalRevision());
        }
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_edit_vehicle, null);
        etId = view.findViewById(R.id.etVehicleId);
        etPlate = view.findViewById(R.id.etVehiclePlate);
        etMarcaModelo = view.findViewById(R.id.etVehicleMarcaModelo);
        etYear = view.findViewById(R.id.etVehicleYear);
        etLastRevision = view.findViewById(R.id.etVehicleLastRevision);
        btnSave = view.findViewById(R.id.btnSaveVehicle);
        viewModel = new ViewModelProvider(requireParentFragment()).get(VehiclesViewModel.class);
        repository = new FirestoreRepository();
        if (getArguments() != null && getArguments().containsKey("documentId")) {
            vehicleToEdit = new Vehicle();
            vehicleToEdit.setDocumentId(getArguments().getString("documentId"));
            vehicleToEdit.setId(getArguments().getString("id"));
            vehicleToEdit.setPlate(getArguments().getString("plate"));
            vehicleToEdit.setMarcaModelo(getArguments().getString("marcaModelo"));
            vehicleToEdit.setYear(getArguments().getInt("year"));
            vehicleToEdit.setLastTechnicalRevision(getArguments().getLong("lastRevision"));
            etId.setText(vehicleToEdit.getId());
            etPlate.setText(vehicleToEdit.getPlate());
            etMarcaModelo.setText(vehicleToEdit.getMarcaModelo());
            etYear.setText(String.valueOf(vehicleToEdit.getYear()));
            etLastRevision.setText(DateUtils.formatDate(vehicleToEdit.getLastTechnicalRevision()));
            selectedRevisionDate = vehicleToEdit.getLastTechnicalRevision();
        } else {
            selectedRevisionDate = System.currentTimeMillis();
            etLastRevision.setText(DateUtils.formatDate(selectedRevisionDate));
        }
        etLastRevision.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveVehicle());
        builder.setView(view)
                .setTitle(vehicleToEdit != null ? "Editar Vehículo" : "Agregar Vehículo")
                .setNegativeButton("Cancelar", (dialog, which) -> dismiss());
        return builder.create();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedRevisionDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedRevisionDate = selected.getTimeInMillis();
                    etLastRevision.setText(DateUtils.formatDate(selectedRevisionDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveVehicle() {
        String id = etId.getText().toString().trim();
        String plate = etPlate.getText().toString().trim();
        String marcaModelo = etMarcaModelo.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        if (id.isEmpty() || plate.isEmpty() || marcaModelo.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        int year = Integer.parseInt(yearStr);
        btnSave.setEnabled(false);
        String excludeDocId = vehicleToEdit != null ? vehicleToEdit.getDocumentId() : null;
        repository.isVehicleIdUniqueGlobally(id, excludeDocId, task -> {
            btnSave.setEnabled(true);
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult()) {
                    if (vehicleToEdit != null) {
                        vehicleToEdit.setId(id);
                        vehicleToEdit.setPlate(plate);
                        vehicleToEdit.setMarcaModelo(marcaModelo);
                        vehicleToEdit.setYear(year);
                        vehicleToEdit.setLastTechnicalRevision(selectedRevisionDate);
                        viewModel.updateVehicle(vehicleToEdit);
                    } else {
                        Vehicle newVehicle = new Vehicle(id, plate, marcaModelo, year, selectedRevisionDate);
                        viewModel.addVehicle(newVehicle);
                    }
                    Toast.makeText(getContext(), "Vehículo guardado", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "El ID del vehículo ya está en uso. Elija otro ID.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al validar el ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
