package com.sena.barberspa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorios")
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private boolean activo;
    private boolean fijado;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "agendamiento_id")
    private Agendamiento agendamiento;

    public Recordatorio() {
    }

    // Constructor para recordatorios manuales
    public Recordatorio(String titulo, String descripcion, LocalDateTime fechaHora, Usuario usuario) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.activo = true;
        this.fijado = false;
    }

    // Constructor para recordatorios de agendamientos
    public Recordatorio(Agendamiento agendamiento, Usuario usuario) {
        this.titulo = "Cita: " + agendamiento.getServicio().getNombre();
        this.descripcion = "Cliente: " + agendamiento.getUsuario().getNombre() +
                " - Sucursal: " + agendamiento.getSucursal().getNombre();
        this.fechaHora = agendamiento.getFechaHora();
        this.agendamiento = agendamiento;
        this.usuario = usuario;
        this.activo = true;
        this.fijado = false;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isFijado() {
        return fijado;
    }

    public void setFijado(boolean fijado) {
        this.fijado = fijado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Agendamiento getAgendamiento() {
        return agendamiento;
    }

    public void setAgendamiento(Agendamiento agendamiento) {
        this.agendamiento = agendamiento;
    }

    @Override
    public String toString() {
        return "Recordatorio{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaHora=" + fechaHora +
                ", activo=" + activo +
                ", fijado=" + fijado +
                '}';
    }
}