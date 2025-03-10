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

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    Button boton, botonLogIn, botonUser;
    ImageView imagenHarry, imagenHobbit, imagenBrujula;

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
        registerForContextMenu(imagenBrujula);
        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Establecer el toolbar como ActionBar
        getSupportActionBar().setTitle("BibliotecaTeis");
        imagenHobbit.setImageResource(R.drawable.hobbitp);
        imagenBrujula.setImageResource(R.drawable.brujulap);
        imagenHarry.setImageResource(R.drawable.harryp);


        //Seteamos el menu en el toolbar
        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate( R.menu.main_menu, menu);
                MenuItem logout = menu.findItem(R.id.cerrar);
                if (preferences.getString("nombre", "No definido").equals("No definido")){
                    logout.setVisible(false);
                }
                else{
                    logout.setVisible(true);
                }

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (item.getItemId() == R.id.menu_search) {
                    if (preferences.getString("nombre", "No definido").equals("No definido")) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(MainActivity.this, Activity_books.class);
                        startActivity(intent);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.menu_login) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (item.getItemId() == R.id.menu_profile) {
                    if (preferences.getString("nombre", "No definido").equals("No definido")) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(MainActivity.this, UserActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.cerrar){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Verificar qué vista activó el contexto
        if (v.getId() == R.id.imgBrujula) {
            // Inflar el menú de contexto desde un recurso XML
            getMenuInflater().inflate(R.menu.main_menu, menu); // Definimos el menú en un archivo XML
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_search) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(MainActivity.this, Activity_books.class);
                startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.menu_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_profile) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return true;
    }

}