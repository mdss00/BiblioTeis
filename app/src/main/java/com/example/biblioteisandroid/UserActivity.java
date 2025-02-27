package com.example.biblioteisandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteisandroid.API.models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserActivity extends AppCompatActivity {

    TextView textNombre, textEmail, textFecha;
    Button buttonVolver;
    User usuarioSesion = SearchHolder.getInstance().getUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String fechaOriginal = usuarioSesion.getDateJoined();

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
        textNombre = findViewById(R.id.txtNameUser);
        textEmail = findViewById(R.id.txtEmailUser);
        textFecha = findViewById(R.id.txtFechaJoin);
        buttonVolver = findViewById(R.id.btnVolverUser);

        textNombre.setText(usuarioSesion.getName());
        textEmail.setText(usuarioSesion.getEmail());
        textFecha.setText(fechaFormateada);
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}