package com.crs.crsemergencia;

import android.os.Parcel;
import android.os.Parcelable;

// User Data

public class userParcelable implements Parcelable {
    private int id;
    private String telefono;
    private String nombre;
    private String pass;
    private String email;

    protected userParcelable(Parcel in) {
        id = in.readInt();
        telefono = in.readString();
        nombre = in.readString();
        pass = in.readString();
        email = in.readString();
    }

    public static final Creator<userParcelable> CREATOR = new Creator<userParcelable>() {
        @Override
        public userParcelable createFromParcel(Parcel in) {
            return new userParcelable(in);
        }

        @Override
        public userParcelable[] newArray(int size) {
            return new userParcelable[size];
        }
    };

    public userParcelable() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(telefono);
        parcel.writeString(nombre);
        parcel.writeString(pass);
        parcel.writeString(email);
    }
}