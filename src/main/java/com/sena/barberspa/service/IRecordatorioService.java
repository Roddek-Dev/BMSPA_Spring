package com.sena.barberspa.service;

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.Recordatorio;
import com.sena.barberspa.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRecordatorioService {
    Recordatorio save(Recordatorio recordatorio);

    Optional<Recordatorio> get(Integer id);

    void update(Recordatorio recordatorio);

    void delete(Integer id);

    void desactivar(Integer id);

    void cambiarFijado(Integer id);

    List<Recordatorio> findByUsuario(Usuario usuario);

    List<Recordatorio> findProximosRecordatorios(Usuario usuario, int dias);

    Recordatorio crearRecordatorioDeAgendamiento(Agendamiento agendamiento, Usuario usuario);

    void procesarAgendamientosProximos(Usuario usuario, int dias);
}