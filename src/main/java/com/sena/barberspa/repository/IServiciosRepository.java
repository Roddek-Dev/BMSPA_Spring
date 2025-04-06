package com.sena.barberspa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sena.barberspa.model.Servicio;

@Repository
public interface IServiciosRepository extends JpaRepository<Servicio, Integer> {
	
}
