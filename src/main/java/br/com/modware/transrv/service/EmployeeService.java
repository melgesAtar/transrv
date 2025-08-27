package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class EmployeeService {

    boolean isEmployeeAvailable(Employee e, LocalDateTime now) {
        return true;
    }


}
