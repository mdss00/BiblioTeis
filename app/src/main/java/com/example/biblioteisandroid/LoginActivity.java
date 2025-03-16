package com.example.biblioteisandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteisandroid.API.models.User;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.UserRepository;
import com.example.biblioteisandroid.auxiliar.Auxiliar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final String NOMBRE = "nombre";
    public static final String EMAIL = "email";
    public static final String IS_LOGGED = "isLogged";
    public static final String FECHA = "fecha";
    public static final String ID_USUARIO = "id_usuario";
    TextView textError;
    EditText edtNom, edtPass;
    Auxiliar auxiliar;
    Button buttonLogin;

    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtNom = findViewById(R.id.editNom);
        edtPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.btnLogIn);
        textError = findViewById(R.id.txtError);
        //SharedPreferences sp = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        //SharedPreferences.Editor editor = sp.edit();
        //editor.putInt();
        //editor.apply();
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
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(v);
            }
        });
        auxiliar = new Auxiliar(this);
        auxiliar.setUpToolbar();
    }

    public void doLogin(View v) {
        UserRepository ur = new UserRepository();

        BookRepository.ApiCallback<List<User>> callback = new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                boolean check = true;
                for (User user : result){
                    if (edtNom.getText().toString().equals(user.getName()) && edtPass.getText().toString().equals(user.getPasswordHash())){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(NOMBRE, user.getName());  // Guardar nombre de usuario
                        editor.putString(EMAIL, user.getEmail());  // Guardar email
                        editor.putBoolean(IS_LOGGED, true);  // Indicar que está logeado
                        editor.putString(FECHA, user.getDateJoined());
                        editor.putInt(ID_USUARIO, user.getId());
                        editor.apply();
                        Intent intent = new Intent(v.getContext(), MainActivity.class);
                        startActivity(intent);
                        check = false;
                    }
                }
                if (check){
                    textError.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Throwable t) {

            }
        };

        ur.getUsers(callback);
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