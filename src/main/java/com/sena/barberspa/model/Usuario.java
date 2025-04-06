package com.sena.barberspa.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;

	private String imagen;

	private String email;

	private String direccion;

	private String telefono;

	private String tipo; // Puede ser un enum para los roles (ADMIN, USER, etc.)

	private String password;

	private String estado;

	// Relación con Productos
	@OneToMany(mappedBy = "usuario")
	private List<Producto> productos;

	// Relación con Servicios
	@OneToMany(mappedBy = "usuario")
	private List<Servicio> servicios; // Aquí era Producto antes, debería ser Servicio

	// Relación con Ordenes
	@OneToMany(mappedBy = "usuario")
	private List<Orden> ordenes;

	// Constructor vacío
	public Usuario() {
	}

	// Constructor con parámetros
	public Usuario(Integer id, String nombre, String email, String imagen, String direccion, String telefono,
			String tipo, String password, String estado) {
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.imagen = imagen;
		this.direccion = direccion;
		this.telefono = telefono;
		this.tipo = tipo;
		this.password = password;
		this.estado = estado;
	}

	// Getters y setters
	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Producto> getProductos() {
		return productos;
	}

	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}

	public List<Servicio> getServicios() {
		return servicios;
	}

	public void setServicios(List<Servicio> servicios) {
		this.servicios = servicios;
	}

	public List<Orden> getOrdenes() {
		return ordenes;
	}

	public void setOrdenes(List<Orden> ordenes) {
		this.ordenes = ordenes;
	}

	public Object getImagen() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setImagen(Object object) {
		// TODO Auto-generated method stub

	}

	// Método toString para depuración
	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", email=" + email + ", imagen=" + imagen + ", direccion="
				+ direccion + ", telefono=" + telefono + ", tipo=" + tipo + ", password=" + password + ", estado="
				+ estado + "]";
	}

}
