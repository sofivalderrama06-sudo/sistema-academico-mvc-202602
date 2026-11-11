package com.uniajc.vista;
 
/**
 * Contrato que cumplen todos los paneles de la ventana principal.
 * Asi la ventana principal puede pedirle a cualquiera que se refresque
 * con los datos mas recientes antes de mostrarse.
 */
public interface Vista {
 
    void actualizarPanel();
}
