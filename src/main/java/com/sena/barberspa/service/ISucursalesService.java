package com.sena.barberspa.service;

import java.util.List;
import java.util.Optional;

import com.sena.barberspa.model.Sucursal;

public interface ISucursalesService {
	public Sucursal save(Sucursal sucursal);

	public Optional<Sucursal> get(Integer id);

	public void update(Sucursal sucursal);

	public void delete(Integer id);

	public List<Sucursal> findAll();
	
	//public List<Sucursal> findbyNombre(String Nombre);
}
