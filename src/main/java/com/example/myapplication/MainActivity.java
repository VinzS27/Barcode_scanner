package com.example.myapplication;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Security;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private Runnable runnable;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private EditText editText,etCodice;
    private ListView listView;
    private Button aggiungi, conferma;
    private ImageButton barcodescanner, unsaved, reload_page;
    private ImageView imageView;
    private Deque<String> list;
    private ArrayAdapter<String> arrayAdapter;
    private AlertDialog.Builder alertDialog;
    private final String filename = "dati.txt";
    private final String ip_filename = "Ip.txt";
    private String savelist = "";
    private boolean connection;
    protected String ip = "";
    protected String file_text = "";

    private String[] permission = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        etCodice = findViewById(R.id.codici);
        editText = findViewById(R.id.quantità);
        listView = findViewById(R.id.lista);
        aggiungi = findViewById(R.id.aggiungi);
        conferma = findViewById(R.id.conferma);
        barcodescanner = findViewById(R.id.scanner);
        imageView = findViewById(R.id.wifi);
        unsaved = findViewById(R.id.file_unsaved);
        reload_page = findViewById(R.id.reload);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open_nav,R.string.close_nav);
        alertDialog = new AlertDialog.Builder(this);
        list = new LinkedList<>();
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, (List<String>) list);

        listView.setAdapter(arrayAdapter);

        Security.addProvider(new BouncyCastleProvider());
        isStoragePermissionGranted();
        //Permission();

        load(ip_filename);
        load(filename);
        if(!file_text.equals("")) {
            savelist = file_text;
            unsaved.setVisibility(View.VISIBLE);
        }else{
            unsaved.setVisibility(View.INVISIBLE);
        }

        //check wifi signal
        handler = new Handler();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String s = ConnectionQuality();
                if(s.equals("GOOD")) {
                    imageView.setVisibility(View.INVISIBLE);
                    connection = true;
                }
                //Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 5000);
            }
        };
        //Start
        handler.postDelayed(runnable, 2000);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_setting:{
                    //Toast.makeText(MainActivity.this, "setting", Toast.LENGTH_SHORT).show();
                    openIpActivity();
                    break;
                }
                case R.id.nav_info:{
                    drawerLayout.closeDrawer(GravityCompat.START);
                    //Toast.makeText(MainActivity.this, "info", Toast.LENGTH_SHORT).show();
                    openInfoActivity();
                    break;
                }
            }
            return false;
        });

        unsaved.setOnClickListener(view -> {
            if(!connection){
                alertDialog.setTitle("Attenzione").setMessage("Hai un file non salvato, riprova collegandoti al Wi-fi")
                        .setCancelable(true)
                        .setNegativeButton("Ok", (dialogInterface, i) -> dialogInterface.cancel()).setPositiveButton("", (dialogInterface, i) -> dialogInterface.cancel()).show();
            }else{
                alertDialog.setTitle("Conferma").setMessage("Rete disponibile, inviare il file?")
                        .setCancelable(true)
                        .setPositiveButton("Si", (dialogInterface, i) -> {
                            dialogInterface.cancel();
                            UploadFile();
                        }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel()).show();
            }
        });

        barcodescanner.setOnClickListener(view -> scanCode());
        aggiungi.setOnClickListener(view -> {
            if(etCodice.length() > 0 && editText.length() > 0) {
                savelist += etCodice.getText().toString() + ";" + editText.getText().toString() + "\n";
                list.addFirst(etCodice.getText().toString() + " - " + editText.getText().toString());
                arrayAdapter.notifyDataSetChanged(); //reload list
                editText.setText("1");
                etCodice.setText("");
            }else{
                Toast.makeText(MainActivity.this, "Inserisci un codice e una quantità", Toast.LENGTH_SHORT).show();
            }
        });
        conferma.setOnClickListener(view -> alertDialog.setTitle("Attenzione").setMessage("Sicuro di voler salvare?")
                .setCancelable(true)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    save(filename, savelist);
                    if(connection) {
                        UploadFile();
                    }else{
                        alertDialog.setTitle("Nessuna connessione.").setMessage("Collegati al WI-FI per salvare la lista.")
                                .setCancelable(true)
                                .setNegativeButton("OK", (dialogInterface1, i1) -> dialogInterface1.cancel())
                                .setPositiveButton("", (dialogInterface12, i12) -> dialogInterface12.cancel())
                                .show();
                        unsaved.setVisibility(View.VISIBLE);
                    }
                }).show());

        reload_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                arrayAdapter.notifyDataSetChanged();
                savelist = "";
            }
        });
    }

    public void openInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    public void openIpActivity() {
        Intent intent = new Intent(this, IpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    public String ConnectionQuality() {
        Context context = getBaseContext();
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return "UNKNOWN";
        }
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            if (level >= 2) {
                connection = true;
                imageView.setVisibility(View.INVISIBLE);
                return "GOOD";
            }else{
                connection = false;
                imageView.setVisibility(View.VISIBLE);
                return "UNKNOWN";}
        }else
            return "UNKNOWN";
    }

    public NetworkInfo getInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    imageView.setVisibility(View.INVISIBLE);
                    connection = true;
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    imageView.setVisibility(View.VISIBLE);
                    connection = false;
                    break;
            }
        }
    };

    private void UploadFile() {
        UploadTask uploadTask=new UploadTask();
        String path = getFilesDir().getAbsolutePath() + "/" + filename;
        uploadTask.execute(new String[]{savelist});
    }

    public class UploadTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equalsIgnoreCase("true")) {
                unsaved.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Errore, riprovare.", Toast.LENGTH_SHORT).show();
                alertDialog.setTitle("Errore").setMessage("Errore sconosciuto.\nPossibile Ip errato.")
                        .setCancelable(true)
                        .setNegativeButton("OK", (dialogInterface1, i1) -> dialogInterface1.cancel())
                        .setPositiveButton("", (dialogInterface12, i12) -> dialogInterface12.cancel())
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "Scansioni salvate.", Toast.LENGTH_SHORT).show();
                unsaved.setVisibility(View.INVISIBLE);
                File dir = getFilesDir();
                File file = new File(dir, filename);
                file.delete();
                list.clear();
                arrayAdapter.notifyDataSetChanged();
                savelist = "";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (uploadFile(strings[0])) {
                return "true";
            } else {
                return "failed";
            }
        }

        private boolean uploadFile(String path1) {
            SMBClient client = new SMBClient();
            try (Connection connection = client.connect(ip)) {
                AuthenticationContext ac = new AuthenticationContext("","no password".toCharArray(),"");
                Session session = connection.authenticate(ac);

                // Connect to Share
                try (DiskShare share = (DiskShare) session.connectShare("Uploads")) {
                    for (FileIdBothDirectoryInformation f : share.list("", "*.*")) {
                        System.out.println("File : " + f.getFileName());
                    }

                    Set<FileAttributes> fileAttributes = new HashSet<>();
                    fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
                    Set<SMB2CreateOptions> createOptions = new HashSet<>();
                    createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);
                    com.hierynomus.smbj.share.File f = share.openFile("" + "\\" + filename,
                            new HashSet(Arrays.asList(new AccessMask[]{AccessMask.GENERIC_ALL})),
                            fileAttributes, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, createOptions);

                    OutputStream oStream = f.getOutputStream();
                    oStream.write(savelist.getBytes());
                    oStream.flush();
                    oStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission
                    (Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Permission is granted
                return true;
            } else {
                //Permission is revoked
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
                return false;
            }
        } else {
            return true;
        }
    }       // pre-control for permission

    public void save(String filename, String text) {  // save to internal
            if (!text.equals("")) {
                FileOutputStream fos;
                try {
                    fos = openFileOutput(filename, MODE_PRIVATE);
                    fos.write(text.getBytes());
                    //Toast.makeText(this, "Salvato in:" + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                editText.setText("");
                //Toast.makeText(MainActivity.this, "Salvataggio...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Nessuna scansione effettuata.", Toast.LENGTH_SHORT).show();
            }
        }
        //save to internal

    public void load(String filename) { //read from internal
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text ;

            if(filename.equals(ip_filename)) {
                while ((text = br.readLine()) != null) {
                    sb.append(text);
                }
                ip = sb.toString();
                Toast.makeText(MainActivity.this, "Ip trovato: " + ip, Toast.LENGTH_SHORT).show();
            }else {
                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                file_text = sb.toString();
                Toast.makeText(MainActivity.this, "Trovato file:\n" + file_text, Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            if(filename.equals(ip_filename))
                Toast.makeText(MainActivity.this, "Ip non trovato.", Toast.LENGTH_SHORT).show();
            /*else
                Toast.makeText(MainActivity.this, "Nessun file in coda.", Toast.LENGTH_SHORT).show();*/
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }       //read from internal

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(ScanActivity.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            etCodice.setText(result.getContents());
        }
    });
}