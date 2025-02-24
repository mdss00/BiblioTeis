package com.example.biblioteisandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.repository.BookRepository;
import com.example.biblioteisandroid.API.repository.ImageRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarLibro(v);
            }
        });

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
                textNumCopias = itemView.findViewById(R.id.txtNumCopias);
                textCopiasDisponibles = itemView.findViewById(R.id.txtCopiasDisp);
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
            holder.imagenLibro.setImageBitmap(listaImagenes.get(position));
            holder.btnDetalles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Hola");
                    Intent intent = new Intent(v.getContext(), ActivityDetalles.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return listaBusqueda.size();
        }
    }

    public void buscarLibro(View v) {
        BookRepository br = new BookRepository();
        ImageRepository ir = new ImageRepository();

        listaBusqueda.clear();
        BookRepository.ApiCallback<List<Book>> callback = new BookRepository.ApiCallback<List<Book>>() {

            BookRepository.ApiCallback<ResponseBody> cback = new BookRepository.ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody result) {
                    InputStream inputStream = result.byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    listaImagenes.add(bitmap);
                }

                @Override
                public void onFailure(Throwable t) {

                }
            };

            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result){
                    if (edtTitulo.getText().toString().isEmpty()){
                        if (edtAutor.getText().toString().equals(book.getAuthor())){
                            listaBusqueda.add(book);
                            ir.getImage(book.getBookPicture() , cback);
                        }
                    }
                    else if (edtAutor.getText().toString().isEmpty()){
                        if (edtTitulo.getText().toString().equals(book.getTitle())){
                            listaBusqueda.add(book);
                            ir.getImage(book.getBookPicture() , cback);
                        }
                    }
                    else if (edtTitulo.getText().toString().equals(book.getTitle()) && edtAutor.getText().toString().equals(book.getAuthor())){
                        listaBusqueda.add(book);
                        ir.getImage(book.getBookPicture() , cback);
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
}