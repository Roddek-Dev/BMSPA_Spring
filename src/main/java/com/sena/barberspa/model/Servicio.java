package com.sena.barberspa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String descripcion;

    private String imagen;

    private String nombre;

    private Double precio; // Cambiado a Double para representar valores numéricos correctamente

    private Integer duracion; // Cambiado a Integer para manejar la duración como minutos (por ejemplo)

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false) // Llave foránea hacia la tabla usuarios
    private Usuario usuario;

    // Constructor vacío
    public Servicio() {
    }

    // Constructor con parámetros
    public Servicio(Integer id, String descripcion, String imagen, String nombre, Double precio, Integer duracion,
                    Usuario usuario) {
        this.id = id;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.nombre = nombre;
        this.precio = precio;
        this.duracion = duracion;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", imagen='" + imagen + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", duracion=" + duracion +
                ", usuario=" + usuario +
                '}';
    }
}
