package com.example.biblioteisandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityDetalles extends AppCompatActivity {
    public static final String DISPONIBLE = "Disponible";
    public static final String NO_DISPONIBLE = "No disponible";
    ImageView imgDetalle;
    TextView txtTitle, txtAuthor, txtFechaPubli, txtISBN;
    RadioButton radioDisp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgDetalle = findViewById(R.id.imgFotoDetalles);
        txtTitle = findViewById(R.id.txtTitleDetalle);
        txtAuthor = findViewById(R.id.txtAut);
        txtFechaPubli = findViewById(R.id.txtFecha);
        txtISBN = findViewById(R.id.txtISBN);
        radioDisp = findViewById(R.id.rbDisp);
        String fechaOriginal = getIntent().getStringExtra("fecha");

        // Definir el formato de la fecha original
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        // Parsear la fecha original a un objeto Date
        Date fecha = null;
        try {
            fecha = formatoEntrada.parse(fechaOriginal);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Definir el formato de salida solo con día, mes y año
        SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy");

        // Formatear la fecha a solo día, mes y año
        String fechaFormateada = formatoSalida.format(fecha);
        byte[] byteArray = getIntent().getByteArrayExtra("imagen");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imgDetalle.setImageBitmap(bitmap);
        }
        txtTitle.setText("Título: " + getIntent().getStringExtra("titulo"));
        txtAuthor.setText("Autor: " + getIntent().getStringExtra("autor"));
        txtFechaPubli.setText("Fecha publicación: " + fechaFormateada);
        txtISBN.setText("ISBN: " + getIntent().getStringExtra("isbn"));
        if (getIntent().getBooleanExtra("disponible", false)){
            radioDisp.setText(DISPONIBLE);
            radioDisp.setChecked(true);
        }
        else{
            radioDisp.setText(NO_DISPONIBLE);
            radioDisp.setChecked(false);
        }

    }
}