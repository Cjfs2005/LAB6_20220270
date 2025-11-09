package com.example.lab6_20220270.ui.summary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab6_20220270.R;
import com.example.lab6_20220270.model.FuelRecord;
import com.example.lab6_20220270.viewmodel.RecordsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private RecordsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        viewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        viewModel.getRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null && !records.isEmpty()) {
                setupBarChart(records);
                setupPieChart(records);
            }
        });
        viewModel.loadRecords();
        return view;
    }

    /*
    Modelo: Claude Sonnet 4.5 (Integrado en Github Copilot en modo ask para que reciba contexto)
    Prompt: Eres un desarrollador Android experto. Necesito implementar un fragmento de resumen que muestre
    dos gráficos usando MPAndroidChart: un gráfico de barras que agregue los litros consumidos por mes, y
    un gráfico circular (pie chart) que muestre la proporción de cada tipo de combustible usado.
    Correcciones: En base al código entregado, tuve que ajustar el formato de las etiquetas del eje X del
    gráfico de barras para que muestre MM/yyyy en lugar de yyyy-MM, y también tuve que configurar las
    propiedades de estilo de los gráficos para que se vean mejor en pantallas pequeñas.
    */
    private void setupBarChart(List<FuelRecord> records) {
        Map<String, Float> monthlyLiters = new HashMap<>();
        for (FuelRecord record : records) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(record.getDate());
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            String key = String.format("%02d/%d", month + 1, year);
            float currentLiters = monthlyLiters.getOrDefault(key, 0f);
            monthlyLiters.put(key, currentLiters + (float) record.getLiters());
        }
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : monthlyLiters.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Litros consumidos por mes");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        barChart.invalidate();
    }

    private void setupPieChart(List<FuelRecord> records) {
        Map<String, Float> fuelTypeLiters = new HashMap<>();
        for (FuelRecord record : records) {
            String fuelType = record.getFuelType();
            float currentLiters = fuelTypeLiters.getOrDefault(fuelType, 0f);
            fuelTypeLiters.put(fuelType, currentLiters + (float) record.getLiters());
        }
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : fuelTypeLiters.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "Proporción por tipo de combustible");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Tipos de combustible");
        pieChart.setCenterTextSize(14f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
