package com.sena.barberspa.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sena.barberspa.model.Sucursal;


@Repository
public interface ISucursalesRepository extends JpaRepository<Sucursal, Integer> {
	//List<Sucursal>findbyNombre(String Nombre);
}
