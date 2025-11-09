package com.example.lab6_20220270.ui.records;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.util.DateUtils;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private List<FuelRecord> records;
    private Context context;
    private OnRecordClickListener listener;

    public interface OnRecordClickListener {
        void onRecordEdit(FuelRecord record);
        void onRecordDelete(FuelRecord record);
    }

    public RecordAdapter(List<FuelRecord> records, Context context, OnRecordClickListener listener) {
        this.records = records;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FuelRecord record = records.get(position);
        holder.tvRecordId.setText("ID: " + record.getRecordId());
        holder.tvVehicleId.setText("Vehículo: " + record.getVehicleId());
        holder.tvDate.setText("Fecha y hora: " + DateUtils.formatDateTime(record.getDate()));
        holder.tvLiters.setText("Litros: " + record.getLiters());
        holder.tvOdometer.setText("Kilometraje: " + record.getOdometer());
        holder.tvPrice.setText("Precio: S/ " + record.getTotalPrice());
        holder.tvFuelType.setText("Combustible: " + record.getFuelType());
        holder.btnEdit.setOnClickListener(v -> listener.onRecordEdit(record));
        holder.btnDelete.setOnClickListener(v -> listener.onRecordDelete(record));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void updateData(List<FuelRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecordId, tvVehicleId, tvDate, tvLiters, tvOdometer, tvPrice, tvFuelType;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecordId = itemView.findViewById(R.id.tvRecordId);
            tvVehicleId = itemView.findViewById(R.id.tvRecordVehicleId);
            tvDate = itemView.findViewById(R.id.tvRecordDate);
            tvLiters = itemView.findViewById(R.id.tvRecordLiters);
            tvOdometer = itemView.findViewById(R.id.tvRecordOdometer);
            tvPrice = itemView.findViewById(R.id.tvRecordPrice);
            tvFuelType = itemView.findViewById(R.id.tvRecordFuelType);
            btnEdit = itemView.findViewById(R.id.btnEditRecord);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecord);
        }
    }
}
