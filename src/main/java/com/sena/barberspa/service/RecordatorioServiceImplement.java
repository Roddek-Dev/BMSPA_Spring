package com.sena.barberspa.service;

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.Recordatorio;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.repository.IRecordatorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioServiceImplement implements IRecordatorioService {

    @Autowired
    private IRecordatorioRepository recordatorioRepository;

    @Autowired
    private IAgendamientosService agendamientosService;

    @Override
    public Recordatorio save(Recordatorio recordatorio) {
        return recordatorioRepository.save(recordatorio);
    }

    @Override
    public Optional<Recordatorio> get(Integer id) {
        return recordatorioRepository.findById(id);
    }

    @Override
    public void update(Recordatorio recordatorio) {
        recordatorioRepository.save(recordatorio);
    }

    @Override
    public void delete(Integer id) {
        recordatorioRepository.deleteById(id);
    }

    @Override
    public void desactivar(Integer id) {
        Optional<Recordatorio> optRecordatorio = recordatorioRepository.findById(id);
        if (optRecordatorio.isPresent()) {
            Recordatorio recordatorio = optRecordatorio.get();
            recordatorio.setActivo(false);
            recordatorioRepository.save(recordatorio);
        }
    }

    @Override
    public void cambiarFijado(Integer id) {
        Optional<Recordatorio> optRecordatorio = recordatorioRepository.findById(id);
        if (optRecordatorio.isPresent()) {
            Recordatorio recordatorio = optRecordatorio.get();
            recordatorio.setFijado(!recordatorio.isFijado());
            recordatorioRepository.save(recordatorio);
        }
    }

    @Override
    public List<Recordatorio> findByUsuario(Usuario usuario) {
        return recordatorioRepository.findByUsuarioAndActivoTrueOrderByFijadoDescFechaHoraAsc(usuario);
    }

    @Override
    public List<Recordatorio> findProximosRecordatorios(Usuario usuario, int dias) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(dias);
        return recordatorioRepository.findProximosRecordatorios(usuario, ahora, limite);
    }

    @Override
    public Recordatorio crearRecordatorioDeAgendamiento(Agendamiento agendamiento, Usuario usuario) {
        // Verificar si ya existe un recordatorio para este agendamiento
        List<Recordatorio> existentes = recordatorioRepository.findByAgendamientoIdAndActivoTrue(agendamiento.getId());
        if (!existentes.isEmpty()) {
            return existentes.get(0); // Devolver el existente si ya hay uno
        }

        // Crear nuevo recordatorio
        Recordatorio recordatorio = new Recordatorio(agendamiento, usuario);
        return recordatorioRepository.save(recordatorio);
    }

    @Override
    public void procesarAgendamientosProximos(Usuario usuario, int dias) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(dias);

        // Obtener todos los agendamientos dentro del rango
        List<Agendamiento> agendamientos = agendamientosService.findAll().stream()
                .filter(a -> a.getFechaHora().isAfter(ahora) && a.getFechaHora().isBefore(limite))
                .toList();

        // Crear recordatorios para cada agendamiento
        for (Agendamiento agendamiento : agendamientos) {
            crearRecordatorioDeAgendamiento(agendamiento, usuario);
        }
    }
}