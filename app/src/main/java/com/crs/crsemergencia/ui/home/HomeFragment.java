package com.crs.crsemergencia.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crs.crsemergencia.MainActivity;
import com.crs.crsemergencia.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getSystemService;

public class HomeFragment extends Fragment implements LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private HomeViewModel homeViewModel;
    public FusedLocationProviderClient fusedLocationProviderClient;
    // Necesario de Volley para las consultas
    RequestQueue rq;
    String idUser, AddressDescription, CoordinateLink;
    Double Lat, Lon;
    double longitudeGPS, latitudeGPS;
    // Creating Progress dialog.
    ProgressDialog progressDialog;
    AlertDialog Alert;
    // Storing server url into String variable.
    String HttpUrl = "https://www.simcrs.org.sv/crs/emergencias/userAppEmergencia.php";
    // Create string variable to hold the EditText Value.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    public static final long UPDATE_INTERVAL = 1000;
    public static final long UPDATE_FASTEST_INTERVAL = UPDATE_INTERVAL / 2;

    private SharedPreferences sessionUser;
    private static final String preferecesKey = "crsemergencia";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);

            }


        });
        locationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Verifcamos permisos
        LocationManager mgr =
                (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        if(!mgr.isProviderEnabled(mgr.GPS_PROVIDER)) {
            ActivarGPS();
        }

        //Tiempo de actualización, 6 segundos y 10 metros.
        mgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1 * 60 * 100, 10, locationListenerGPS);

        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.super.getActivity());
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(HomeFragment.super.getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Lat = location.getLatitude();
                                Lon = location.getLongitude();
                            } else {
                                Toast.makeText(HomeFragment.super.getContext(), "Error  !!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception ex) {
            Toast.makeText(HomeFragment.super.getContext(), "Error  !!!" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Setear botones para llamadad
        ImageButton ButtonCRS = root.findViewById(R.id.ImageButtonCR);
        ImageButton ButtonPolice = root.findViewById((R.id.ImageButtonPNC));
        //String valorRecibido = HomeFragment.super.getArguments().getString("USER");
        sessionUser = this.getActivity().getSharedPreferences(preferecesKey, Context.MODE_PRIVATE);
        //Obtenemos id de las preferencias
        Integer IdUserString = sessionUser.getInt("sessionID", 0);
        idUser = IdUserString.toString();
        // Iniciamos el Volley
        rq = Volley.newRequestQueue(HomeFragment.super.getContext());


        progressDialog = new ProgressDialog(HomeFragment.super.getContext());
        ButtonCRS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Llenar parametros
                setParameters();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String ServerResponse) {
                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();
                                // Showing response message coming from server.
                                // Toast.makeText(MainActivity.this, ServerResponse, Toast.LENGTH_LONG).show();
                                Toast.makeText(HomeFragment.super.getContext(), "Informacion Guardada con Exito !!!", Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();

                                // Showing error message if something goes wrong.
                                // Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                                Toast.makeText(HomeFragment.super.getContext(), "No fue posible Guardar los Datos...", Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Creating Map String Params.
                        Map<String, String> params = new HashMap<String, String>();

                        // Adding All values to Params.
                        params.put("id",idUser );
                        params.put("ubicacion", CoordinateLink);

                        return params;
                    }
                };
                // Creating RequestQueue.
                RequestQueue requestQueue = Volley.newRequestQueue(HomeFragment.super.getContext());

                // Adding the StringRequest object into requestQueue.
                requestQueue.add(stringRequest);
                //Lanzamos llamada
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "+503 76347337"));
                startActivity(intent);
            }
        });

        ButtonPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setParameters();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String ServerResponse) {
                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();

                                // Showing response message coming from server.
                                // Toast.makeText(MainActivity.this, ServerResponse, Toast.LENGTH_LONG).show();

                                Toast.makeText(HomeFragment.super.getContext(), "Informacion Guardada con Exito !!!", Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();

                                // Showing error message if something goes wrong.
                                // Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                                Toast.makeText(HomeFragment.super.getContext(), "No fue posible Guardar los Datos...", Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Creating Map String Params.
                        Map<String, String> params = new HashMap<String, String>();

                        // Adding All values to Params.
                        params.put("id", idUser);
                        params.put("ubicacion", CoordinateLink);
                        return params;
                    }
                };

                // Creating RequestQueue.
                RequestQueue requestQueue = Volley.newRequestQueue(HomeFragment.super.getContext());

                // Adding the StringRequest object into requestQueue.
                requestQueue.add(stringRequest);
                //Lanzamos llamada
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "+503 76347337"));
                startActivity(intent);
            }
        });

        return root;

    }

    private void ActivarGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("El GPS está desactivado, ¿Desea encenderlo?").setCancelable(true)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    };
                });
        Alert = builder.create();
        Alert.show();
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    private void setParameters() {
        //UserName = IdUser;

        AddressDescription = "CASA DE CESAR";
        CoordinateLink = "https://maps.google.com/?q=" + latitudeGPS + "," + longitudeGPS;
    }

    @Override
    public void onLocationChanged(Location location) {
        longitudeGPS = location.getLongitude();
        latitudeGPS = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}