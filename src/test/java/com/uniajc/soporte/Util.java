package com.uniajc.soporte;
public final class Util {
    private Util() {
    }
//sirve para repetir un caracter una cantidad de veces y devolverlo como String
    public static String repetir(char caracter, int cantidad) {
        return String.valueOf(caracter).repeat(cantidad);
    }
}
