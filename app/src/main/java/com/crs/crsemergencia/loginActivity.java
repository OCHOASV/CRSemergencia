package com.crs.crsemergencia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class loginActivity extends AppCompatActivity {

    // Controls
    private EditText tbLoginTelefono, tbLoginPassword;
    private TextView registrate;
    private ProgressDialog progress;
    private Button btnIngresar;
    private SharedPreferences sessionUser;

    // Necesario de Volley para las consultas
    RequestQueue rq;
    StringRequest srq;

    // Key o ID de las preferences, por lo general se coloca el nombre del paquete
    private static final String preferecesKey = "crsemergencia";
    // Nombre de la preference
    private static final String preferecesSession = "sessionUser";
    private static final String preferecesID = "sessionID";
    private static final String preferecesPhone = "sessionPhone";
    private static final String preferecesName = "sessionName";
    private static final String preferecesEmail = "sessionEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Manejo toda la logica de verificar si hay sesion iniciada
        if (getSessionPreference()){
            sessionUser = getSharedPreferences(preferecesKey, MODE_PRIVATE);
            Integer sessionID = sessionUser.getInt(preferecesID, 0);
            String sessionTelefono = sessionUser.getString(preferecesPhone,"");
            String sessionNombre = sessionUser.getString(preferecesName, "");
            String sessionEmail = sessionUser.getString(preferecesEmail, "");

            userParcelable userParcelable = new userParcelable();
            userParcelable.setId(sessionID);
            userParcelable.setTelefono(sessionTelefono);
            userParcelable.setNombre(sessionNombre);
            userParcelable.setEmail(sessionEmail);

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("DATA_USER",userParcelable);
            startActivity(intent);
            finish();
        }

        // Castings
        registrate = (TextView)findViewById(R.id.tvRegistrate);
        tbLoginTelefono = (EditText)findViewById(R.id.tbLoginTelefono);
        tbLoginPassword = (EditText)findViewById(R.id.tbLoginPassword);
        btnIngresar = (Button)findViewById(R.id.btnIngresar);
        rq = Volley.newRequestQueue(this);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingresar();
            }
        });

        registrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),registerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });
    }

    // Variables para guardar las SharedPreferences
    Integer userParceID;
    String userParcePhone = "";
    String userParceNombre = "";
    String userParceEmail = "";

    private void ingresar(){
        if (!validar()) return;
        // Dialogo
        progress = new ProgressDialog(this);
        progress.setTitle("CRS Emergencia...");
        progress.setCancelable(false);
        progress.setIcon(R.mipmap.ic_launcher);
        progress.setMessage("Ingresando...");
        progress.show();

        String url = "https://www.simcrs.org.sv/crs/emergencias/userAppLogin.php?";
        srq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Instanciamos userParcelable en userData
                userParcelable userData = new userParcelable();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.names().get(0).equals("success")) {
                        // Llenamos userData
                        userData.setId(jsonObject.getJSONArray("usuario").getJSONObject(0).getInt("id"));
                        userData.setTelefono(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("telefono"));
                        userData.setNombre(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("nombre"));
                        userData.setEmail(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("email"));

                        // Set variables
                        userParceID = userData.getId();
                        userParcePhone = userData.getTelefono();
                        userParceNombre = userData.getNombre();
                        userParceEmail = userData.getEmail();

                        // Guardo las preferencias
                        saveSessionPreference();

                        Toast.makeText(getApplicationContext(), jsonObject.getString("success"), Toast.LENGTH_LONG).show();
                        progress.dismiss();

                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("DATA_USER",userData);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),jsonObject.getString("error"),Toast.LENGTH_SHORT).show();
                        // Log.i("RESPUESTA JSON: ",""+jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No se ha podido conectar con el Servidor...", Toast.LENGTH_LONG).show();
                // Log.i("ERROR: ",""+error.toString());
                progress.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //para enviar los datos mediante POST
                String sTelefono = tbLoginTelefono.getText().toString();
                String sPassword = tbLoginPassword.getText().toString();

                Map<String, String> parametros = new HashMap<>();
                parametros.put("telefono", sTelefono);
                parametros.put("password", sPassword);

                return parametros;
            }
        };

        rq.add(srq);
    }

    private boolean validar(){
        boolean valid = true;
        String sTelefono = tbLoginTelefono.getText().toString();
        String sPassword =  tbLoginPassword.getText().toString();

        if (sTelefono.isEmpty() || sTelefono.length() != 8) {
            tbLoginTelefono.setError("Ingrese un CELULAR valido, esto es MUY IMPORTANTE");
            valid = false;
        } else {
            tbLoginTelefono.setError(null);
        }

        if (sPassword.isEmpty() || tbLoginPassword.length() < 4) {
            tbLoginPassword.setError("Ingrese mas de 4 caracteres");
            valid = false;
        } else {
            tbLoginPassword.setError(null);
        }

        return valid;
    }

    // Guardaremos las SharedPreferences
    public void saveSessionPreference(){
        sessionUser  = getSharedPreferences(preferecesKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sessionUser.edit();
        // Guardaremos true para definir que en edecto hay una sesion
        editor.putBoolean(preferecesSession, true);
        editor.putInt(preferecesID, userParceID);
        editor.putString(preferecesPhone, userParcePhone);
        editor.putString(preferecesName, userParceNombre);
        editor.putString(preferecesEmail, userParceEmail);
        editor.commit();
    }

    // Recuperaremos el estado, pero por defecto al inicio sera false
    public boolean getSessionPreference(){
        sessionUser = getSharedPreferences(preferecesKey,MODE_PRIVATE);
        return sessionUser.getBoolean(preferecesSession, false);
    }
}
