package com.sena.barberspa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.repository.IOrdenRepository;

@Service
public class OrdenServiceImplement implements IOrdenService {

	@Autowired
	private IOrdenRepository ordenRepository;

	@Override
	public Orden save(Orden orden) {
		// TODO Auto-generated method stub
		return ordenRepository.save(orden);
	}

	@Override
	public Orden update(Orden orden) {
		// Verify if the orden exists
		Optional<Orden> existingOrden = ordenRepository.findById(orden.getId());
		if (existingOrden.isPresent()) {
			return ordenRepository.save(orden); // JPA's save performs update when ID exists
		}
		return null; // or throw an exception
	}

	@Override
	public List<Orden> findAll() {
		// TODO Auto-generated method stub
		return ordenRepository.findAll();
	}

	@Override
	public String generarNumeroOrden() {

		int numero = 0;

		String numeroConcatenado = "";

		List<Orden> ordenes = findAll();

		List<Integer> numeros = new ArrayList<Integer>();

		// funciones java 8
		// una variable anonima
		ordenes.stream().forEach(o -> numeros.add(Integer.parseInt(o.getNumero())));

		// validacion
		if (ordenes.isEmpty()) {
			numero = 1;
		} else {
			numero = numeros.stream().max(Integer::compare).get();
			numero++;

		}

		if (numero < 10) {
			numeroConcatenado = "000000000000" + String.valueOf(numero);
		} else if (numero < 100) {
			numeroConcatenado = "00000000000" + String.valueOf(numero);
		} else if (numero < 1000) {
			numeroConcatenado = "0000000000" + String.valueOf(numero);
		}

		return numeroConcatenado;
	}

	@Override
	public List<Orden> findByUsuario(Usuario usuario) {
		// TODO Auto-generated method stub
		return ordenRepository.findByusuario(usuario);
	}

	@Override
	public Optional<Orden> findById(Integer id) {
		// TODO Auto-generated method stub
		return ordenRepository.findById(id);
	}

}
