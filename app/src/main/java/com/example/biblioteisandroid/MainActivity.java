package com.example.biblioteisandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button boton, botonLogIn, botonUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        botonUser = findViewById(R.id.btnPerfil);
        botonLogIn = findViewById(R.id.btnLoginIn);
        boton = findViewById(R.id.btnIrBuscar);
        if (SearchHolder.getInstance().getUser().getName() == null) {
            botonUser.setEnabled(false);
        }
        else{
            botonUser.setEnabled(true);
        }
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SearchHolder.getInstance().getUser().getName() == null) {
                    Intent intent = new Intent(v.getContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(v.getContext(), Activity_books.class);
                    startActivity(intent);
                }
            }
        });
        botonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        botonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserActivity.class);
                startActivity(intent);
            }
        });
    }
}