package com.example.lab6_20220270.ui.vehicles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.Vehicle;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private List<Vehicle> vehicles;
    private Context context;
    private OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
        void onVehicleEdit(Vehicle vehicle);
        void onVehicleDelete(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicles, Context context, OnVehicleClickListener listener) {
        this.vehicles = vehicles;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.tvId.setText("ID: " + vehicle.getId());
        holder.tvPlate.setText("Placa: " + vehicle.getPlate());
        holder.tvMarcaModelo.setText("Marca/Modelo: " + vehicle.getMarcaModelo());
        holder.tvYear.setText("Año: " + vehicle.getYear());
        holder.itemView.setOnClickListener(v -> listener.onVehicleClick(vehicle));
        holder.btnEdit.setOnClickListener(v -> listener.onVehicleEdit(vehicle));
        holder.btnDelete.setOnClickListener(v -> listener.onVehicleDelete(vehicle));
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public void updateData(List<Vehicle> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvPlate, tvMarcaModelo, tvYear;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvVehicleId);
            tvPlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvMarcaModelo = itemView.findViewById(R.id.tvVehicleMarcaModelo);
            tvYear = itemView.findViewById(R.id.tvVehicleYear);
            btnEdit = itemView.findViewById(R.id.btnEditVehicle);
            btnDelete = itemView.findViewById(R.id.btnDeleteVehicle);
        }
    }
}
