package com.example.biblioteisandroid;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.ImageRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_books);

        rvBooks = findViewById(R.id.rvBookList);
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        rvBooks.setAdapter(new MyAdapter());
        btnBuscar = findViewById(R.id.btnBusqueda);
        edtTitulo = findViewById(R.id.buscaNombre);
        edtAutor = findViewById(R.id.buscaAutor);

        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarLibro();
            }
        });
        buscarLibro();

    }
    // Adaptador para RecyclerView
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        // ViewHolder interno
        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textTitulo;
            TextView textNumCopias;
            TextView textCopiasDisponibles;
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

            holder.textTitulo.setText(listaBusqueda.get(position).getTitle());
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
            Book book = listaBusqueda.get(position);
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
                    System.out.println("Hola");
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
            return listaBusqueda.size();
        }
    }

    public void buscarLibro() {
        BookRepository br = new BookRepository();
        ImageRepository ir = new ImageRepository();

        listaBusqueda.clear();
        BookRepository.ApiCallback<List<Book>> callback = new BookRepository.ApiCallback<List<Book>>() {

            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result){
                    if (edtTitulo.getText().toString().isEmpty() && edtAutor.getText().toString().isEmpty()){
                        listaBusqueda.add(book);
                    }
                    else if (edtTitulo.getText().toString().isEmpty()){
                        if (edtAutor.getText().toString().equals(book.getAuthor())){
                            listaBusqueda.add(book);
                        }
                    }
                    else if (edtAutor.getText().toString().isEmpty()){
                        if (edtTitulo.getText().toString().equals(book.getTitle())){
                            listaBusqueda.add(book);
                        }
                    }
                    else if (edtTitulo.getText().toString().equals(book.getTitle()) && edtAutor.getText().toString().equals(book.getAuthor())){
                        listaBusqueda.add(book);
                    }
                }

                // Notificar al adaptador que los datos han cambiado
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notificar al adaptador que la lista ha sido actualizada
                        rvBooks.getAdapter().notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(Throwable t) {

            }
        };



        br.getBooks(callback);
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