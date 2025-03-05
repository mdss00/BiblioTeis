package com.example.biblioteisandroid;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.models.BookLending;
import com.example.biblioteisandroid.API.repository.BookLendingRepository;
import com.example.biblioteisandroid.API.repository.BookRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ActivityDetalles extends AppCompatActivity {
    public static final String DISPONIBLE = "Disponible";
    public static final String NO_DISPONIBLE = "No disponible";
    public static final String DEVOLUCIÓN = "Fecha de devolución: ";
    ImageView imgDetalle;
    TextView txtTitle, txtAuthor, txtFechaPubli, txtISBN, textReturnDate;
    RadioButton radioDisp;
    Integer idBook;
    Button botonPrestamo, botonDevolver;

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

        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgDetalle = findViewById(R.id.imgFotoDetalles);
        txtTitle = findViewById(R.id.txtTitleDetalle);
        txtAuthor = findViewById(R.id.txtAut);
        txtFechaPubli = findViewById(R.id.txtFecha);
        txtISBN = findViewById(R.id.txtISBN);
        radioDisp = findViewById(R.id.rbDisp);
        botonPrestamo = findViewById(R.id.btnPrestamo);
        botonDevolver = findViewById(R.id.btnDevolver);
        textReturnDate = findViewById(R.id.txtReturnBook);
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
        idBook = getIntent().getIntExtra("id",0);
        txtTitle.setText("Título: " + getIntent().getStringExtra("titulo"));
        txtAuthor.setText("Autor: " + getIntent().getStringExtra("autor"));
        txtFechaPubli.setText("Fecha publicación: " + fechaFormateada);
        txtISBN.setText("ISBN: " + getIntent().getStringExtra("isbn"));
        if (getIntent().getBooleanExtra("disponible", false)){
            radioDisp.setText(DISPONIBLE);
            radioDisp.setChecked(true);
            botonPrestamo.setEnabled(true);
        }
        else{
            radioDisp.setText(NO_DISPONIBLE);
            radioDisp.setChecked(false);
            botonPrestamo.setEnabled(false);
            fechaDevolucion();
        }
        botonPrestamo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPrestado();
                radioDisp.setText(NO_DISPONIBLE);
                radioDisp.setChecked(false);
                botonPrestamo.setEnabled(false);
                fechaDevolucion();
                botonDevolver.setVisibility(VISIBLE);
            }
        });

        botonDevolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devolverLibro();
                radioDisp.setText(DISPONIBLE);
                radioDisp.setChecked(true);
                botonPrestamo.setEnabled(true);
                textReturnDate.setVisibility(INVISIBLE);
                botonDevolver.setVisibility(INVISIBLE);
            }
        });

    }

    private void devolverLibro() {
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        BookRepository.ApiCallback<Boolean> callback = new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        };

        bookLendingRepository.returnBook(idBook, callback);
    }

    public void pedirPrestado(){

        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        BookRepository.ApiCallback<Boolean> callback = new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        };

        bookLendingRepository.lendBook(SearchHolder.getInstance().getUser().getId(),idBook, callback);
    }
    public void fechaDevolucion(){
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        BookRepository.ApiCallback<List<BookLending>> callback = new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                for (BookLending bookLending : result){
                    if (bookLending.getBookId() == idBook){
                        LocalDateTime fecha = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            fecha = LocalDateTime.parse(bookLending.getLendDate());

                            // Sumar 15 días
                            LocalDateTime nuevaFecha = fecha.plusDays(15);

                            // Formatear la nueva fecha en "dd/MM/yyyy"
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String resultado = nuevaFecha.format(formatter);
                            textReturnDate.setText(DEVOLUCIÓN + resultado);
                            textReturnDate.setVisibility(VISIBLE);
                        }
                    }
                    if (bookLending.getBookId() == idBook && bookLending.getUserId() == SearchHolder.getInstance().getUser().getId()){
                        botonDevolver.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(ActivityDetalles.this, "Fallo Book lending", Toast.LENGTH_SHORT).show();
            }
        };

        bookLendingRepository.getAllLendings(callback);
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