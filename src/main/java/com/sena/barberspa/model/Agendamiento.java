package com.sena.barberspa.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamientos")
public class Agendamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime fechaHora;  // Maneja solo fecha, hora y minutos
    private String estado;
    private String mensaje;

    // Relaciones con otras tablas
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false) // Aseguramos que este campo haga referencia correcta al modelo ER
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable = false) // Lo mismo para servicio
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false) // Y sucursal
    private Sucursal sucursal;

    // Constructor vacío
    public Agendamiento() {
    }

    

	// Constructor con parámetros
    public Agendamiento(Integer id, LocalDateTime fechaHora, String estado, Usuario usuario, Servicio servicio, Sucursal sucursal, String mensaje) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.usuario = usuario;
        this.servicio = servicio;
        this.sucursal = sucursal;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    
    public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    // Método toString
    @Override
    public String toString() {
        return "Agendamiento{" +
                "id=" + id +
                ", fechaHora='" + fechaHora + '\'' +
                ", estado='" + estado + '\'' +
                ", usuario=" + usuario +
                ", servicio=" + servicio +
                ", sucursal=" + sucursal +
                ", mensaje=" + mensaje +
                '}';
    }
}
