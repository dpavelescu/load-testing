package com.loadtesting.controller;

import com.loadtesting.service.MemorySimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for MemorySimulationController
 */
@WebMvcTest(MemorySimulationController.class)
class MemorySimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemorySimulationService memorySimulationService;

    @Test
    void testGenerateAndCache() throws Exception {
        // Given
        when(memorySimulationService.generateAndCache(anyString(), anyInt(), anyInt()))
                .thenReturn("Generated and cached 100 employees with string size 512 bytes. Cache key: test-key. Will be cleaned up in 30 seconds.");

        // When & Then
        mockMvc.perform(post("/api/memory/cache")
                        .param("cacheKey", "test-key")
                        .param("count", "100")
                        .param("stringSize", "512")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Generated and cached 100 employees with string size 512 bytes. Cache key: test-key. Will be cleaned up in 30 seconds."));
    }

    @Test
    void testForceGarbageCollection() throws Exception {
        // Given
        when(memorySimulationService.forceGarbageCollection())
                .thenReturn("Garbage collection requested. Memory before: 123456 bytes, after: 98765 bytes, freed: 24691 bytes");

        // When & Then
        mockMvc.perform(post("/api/memory/gc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garbage collection requested. Memory before: 123456 bytes, after: 98765 bytes, freed: 24691 bytes"));
    }

    @Test
    void testClearCache() throws Exception {
        // Given
        when(memorySimulationService.clearCache())
                .thenReturn("Cleared 5 cache entries");

        // When & Then
        mockMvc.perform(delete("/api/memory/cache")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cleared 5 cache entries"));
    }

    @Test
    void testGetAvailableScenarios() throws Exception {
        // Given
        when(memorySimulationService.getAvailableScenarios())
                .thenReturn(Arrays.asList("small", "medium", "large"));

        // When & Then
        mockMvc.perform(get("/api/memory/scenarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("small"))
                .andExpect(jsonPath("$[1]").value("medium"))
                .andExpect(jsonPath("$[2]").value("large"));
    }

    @Test
    void testGenerateByScenario() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/memory/scenario/medium")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
