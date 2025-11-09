package com.example.lab6_20220270.ui.vehicles;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.model.Vehicle;
import com.example.lab6_20220270.repository.FirestoreRepository;
import com.example.lab6_20220270.util.DateUtils;
import com.example.lab6_20220270.util.QRGenerator;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Calendar;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class VehicleDetailActivity extends AppCompatActivity {

    private TextView tvVehicleInfo;
    private ImageView ivQR;
    private Button btnUpdateRevision;
    private String vehicleDocId, vehiclePlate, vehicleId, vehicleMarcaModelo;
    private int vehicleYear;
    private long vehicleLastRevision;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);
        tvVehicleInfo = findViewById(R.id.tvVehicleDetailInfo);
        ivQR = findViewById(R.id.ivQRCode);
        btnUpdateRevision = findViewById(R.id.btnUpdateRevision);
        repository = new FirestoreRepository();
        vehicleDocId = getIntent().getStringExtra("vehicleDocId");
        vehiclePlate = getIntent().getStringExtra("vehiclePlate");
        vehicleId = getIntent().getStringExtra("vehicleId");
        vehicleMarcaModelo = getIntent().getStringExtra("vehicleMarcaModelo");
        vehicleYear = getIntent().getIntExtra("vehicleYear", 0);
        vehicleLastRevision = getIntent().getLongExtra("vehicleLastRevision", 0);
        displayVehicleInfo();
        generateQR();
        btnUpdateRevision.setOnClickListener(v -> showRevisionDatePicker());
    }

    private void displayVehicleInfo() {
        String info = "ID: " + vehicleId + "\n" +
                "Placa: " + vehiclePlate + "\n" +
                "Marca/Modelo: " + vehicleMarcaModelo + "\n" +
                "Año: " + vehicleYear + "\n" +
                "Última revisión: " + DateUtils.formatDate(vehicleLastRevision);
        tvVehicleInfo.setText(info);
    }

    private void generateQR() {
        repository.getLastOdometerForVehicle(vehicleId, task -> {
            if (task.isSuccessful()) {
                long lastOdometer = task.getResult() != null ? task.getResult() : 0;
                JSONObject qrData = new JSONObject();
                try {
                    qrData.put("placa", vehiclePlate);
                    qrData.put("kilometraje", lastOdometer);
                    qrData.put("ultima_revision", DateUtils.formatDate(vehicleLastRevision));
                    String qrContent = qrData.toString();
                    Bitmap qrBitmap = QRGenerator.generateQR(qrContent, 512, 512);
                    if (qrBitmap != null) {
                        ivQR.setImageBitmap(qrBitmap);
                    } else {
                        Toast.makeText(this, "Error generando QR", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error generando QR", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error al obtener kilometraje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    Modelo: Claude Sonnet 4.5 (Integrado en Github Copilot en modo ask para que reciba contexto)
    Prompt: Eres un experto desarrollador Android. Para actualizar la revisión técnica del vehículo, necesito
    que al presionar el botón aparezca un DatePickerDialog que solicite la fecha y valide que sea mayor a la
    fecha actual. Si no es válida, debe mostrar un Toast de error.
    Correcciones: En base al código entregado, tuve que corregir el título del DatePickerDialog de "Fecha de
    próxima revisión técnica" a "Fecha de última revisión técnica" para que coincida con la lógica del negocio,
    y también tuve que importar java.util.Calendar que faltaba en los imports iniciales.
    */
    private void showRevisionDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            long selectedTimestamp = selectedDate.getTimeInMillis();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayTimestamp = today.getTimeInMillis();
            if (selectedTimestamp <= todayTimestamp) {
                Toast.makeText(this, "La fecha de revisión técnica debe ser posterior a hoy", Toast.LENGTH_LONG).show();
            } else {
                updateLastRevision(selectedTimestamp);
            }
        }, year, month, day);
        datePickerDialog.setTitle("Fecha de última revisión técnica");
        datePickerDialog.show();
    }

    private void updateLastRevision(long newRevisionDate) {
        Vehicle updatedVehicle = new Vehicle(vehicleId, vehiclePlate, vehicleMarcaModelo, vehicleYear, newRevisionDate);
        updatedVehicle.setDocumentId(vehicleDocId);
        repository.updateVehicle(vehicleDocId, updatedVehicle, task -> {
            if (task.isSuccessful()) {
                vehicleLastRevision = newRevisionDate;
                displayVehicleInfo();
                generateQR();
                Toast.makeText(this, "Revisión técnica actualizada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
