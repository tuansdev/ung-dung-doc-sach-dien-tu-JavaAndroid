package com.example.ebookreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebookreader.R;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private final List<File> files;
    private final Consumer<File> onFileClick;

    public FileAdapter(List<File> files, Consumer<File> onFileClick) {
        this.files = files;
        this.onFileClick = onFileClick;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_list_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        holder.bind(file);
        holder.itemView.setOnClickListener(v -> onFileClick.accept(file));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }


    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFileIcon;
        TextView tvFileName;
        TextView tvFileSize;

        FileViewHolder(View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
        }

        void bind(File file) {
            tvFileName.setText(file.getName());
            // Format file size
            long fileSizeInBytes = file.length();
            String fileSize = fileSizeInBytes >= 1024 * 1024
                    ? String.format("%.2f MB", fileSizeInBytes / (1024.0 * 1024.0))
                    : String.format("%.2f KB", fileSizeInBytes / 1024.0);
            tvFileSize.setText(fileSize);
            // Set file type icon
            if (file.getName().endsWith(".pdf")) {
                ivFileIcon.setImageResource(R.drawable.ic_file_pdf);
            } else if (file.getName().endsWith(".txt")) {
                ivFileIcon.setImageResource(R.drawable.ic_file_txt);
            } else {
                ivFileIcon.setImageResource(R.drawable.ic_file_default);
            }
        }
    }
}