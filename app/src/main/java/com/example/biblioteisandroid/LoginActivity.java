package com.example.biblioteisandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteisandroid.API.models.User;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.UserRepository;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    TextView textError;
    EditText edtNom, edtPass;
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtNom = findViewById(R.id.editNom);
        edtPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.btnLogIn);
        textError = findViewById(R.id.txtError);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(v);
            }
        });
        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void doLogin(View v) {
        UserRepository ur = new UserRepository();

        BookRepository.ApiCallback<List<User>> callback = new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                boolean check = true;
                for (User user : result){
                    if (edtNom.getText().toString().equals(user.getName()) && edtPass.getText().toString().equals(user.getPasswordHash())){
                        SearchHolder.getInstance().setUser(user);
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