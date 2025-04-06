package com.sena.barberspa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sucursales")
public class Sucursal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;
	private String imagen;
	private String direccion;
	private String link_maps;
	private String horarios; // Considera cambiarlo por una estructura más detallada si es necesario
	private String ciudad;
	
	// Constructor vacío
	public Sucursal() {
	}

	// Constructor con parámetros
	public Sucursal(Integer id, String nombre, String imagen, String direccion, String link_maps, String horarios, String ciudad) {
		this.id = id;
		this.nombre = nombre;
		this.imagen = imagen;
		this.direccion = direccion;
		this.link_maps = link_maps;
		this.horarios = horarios;
		this.ciudad = ciudad;
	}

	// Getters y Setters
	

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getLink_maps() {
		return link_maps;
	}

	public void setLink_maps(String link_maps) {
		this.link_maps = link_maps;
	}

	public String getHorarios() {
		return horarios;
	}

	public void setHorarios(String horarios) {
		this.horarios = horarios;
	}

	// Método toString para depuración
	@Override
	public String toString() {
		return "Sucursal [id=" + id + ", nombre=" + nombre + ", imagen=" + imagen + ", direccion=" + direccion
				+ ", link_maps=" + link_maps + ", horarios=" + horarios + ", ciudad=" + ciudad + "]";
	}
}
