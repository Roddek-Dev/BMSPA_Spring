package com.sena.barberspa.service;

import java.util.List;
import java.util.Optional;

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Usuario;

public interface IOrdenService {

	public Orden save(Orden orden);
	
    public Orden update(Orden orden);  // New update method

	public List<Orden> findAll();

	public String generarNumeroOrden();

	public List<Orden> findByUsuario(Usuario usuario);

	public Optional<Orden> findById(Integer id);

}
