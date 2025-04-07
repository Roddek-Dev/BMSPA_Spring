package com.sena.barberspa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.repository.IAgendamientosRepository;

@Service
public class AgendamientosServiceImplement implements IAgendamientosService {

	@Autowired
	private IAgendamientosRepository agendamientoRepository;

	@Override
	public Agendamiento save(Agendamiento agendamiento) {
		return agendamientoRepository.save(agendamiento);
	}

	@Override
	public Optional<Agendamiento> get(Integer id) {
		return agendamientoRepository.findById(id);
	}

	@Override
	public void update(Agendamiento agendamiento) {
		agendamientoRepository.save(agendamiento);
	}

	@Override
	public void delete(Integer id) {
		agendamientoRepository.deleteById(id);
	}

	@Override
	public List<Agendamiento> findAll() {
		return agendamientoRepository.findAll();
	}

	@Override
	public List<Agendamiento> findByUsuario(Usuario usuario) {
		return agendamientoRepository.findByUsuario(usuario);
	}
}