package com.example.biblioteisandroid;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.models.BookLending;
import com.example.biblioteisandroid.API.repository.BookLendingRepository;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.auxiliar.Auxiliar;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
    Auxiliar auxiliar;
    Button botonPrestamo, botonDevolver;
    SharedPreferences preferences;

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
        // Configurar el Toolbar
        auxiliar = new Auxiliar(this);
        auxiliar.setUpToolbar();
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
            textReturnDate.setText("");
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
                textReturnDate.setText("");
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
                fechaDevolucion();
            }

            @Override
            public void onFailure(Throwable t) {

            }
        };

        bookLendingRepository.lendBook(preferences.getInt("id_usuario",0), idBook, callback);
    }
    public void fechaDevolucion(){
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        BookRepository.ApiCallback<List<BookLending>> callback = new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                Log.i("ActivityDetails", result.toString());
                for (BookLending bookLending : result){
                    if (bookLending.getBookId() == idBook && bookLending.getReturnDate() == null){
                        LocalDateTime fecha = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            fecha = LocalDateTime.parse(bookLending.getLendDate());

                            // Sumar 15 días
                            LocalDateTime nuevaFecha = fecha.plusDays(15);

                            // Formatear la nueva fecha en "dd/MM/yyyy"
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String resultado = nuevaFecha.format(formatter);
                            //runOnUiThread(() -> textReturnDate.setText(DEVOLUCIÓN + resultado));
                            Log.i("ActivityDetalles", "Nuevo actual: " + resultado);
                            textReturnDate.setText(DEVOLUCIÓN + resultado);


                        }
                    }
                    if (bookLending.getBookId() == idBook && bookLending.getUserId() == preferences.getInt("id_usuario",0) && bookLending.getReturnDate() == null){
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