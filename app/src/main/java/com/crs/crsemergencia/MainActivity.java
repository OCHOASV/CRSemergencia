package com.crs.crsemergencia;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.crs.crsemergencia.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Para datos del navHeader
    private String idUser;
    private userParcelable user;

    // Key o ID de las preferences, por lo general se coloca el nombre del paquete
    private SharedPreferences sessionUser;
    private static final String preferecesKey = "crsemergencia";

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Recupero SharedPreferences
        sessionUser  = getSharedPreferences(preferecesKey, MODE_PRIVATE);

        // Poner datos del usuario en el navHeader
        View header = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        try {
            Bundle bundle = getIntent().getExtras();
            user = bundle.getParcelable("DATA_USER");
            if (bundle != null){
                idUser = String.valueOf(user.getId());

                ((TextView) header.findViewById(R.id.tvUserHeader)).setText(user.getNombre());
                ((TextView) header.findViewById(R.id.tvEmailHeader)).setText(user.getTelefono());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /*
        * Con este ID es con el que se van a meter las coordenadas, Daniel solo va a enviar
        * el ID y las coordenadas (link) el resto lo hara php
        * El link es: https://www.simcrs.org.sv/crs/emergencias/userAppEmergencia.php
        *
        *
        * Deben borrar el Toast y esto al finalizar (¬_¬)
        * */
        Toast.makeText(this, "ID del usuario => " + user.getId(), Toast.LENGTH_SHORT).show();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();

        HomeFragment homeFragment = new HomeFragment();
        Bundle datos = new Bundle();
        datos.putString("USER", idUser);// Aquí lo he puesto directamente el string
        homeFragment.setArguments(datos);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logOut(){
        deleteSharedPreferences();
        Toast.makeText(this, "Has cerrado tu Sesión " + user.getNombre(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, loginActivity.class);
        // Destruye todo el historial de la activity, esto es seguridad
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void deleteSharedPreferences(){
        sessionUser.edit().clear().apply();
    }

}
