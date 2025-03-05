package com.example.biblioteisandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button boton, botonLogIn, botonUser;
    ImageView imagenHarry, imagenHobbit, imagenBrujula;

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
        imagenBrujula = findViewById(R.id.imgBrujula);
        imagenHarry = findViewById(R.id.imgHarry);
        imagenHobbit = findViewById(R.id.imgHobbit);
        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Establecer el toolbar como ActionBar

        imagenHobbit.setImageResource(R.drawable.hobbitp);
        imagenBrujula.setImageResource(R.drawable.brujulap);
        imagenHarry.setImageResource(R.drawable.harryp);
    }

    // Inflar el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Inflar el archivo de menú
        return true;
    }

    // Manejar clics en los ítems del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_search) {
            Toast.makeText(this, "Buscar", Toast.LENGTH_SHORT).show();
            if (SearchHolder.getInstance().getUser().getName() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(this, Activity_books.class);
                startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.menu_login) {
            Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_profile) {
            Toast.makeText(this, "Mi Perfil", Toast.LENGTH_SHORT).show();
            if (SearchHolder.getInstance().getUser().getName() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return true;
    }
}