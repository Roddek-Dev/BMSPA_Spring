package com.sena.barberspa.service;

import java.util.List;
import java.util.Optional;

import com.sena.barberspa.model.Servicio;

public interface IServiciosService {
	public Servicio save(Servicio servicio);

	public Optional<Servicio> get(Integer id);

	public void update(Servicio servicio);

	public void delete(Integer id);

	public List<Servicio> findAll();
}
