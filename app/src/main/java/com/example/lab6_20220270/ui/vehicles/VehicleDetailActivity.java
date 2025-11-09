package com.example.lab6_20220270.ui.vehicles;

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
        btnUpdateRevision.setOnClickListener(v -> updateLastRevision());
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
        android.util.Log.d("VehicleDetailActivity", "Buscando registros para vehicleId: " + vehicleId);
        repository.getLastOdometerForVehicle(vehicleId, task -> {
            if (task.isSuccessful()) {
                long lastOdometer = task.getResult() != null ? task.getResult() : 0;
                android.util.Log.d("VehicleDetailActivity", "Kilometraje obtenido: " + lastOdometer);
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
                android.util.Log.e("VehicleDetailActivity", "Error al obtener kilometraje", task.getException());
                Toast.makeText(this, "Error al obtener kilometraje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLastRevision() {
        long newRevisionDate = System.currentTimeMillis();
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
