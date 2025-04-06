package com.sena.barberspa.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sena.barberspa.model.Agendamiento;

@Repository
public interface IAgendamientosRepository extends JpaRepository<Agendamiento, Integer> {

}
