package com.example.filemanager.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAdapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategorizedFragment extends Fragment implements OnFileSelectedListener {
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList;
    String[] items = {"Details", "Rename", "Delete", "Share"};
    File path;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categorized, container, false);

        Bundle bundle = this.getArguments();
        if (bundle.getString("fileType").equals("downloads")) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
        else {
            path = Environment.getExternalStorageDirectory();
        }

        runtimePermission();

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions (
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list,
                                                           PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public ArrayList<File> findFiles(File file) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findFiles(singleFile));
            }
            else {
                switch (getArguments().getString("fileType")) {
                    case "image":
                        if (
                                singleFile.getName().toLowerCase().endsWith(".jpeg") ||
                                singleFile.getName().toLowerCase().endsWith(".jpg") ||
                                singleFile.getName().toLowerCase().endsWith(".png") ||
                                singleFile.getName().toLowerCase().endsWith(".webp")
                        ) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "video":
                        if (singleFile.getName().toLowerCase().endsWith(".mp4")) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "music":
                        if (
                                singleFile.getName().toLowerCase().endsWith(".mp3") ||
                                singleFile.getName().toLowerCase().endsWith(".wav")
                        ) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "docs":
                        if (
                                singleFile.getName().toLowerCase().endsWith(".pdf") ||
                                singleFile.getName().toLowerCase().endsWith(".doc")
                        ) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "apk":
                        if (singleFile.getName().toLowerCase().endsWith(".apk")) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "downloads":
                        if (
                                singleFile.getName().toLowerCase().endsWith(".jpeg") ||
                                singleFile.getName().toLowerCase().endsWith(".jpg") ||
                                singleFile.getName().toLowerCase().endsWith(".png") ||
                                singleFile.getName().toLowerCase().endsWith(".webp") ||
                                singleFile.getName().toLowerCase().endsWith(".mp3") ||
                                singleFile.getName().toLowerCase().endsWith(".wav") ||
                                singleFile.getName().toLowerCase().endsWith(".mp4") ||
                                singleFile.getName().toLowerCase().endsWith(".pdf") ||
                                singleFile.getName().toLowerCase().endsWith(".doc") ||
                                singleFile.getName().toLowerCase().endsWith(".apk")
                        ) {
                            arrayList.add(singleFile);
                        }
                        break;
                }
            }
        }
        return arrayList;
    }

    private void displayFiles() {
        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(path));
        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }

    private boolean noFileNameExist(String name) {
        for (File file : fileList) {
            if (file.getName().equals(name)) return false;
        }
        return true;
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            CategorizedFragment internalFragment = new CategorizedFragment();
            internalFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(
                    R.id.fragment_container,
                    internalFragment
            ).addToBackStack(null).commit();
        }
        else {
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onFileLongClicked(File file, int filePosition) {
        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select Option");
        ListView options = (ListView) optionDialog.findViewById(R.id.List);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                String fileName;

                switch (selectedItem) {
                    case "Details":
                        AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                        detailDialog.setTitle("Details:");
                        final TextView details = new TextView(getContext());
                        detailDialog.setView(details);
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formattedDate = formatter.format(lastModified);

                        details.setText(String.format(
                                        "File Name: " + file.getName() + "\n" +
                                        "Size: " + Formatter.formatShortFileSize(
                                                getContext(), file.length()) + "\n" +
                                        "Path: " + file.getAbsolutePath() + "\n" +
                                        "Last Modified: " + formattedDate
                                )
                        );
                        detailDialog.setPositiveButton(
                                "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_details = detailDialog.create();
                        alertDialog_details.show();
                        break;
                    case "Rename":
                        AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("Rename File:");
                        final EditText name = new EditText(getContext());
                        name.setText(file.getName().substring(0, file.getName().indexOf(".")));
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton(
                                "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String new_name = name.getEditableText().toString();
                                String extension = file.getAbsolutePath().
                                        substring(file.getAbsolutePath().lastIndexOf("."));
                                File current = new File(file.getAbsolutePath());
                                File destination = new File(file.getAbsolutePath()
                                        .replace(file.getName(), new_name) + extension);
                                if (noFileNameExist(destination.getName())) {
                                    if (current.renameTo(destination)) {
                                        try {
                                            fileList.set(filePosition, destination);
                                        } catch (Exception e) {
                                            Toast.makeText(
                                                    getContext(),
                                                    String.valueOf(e),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                        fileAdapter.notifyItemChanged(filePosition);
                                        Toast.makeText(
                                                getContext(),
                                                "Renamed successfully!",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                    else {
                                        Toast.makeText(
                                                getContext(),
                                                "Couldn't rename!",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                                else {
                                    Toast.makeText(
                                            getContext(),
                                            "Couldn't rename, This file name already exists!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                                optionDialog.cancel();
                            }
                        });

                        renameDialog.setNegativeButton(
                                "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_rename = renameDialog.create();
                        alertDialog_rename.show();
                        break;
                    case "Delete":
                        fileName = file.getName();
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete " + fileName + "?");
                        deleteDialog.setPositiveButton(
                                "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (file.delete()) {
                                    fileList.remove(filePosition);
                                    fileAdapter.notifyDataSetChanged();
                                    Toast.makeText(
                                            getContext(),
                                            fileName + " Deleted successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                                else {
                                    Toast.makeText(
                                            getContext(),
                                            "Cannot delete " + fileName,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                                optionDialog.cancel();
                            }
                        });

                        deleteDialog.setNegativeButton(
                                "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_delete = deleteDialog.create();
                        alertDialog_delete.show();
                        break;
                    case "Share":
                        fileName = file.getName();
                        Intent share = new Intent();
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(share, "Share " + fileName));
                        break;
                }
            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.option_layout, null);
            TextView txtOption = myView.findViewById(R.id.txtOption);
            ImageView imgOption = myView.findViewById(R.id.imgOption);
            txtOption.setText(items[position]);
            if (items[position].equals("Details")) {
                imgOption.setImageResource(R.drawable.ic_details);
            }
            else if (items[position].equals("Rename")) {
                imgOption.setImageResource(R.drawable.ic_rename);
            }
            else if (items[position].equals("Delete")) {
                imgOption.setImageResource(R.drawable.ic_delete);
            }
            else if (items[position].equals("Share")) {
                imgOption.setImageResource(R.drawable.ic_share);
            }
            return myView;
        }
    }
}
