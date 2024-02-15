package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class IpActivity extends AppCompatActivity {
    private EditText editText;
    private Button button;
    private String Ipfilename = "Ip.txt";
    private String ip = "";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        editText = findViewById(R.id.ipaddress);
        button = findViewById(R.id.salvaip);
        drawerLayout = findViewById(R.id.drawer_layout3);
        navigationView = findViewById(R.id.nav_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open_nav,R.string.close_nav);

        load(Ipfilename);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = editText.getText().toString();
                if(ip == "") {
                    Toast.makeText(IpActivity.this, "Inserisci un IP", Toast.LENGTH_SHORT).show();
                }else{
                    save(Ipfilename, ip);
                    openMainActivity();
                }
            }
        });
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:{
                    //Toast.makeText(MainActivity.this, "home", Toast.LENGTH_SHORT).show();
                    openMainActivity();
                    break;
                }
                case R.id.nav_info:{
                    //Toast.makeText(MainActivity.this, "setting", Toast.LENGTH_SHORT).show();
                    openInfoActivity();
                    break;
                }
            }
            return false;
        });
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    public void load(String filename) { //read from internal
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text);
            }
            editText.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    //Toast.makeText(IpActivity.this, "Ip caricato", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }       //read from internal

    public void save(String filename, String text) {  // save to internal
            if (!text.equals("")) {
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(filename, MODE_PRIVATE);
                    fos.write(text.getBytes());
                    //Toast.makeText(this, "Salvato in:" + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(IpActivity.this, "Ip salvato.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(IpActivity.this, "Errore", Toast.LENGTH_SHORT).show();
            }
        }//save to internal
}