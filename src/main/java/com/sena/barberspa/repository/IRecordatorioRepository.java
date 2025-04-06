package com.sena.barberspa.repository;

import com.sena.barberspa.model.Recordatorio;
import com.sena.barberspa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRecordatorioRepository extends JpaRepository<Recordatorio, Integer> {

    List<Recordatorio> findByUsuarioAndActivoTrueOrderByFijadoDescFechaHoraAsc(Usuario usuario);

    @Query("SELECT r FROM Recordatorio r WHERE r.usuario = ?1 AND r.activo = true AND r.fechaHora BETWEEN ?2 AND ?3 ORDER BY r.fijado DESC, r.fechaHora ASC")
    List<Recordatorio> findProximosRecordatorios(Usuario usuario, LocalDateTime inicio, LocalDateTime fin);

    List<Recordatorio> findByAgendamientoIdAndActivoTrue(Integer agendamientoId);
}