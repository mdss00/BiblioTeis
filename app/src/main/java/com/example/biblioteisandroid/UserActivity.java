package com.example.biblioteisandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.models.BookLending;
import com.example.biblioteisandroid.API.models.User;
import com.example.biblioteisandroid.API.repository.BookLendingRepository;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.ImageRepository;
import com.example.biblioteisandroid.auxiliar.Auxiliar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;

public class UserActivity extends AppCompatActivity {
    RecyclerView rvBookUserList;
    TextView textNombre, textEmail, textFecha;
    Button buttonVolver;

    Auxiliar auxiliar;

    List<BookLending> listaLibros = new ArrayList<>();

    List<Book> listaParaImagenes = new ArrayList<>();

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
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

        String fechaOriginal = preferences.getString("fecha", "");

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
        textNombre = findViewById(R.id.txtNameUser);
        textEmail = findViewById(R.id.txtEmailUser);
        textFecha = findViewById(R.id.txtFechaJoin);
        buttonVolver = findViewById(R.id.btnVolverUser);
        rvBookUserList = findViewById(R.id.rvBooksUser);
        rvBookUserList.setLayoutManager(new LinearLayoutManager(this));
        rvBookUserList.setAdapter(new MyAdapter());


        textNombre.setText(preferences.getString("nombre", ""));
        textEmail.setText(preferences.getString("email", ""));
        textFecha.setText(fechaFormateada);
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        BookLendingRepository blr = new BookLendingRepository();
        BookRepository bookRepository = new BookRepository();
        listaLibros.clear();
        listaParaImagenes.clear();
        BookRepository.ApiCallback<List<Book>> callback_books = new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result){
                    for (BookLending bookLending : listaLibros){
                        if (bookLending.getBookId() == book.getId() ){
                            listaParaImagenes.add(book);
                        }
                    }
                }
                rvBookUserList.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Err","ee");
            }
        };
        BookRepository.ApiCallback<List<BookLending>> callback = new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                for (BookLending bookLending : result){
                    if (bookLending.getUserId() == preferences.getInt("id_usuario", 0) && bookLending.getReturnDate() == null){
                        listaLibros.add(bookLending);
                    }
                }
                bookRepository.getBooks(callback_books);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(UserActivity.this, "Fallo", Toast.LENGTH_SHORT).show();
            }
        };

        blr.getAllLendings(callback);

    }

    // Adaptador para RecyclerView
    class MyAdapter extends RecyclerView.Adapter<UserActivity.MyAdapter.MyViewHolder> {

        // ViewHolder interno
        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textTituloBookUser, textFechaDevolucionUser;
            ImageView imagenLibroUser;

            Button botonDetalles;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textTituloBookUser = itemView.findViewById(R.id.txtLibroTituloUser);
                textFechaDevolucionUser = itemView.findViewById(R.id.txtDevuelveFechaUser);
                imagenLibroUser = itemView.findViewById(R.id.imgLibroUser);
                botonDetalles = itemView.findViewById(R.id.btnDetallesUser);
            }
        }

        @NonNull
        @Override
        public UserActivity.MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout de cada ítem
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_user_books, parent, false);
            return new UserActivity.MyAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserActivity.MyAdapter.MyViewHolder holder, int position) {

            holder.textTituloBookUser.setText(listaParaImagenes.get(position).getTitle());
            BookRepository.ApiCallback<ResponseBody> cback = new BookRepository.ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody result) {
                    InputStream inputStream = result.byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    holder.imagenLibroUser.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(UserActivity.this, "Fallo", Toast.LENGTH_SHORT).show();
                }
            };
            Book book = listaParaImagenes.get(position);
            if (!Objects.equals(book.getBookPicture(), "")) {
                ImageRepository ir = new ImageRepository();
                ir.getImage(book.getBookPicture(), cback);
            } else {
                holder.imagenLibroUser.setImageResource(R.drawable.problemas_tecnicos);
            }
            LocalDateTime fecha = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fecha = LocalDateTime.parse(listaLibros.get(position).getLendDate());

                // Sumar 15 días
                LocalDateTime nuevaFecha = fecha.plusDays(15);

                // Formatear la nueva fecha en "dd/MM/yyyy"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String resultado = nuevaFecha.format(formatter);
                LocalDateTime fechaHoy = LocalDateTime.now();
                if (fechaHoy.isAfter(nuevaFecha)) {
                    holder.textFechaDevolucionUser.setTextColor(Color.RED);
                }
                holder.textFechaDevolucionUser.setText(resultado);
            }
            holder.botonDetalles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Detalles", "Botón presionado en posición: " + position);
                    Intent intent = new Intent(v.getContext(), ActivityDetalles.class);
                    holder.imagenLibroUser.invalidate();
                    holder.imagenLibroUser.setDrawingCacheEnabled(true);
                    holder.imagenLibroUser.buildDrawingCache();
                    Bitmap bitmap = holder.imagenLibroUser.getDrawingCache();

                    if (bitmap != null) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        intent.putExtra("imagen", byteArray);
                    } else {
                        Log.e("Detalles", "Bitmap es null");
                    }

                    intent.putExtra("titulo", book.getTitle());
                    intent.putExtra("autor", book.getAuthor());
                    intent.putExtra("isbn", book.getIsbn());
                    intent.putExtra("fecha", book.getPublishedDate());
                    intent.putExtra("disponible", book.isAvailable());
                    intent.putExtra("id",book.getId());
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return listaLibros.size();
        }
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