package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanager.Fragments.HomeFragment;
import com.example.filemanager.Fragments.InternalFragment;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private TextView fragmentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // After start, open home fragment and then set check nav_home
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                new HomeFragment()
        ).commit();
        navigationView.setCheckedItem(R.id.nav_home);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                HomeFragment homeFragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        homeFragment
                ).addToBackStack(null).commit();
                break;
            case R.id.nav_internal:
                InternalFragment internalFragment = new InternalFragment();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        internalFragment
                ).addToBackStack(null).commit();
                break;
            case R.id.nav_about:
                final TextView textView = new TextView(this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Group Name: Insomnia");
                builder.setView(textView);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(16);
                textView.setPadding(30, 30, 30, 30);
                textView.setText(
                        "นายธนภัทร จีนสำราญ 6306021611060 Sec B\n" +
                        "นายอชิรวิชญ์ แสนใจกล้า 6306021621065 Sec B\n" +
                        "นายภัทรพล ไกรทอง 6306021611043 Sec B\n" +
                        "นายวรวิช พิริยโยธากุล 6306021611086 Sec B\n" +
                        "นายนวพรรษ นกงาม 6306021620018 Sec A\n" +
                        "นายสันติเมธ ทวีปะ 6306021610047 Sec A"
                );

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog_credit = builder.create();
                alertDialog_credit.show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
//        getSupportFragmentManager().popBackStackImmediate();

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
        super.onBackPressed();
    }
}