package com.example.biblioteisandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.biblioteisandroid.auxiliar.Auxiliar;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    Button boton, botonLogIn, botonUser;
    ImageView imagenHarry, imagenHobbit, imagenBrujula;
    Auxiliar auxiliar;
    SharedPreferences preferences;

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
        MasterKey masterKey = null;
        try {
            masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // Usando AES de 256 bits.
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Inicializar EncryptedSharedPreferences.
        try {
            preferences = EncryptedSharedPreferences.create(
                    this, // Nombre del archivo SharedPreferences
                    "SesionUsuario", // MasterKey para cifrado
                    masterKey, // Contexto
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifrado de claves
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Cifrado de valores
            );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imagenBrujula = findViewById(R.id.imgBrujula);
        imagenHarry = findViewById(R.id.imgHarry);
        imagenHobbit = findViewById(R.id.imgHobbit);
        // Configurar el Toolbar
        auxiliar = new Auxiliar(this);
        auxiliar.setUpToolbar();
        imagenHobbit.setImageResource(R.drawable.hobbitp);
        imagenBrujula.setImageResource(R.drawable.brujulap);
        imagenHarry.setImageResource(R.drawable.harryp);

    }
    // Inflater del menú en la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return auxiliar.createMenu(menu);
    }

    // listeners de las opciones de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return auxiliar.opcionesMenu(item);
    }

}