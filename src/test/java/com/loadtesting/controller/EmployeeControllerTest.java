package com.loadtesting.controller;

import com.loadtesting.model.Employee;
import com.loadtesting.service.EmployeeDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EmployeeController REST endpoints.
 * Uses pure Mockito without Spring Boot test annotations to avoid deprecation warnings.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    
    @Mock
    private EmployeeDataService employeeDataService;
    
    private MockMvc mockMvc;
    private Employee sampleEmployee;
    private Employee engineeringManager;
    private List<Employee> sampleEmployees;
    
    @BeforeEach
    void setUp() {
        // Set up MockMvc with the controller
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeController(employeeDataService)).build();
        
        sampleEmployee = new Employee(1L, "John", "Doe", "john.doe@example.com", 
                "Software Engineer", "Engineering", 75000.0, LocalDate.now(), "test-data");
        
        engineeringManager = new Employee(2L, "Jane", "Smith", "jane.smith@example.com", 
                "Manager", "Engineering", 85000.0, LocalDate.now(), "test-data");
        
        sampleEmployees = Arrays.asList(sampleEmployee, engineeringManager);
    }
    
    @Test
    void getAllEmployees_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateEmployees()).thenReturn(sampleEmployees);
        
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void getEmployeesByCount_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateEmployees(eq(50))).thenReturn(sampleEmployees);
        
        mockMvc.perform(get("/api/employees?count=50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void getEmployeesWithMemorySize_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateEmployees(eq(25), eq(2048))).thenReturn(sampleEmployees);
        
        mockMvc.perform(get("/api/employees?count=25&memorySize=2048"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void getEmployeeById_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateSingleEmployee(eq(1L), eq(1024))).thenReturn(sampleEmployee);
        
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void getEmployeesByDepartment_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateEmployees(eq(100), eq(1024))).thenReturn(sampleEmployees);
        
        mockMvc.perform(get("/api/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void getEmployeesByPosition_ShouldReturnOk() throws Exception {
        when(employeeDataService.generateEmployees(eq(100), eq(1024))).thenReturn(sampleEmployees);
        
        mockMvc.perform(get("/api/employees/position/Manager"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        // Health check doesn't use the service, so no mocking needed
        mockMvc.perform(get("/api/employees/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("{\"status\":\"Employee service is running\"}"));
    }
}
