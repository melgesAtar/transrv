package br.com.modware.transrv.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employee_wa_contact", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_wa_contact", columnNames = {"employee_id", "wa_contact_id"})
})
public class EmployeeWAContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wa_contact_id", nullable = false)
    private WAContact waContact;

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public WAContact getWaContact() { return waContact; }
    public void setWaContact(WAContact waContact) { this.waContact = waContact; }
}


