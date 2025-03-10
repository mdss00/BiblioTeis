package com.example.biblioteisandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.ImageRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;

public class Activity_books extends AppCompatActivity {

    RecyclerView rvBooks;
    Button btnBuscar;
    EditText edtTitulo;
    EditText edtAutor;
    List<Book> listaBusqueda = new ArrayList<>();
    List<Bitmap> listaImagenes = new ArrayList<Bitmap>();
    Activity_booksViewModel  viewModel;

    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_books);
        MyAdapter adapter = new MyAdapter();
        rvBooks = findViewById(R.id.rvBookList);
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        rvBooks.setAdapter(adapter);
        btnBuscar = findViewById(R.id.btnBusqueda);
        edtTitulo = findViewById(R.id.buscaNombre);
        edtAutor = findViewById(R.id.buscaAutor);
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
        viewModel = new ViewModelProvider(this).get(Activity_booksViewModel.class);
        viewModel.loadBooks(edtTitulo.getText().toString(), edtAutor.getText().toString());
        viewModel.getListaLibros().observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(List<Book> books) {
                if (books != null) {
                    // Actualizar el adaptador con la nueva lista
                    adapter.setBooks(books); // Este método actualiza la lista en el adaptador
                }
            }
        });
        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BibliotecaTeis");

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarLibro();
            }
        });
    }
    // Adaptador para RecyclerView
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        List<Book> bookList = new ArrayList<>();

        public void setBooks(List<Book> books){
            bookList = books;
            notifyDataSetChanged();
        }
        // ViewHolder interno
        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textTitulo;
            ImageView imagenLibro;
            Button btnDetalles;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textTitulo = itemView.findViewById(R.id.txtTitulo);
                imagenLibro = itemView.findViewById(R.id.imgBook);
                btnDetalles = itemView.findViewById(R.id.btnDetalles);

            }
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout de cada ítem
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_book_list, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.textTitulo.setText(bookList.get(position).getTitle());
            BookRepository.ApiCallback<ResponseBody> cback = new BookRepository.ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody result) {
                    InputStream inputStream = result.byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    holder.imagenLibro.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(Activity_books.this, "Fallo", Toast.LENGTH_SHORT).show();
                }
            };
            Book book = bookList.get(position);
            if (!Objects.equals(book.getBookPicture(), "")){
                ImageRepository ir = new ImageRepository();
                ir.getImage(book.getBookPicture() , cback);
            }
            else{
                holder.imagenLibro.setImageResource(R.drawable.problemas_tecnicos);
            }

            holder.btnDetalles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ActivityDetalles.class);
                    holder.imagenLibro.setDrawingCacheEnabled(true);  // Habilita el cache de dibujo
                    holder.imagenLibro.buildDrawingCache();  // Construye el cache de dibujo
                    Bitmap bitmap = holder.imagenLibro.getDrawingCache();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);  // Convierte el bitmap a PNG
                    byte[] byteArray = byteArrayOutputStream.toByteArray();  // Obtén el array de bytes
                    intent.putExtra("imagen", byteArray);
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
            return bookList.size();
        }
    }

    public void buscarLibro() {
        viewModel.loadBooks(edtTitulo.getText().toString(), edtAutor.getText().toString());
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
            Intent intent = new Intent(Activity_books.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_search) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(Activity_books.this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(Activity_books.this, Activity_books.class);
                startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.menu_login) {
            Intent intent = new Intent(Activity_books.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.menu_profile) {
            if (preferences.getString("nombre", "No definido").equals("No definido")) {
                Intent intent = new Intent(Activity_books.this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(Activity_books.this, UserActivity.class);
                startActivity(intent);
            }
            return true;
        }
        else if (item.getItemId() == R.id.cerrar){
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(Activity_books.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }
}