package com.example.biblioteisandroid.auxiliar;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.biblioteisandroid.Activity_books;
import com.example.biblioteisandroid.LoginActivity;
import com.example.biblioteisandroid.MainActivity;
import com.example.biblioteisandroid.R;
import com.example.biblioteisandroid.UserActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Auxiliar {

    AppCompatActivity activity;
    SharedPreferences preferences;
    public Auxiliar(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setUpToolbar(){
        // Configurar el Toolbar
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("BibliotecaTeis");
        MasterKey masterKey = null;
        try {
            masterKey = new MasterKey.Builder(activity)
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
                    activity, // Nombre del archivo SharedPreferences
                    "SesionUsuario", // MasterKey para cifrado
                    masterKey, // Contexto
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifrado de claves
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Cifrado de valores
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean createMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem logout = menu.findItem(R.id.cerrar);
        if (preferences.getString("nombre", "No definido").equals("No definido")) {
            logout.setVisible(false);
        }
        else{
            logout.setVisible(true);
        }
        return true;
    }

    public boolean opcionesMenu(MenuItem item){
        Context context = activity.getApplicationContext();

        if (item.getItemId() == R.id.menu_home) {
            Intent intent = new Intent(context, MainActivity.class);
            activity.startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_search) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(context, LoginActivity.class);
                activity.startActivity(intent);
            }
            else{
                Intent intent = new Intent(context, Activity_books.class);
                activity.startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.menu_login) {
            Intent intent = new Intent(context, LoginActivity.class);
            activity.startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_profile) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(context, LoginActivity.class);
                activity.startActivity(intent);
            }
            else{
                Intent intent = new Intent(context, UserActivity.class);
                activity.startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.cerrar){
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(context, MainActivity.class);
            activity.startActivity(intent);
        }
        return true;
    }
}
