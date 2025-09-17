package com.example.ebookreader.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ebookreader.api.Book;
import com.example.ebookreader.databinding.ItemBookBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books = new ArrayList<>();
    private final Consumer<Book> onBookClick;

    public BookAdapter(Consumer<Book> onBookClick) {
        this.onBookClick = onBookClick;
    }

    public void setBooks(List<Book> newBooks) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BookDiffCallback(books, newBooks));
        books = newBooks != null ? newBooks : new ArrayList<>();
        diffResult.dispatchUpdatesTo(this);
    }

    public void addBooks(List<Book> newBooks) {
        if (newBooks != null) {
            int startPosition = books.size();
            books.addAll(newBooks);
            notifyItemRangeInserted(startPosition, newBooks.size());
        }
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book) {
            binding.tvTitle.setText(book.title);
            binding.tvAuthor.setText(book.authors != null && !book.authors.isEmpty() ? book.authors.get(0).name : "Unknown");
            binding.getRoot().setOnClickListener(v -> onBookClick.accept(book));
        }
    }

    static class BookDiffCallback extends DiffUtil.Callback {
        private final List<Book> oldList;
        private final List<Book> newList;

        BookDiffCallback(List<Book> oldList, List<Book> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}