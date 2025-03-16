package com.example.biblioteisandroid;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.biblioteisandroid.API.models.Book;
import com.example.biblioteisandroid.API.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Activity_booksViewModel extends ViewModel {
    private MutableLiveData<List<Book>> listaLibros = new MutableLiveData<>();
    private BookRepository bookRepository = new BookRepository();

    public void loadBooks(String titulo, String autor){
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                List<Book> currentList = new ArrayList<>();
                for (Book book: result){
                    if (titulo.isEmpty() && autor.isEmpty()){
                        currentList.add(book);
                    }
                    else if (titulo.isEmpty()){
                        if (book.getAuthor().contains(autor)){
                            currentList.add(book);
                        }
                    }
                    else if (autor.isEmpty()){
                        if (book.getTitle().contains(titulo)){
                            currentList.add(book);
                        }
                    }
                    else if (book.getTitle().contains(titulo) && book.getAuthor().contains(autor)){
                        currentList.add(book);
                    }
                }
                if (!Objects.equals(listaLibros.getValue(), currentList))
                    listaLibros.setValue(currentList);
            }

            @Override
            public void onFailure(Throwable t) {
                listaLibros.setValue(null);
            }
        });
    }

    public MutableLiveData<List<Book>> getListaLibros(){
        return listaLibros;
    }

}
