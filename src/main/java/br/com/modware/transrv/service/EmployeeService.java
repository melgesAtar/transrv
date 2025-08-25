package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class EmployeeService {

    boolean isEmployeeAvailable(Employee e, LocalDateTime now) {
        if (e.getEnterTime() == null || e.getExitTime() == null) {
            return true; // se não configurado, considera sempre disponível
        }

        LocalTime horaAtual = now.toLocalTime();
        return !horaAtual.isBefore(e.getEnterTime()) && !horaAtual.isAfter(e.getExitTime());
    }

}
