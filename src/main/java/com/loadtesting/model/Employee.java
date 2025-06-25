package com.loadtesting.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Employee model representing employee data with configurable attributes.
 * This class includes a configurable string field that can be used to simulate
 * different memory consumption scenarios for load testing.
 */
public class Employee {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private String department;
    private Double salary;
    private LocalDate hireDate;
    private String configurableData; // This field size can be configured for memory testing
    
    // Default constructor
    public Employee() {}
    
    // Constructor with all fields
    public Employee(Long id, String firstName, String lastName, String email, 
                   String position, String department, Double salary, 
                   LocalDate hireDate, String configurableData) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
        this.configurableData = configurableData;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Double getSalary() {
        return salary;
    }
    
    public void setSalary(Double salary) {
        this.salary = salary;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public String getConfigurableData() {
        return configurableData;
    }
    
    public void setConfigurableData(String configurableData) {
        this.configurableData = configurableData;
    }
    
    /**
     * Get the approximate memory size of this employee object in bytes.
     * This is useful for memory consumption analysis during load testing.
     */
    public long getApproximateMemorySize() {
        long baseSize = 8 + 8 + 8 + 8 + 8 + 8 + 8 + 8; // Object references (8 bytes each)
        baseSize += (firstName != null ? firstName.length() * 2 : 0);
        baseSize += (lastName != null ? lastName.length() * 2 : 0);
        baseSize += (email != null ? email.length() * 2 : 0);
        baseSize += (position != null ? position.length() * 2 : 0);
        baseSize += (department != null ? department.length() * 2 : 0);
        baseSize += (configurableData != null ? configurableData.length() * 2 : 0);
        baseSize += 8; // Long id
        baseSize += 8; // Double salary
        baseSize += 12; // LocalDate (approximate)
        return baseSize;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) &&
               Objects.equals(firstName, employee.firstName) &&
               Objects.equals(lastName, employee.lastName) &&
               Objects.equals(email, employee.email) &&
               Objects.equals(position, employee.position) &&
               Objects.equals(department, employee.department) &&
               Objects.equals(salary, employee.salary) &&
               Objects.equals(hireDate, employee.hireDate) &&
               Objects.equals(configurableData, employee.configurableData);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, position, 
                          department, salary, hireDate, configurableData);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", configurableDataSize=" + (configurableData != null ? configurableData.length() : 0) + " chars" +
                '}';
    }
}
