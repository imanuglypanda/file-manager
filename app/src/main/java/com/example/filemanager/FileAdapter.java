package com.example.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private Context context;
    private List<File> file;
    private OnFileSelectedListener listener;
    private MediaMetadataRetriever retriever;
    private MediaPlayer mediaPlayer;

    public FileAdapter(Context context, List<File> file, OnFileSelectedListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.file_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.tvName.setText(file.get(position).getName());
        holder.tvName.setSelected(true);

        // Display file inside the Folder
        int items = 0;
        if (file.get(position).isDirectory()) {
            File[] files = file.get(position).listFiles();
            for (File singleFile : files) {
                if (!singleFile.isHidden()) {
                    items += 1;
                }
            }
            holder.tvSize.setText(items + " Files");
        }
        else {
            holder.tvSize.setText(Formatter.formatShortFileSize(
                    context,
                    file.get(position).length()
            ));
        }

        String fileName = file.get(position).getName().toLowerCase();

        if (fileName.endsWith(".jpeg")) {
            holder.imgFile.setImageBitmap(
                    BitmapFactory.decodeFile(file.get(position).getAbsolutePath())
            );
            holder.imgFile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else if (fileName.endsWith(".jpg")) {
            holder.imgFile.setImageBitmap(
                    BitmapFactory.decodeFile(file.get(position).getAbsolutePath())
            );
            holder.imgFile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else if (fileName.endsWith(".png")) {
            holder.imgFile.setImageBitmap(
                    BitmapFactory.decodeFile(file.get(position).getAbsolutePath())
            );
            holder.imgFile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else if (fileName.endsWith(".webp")) {
            holder.imgFile.setImageBitmap(
                    BitmapFactory.decodeFile(file.get(position).getAbsolutePath())
            );
        }
        else if (fileName.endsWith(".pdf")) {
            holder.imgFile.setImageResource(R.drawable.ic_pdf);
        }
        else if (fileName.endsWith(".doc")) {
            holder.imgFile.setImageResource(R.drawable.ic_docs);
        }
        else if (fileName.endsWith(".mp3")) {
            holder.imgFile.setImageResource(R.drawable.ic_music);
            holder.mediaTime.setText(getTotalDuration(file.get(position)));
        }
        else if (fileName.endsWith(".wav")) {
            holder.imgFile.setImageResource(R.drawable.ic_music);
            holder.mediaTime.setText(getTotalDuration(file.get(position)));
        }
        else if (fileName.endsWith(".mp4")) {
//            holder.imgFile.setImageResource(R.drawable.ic_video);
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.get(position).getAbsolutePath());
            Bitmap frameBitmap = retriever.getFrameAtTime(
                    0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frameBitmap != null) {
                holder.imgFile.setImageBitmap(frameBitmap);
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                holder.imgFile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.mediaTime.setText(getTotalDuration(file.get(position)));

            } else {
                holder.imgFile.setImageResource(R.drawable.ic_video);
            }
        }
        else if (fileName.endsWith(".apk")) {
            holder.imgFile.setImageResource(R.drawable.ic_android);
        }
        else {
            holder.imgFile.setImageResource(R.drawable.ic_folder);
            holder.imgFile.setPadding(30, 30, 30, 30);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFileClicked(file.get(position));
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onFileLongClicked(file.get(position), position);
                return true;
            }
        });
    }

    public String getTotalDuration(File file) {
        mediaPlayer = MediaPlayer.create(
                context.getApplicationContext(),
                Uri.parse(file.getAbsolutePath())
        );
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        if (duration >= 3600000) {
            return String.format(Locale.getDefault(),"%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        }
    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
