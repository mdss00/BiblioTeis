package com.example.biblioteisandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteisandroid.API.models.User;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.UserRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textError;
    EditText edtNom, edtPass;
    Button buttonLogin;
    ImageView imgUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        edtNom = findViewById(R.id.editNom);
        edtPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.btnLogIn);
        imgUser = findViewById(R.id.imgUser);
        textError = findViewById(R.id.txtError);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(v);
            }
        });
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
                        Intent intent = new Intent(v.getContext(), Activity_books.class);
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
}