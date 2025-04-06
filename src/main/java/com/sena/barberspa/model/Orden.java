package com.sena.barberspa.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String numero;

    private Date fechacreacion;

    private Date fecharecibida;

    private Double total;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false) // Relación con la tabla usuarios
    private Usuario usuario;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalle;

    // Constructor vacío
    public Orden() {
    }

    // Constructor con parámetros
    public Orden(Integer id, String numero, Date fechacreacion, Date fecharecibida, Double total, String estado,
            Usuario usuario) {
        this.id = id;
        this.numero = numero;
        this.fechacreacion = fechacreacion;
        this.fecharecibida = fecharecibida;
        this.total = total;
        this.estado = estado;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Date getFechacreacion() {
        return fechacreacion;
    }

    public void setFechacreacion(Date fechacreacion) {
        this.fechacreacion = fechacreacion;
    }

    public Date getFecharecibida() {
        return fecharecibida;
    }

    public void setFecharecibida(Date fecharecibida) {
        this.fecharecibida = fecharecibida;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
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

    public List<DetalleOrden> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleOrden> detalle) {
        this.detalle = detalle;
    }

    @Override
    public String toString() {
        return "Orden{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", fechacreacion=" + fechacreacion +
                ", fecharecibida=" + fecharecibida +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", usuario=" + usuario +
                ", detalle=" + detalle +
                '}';
    }
}
