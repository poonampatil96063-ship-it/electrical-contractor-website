package com.example.electrical.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    @GetMapping
    List<Map<String, String>> services() {
        return List.of(
            Map.of("name", "House Wiring", "description", "New wiring, rewiring and electrical fitting."),
            Map.of("name", "Electrical Repairs", "description", "Troubleshooting and repair work."),
            Map.of("name", "Commercial Electrical Work", "description", "Office, shop and commercial installations."),
            Map.of("name", "Lighting Installation", "description", "Indoor, outdoor and security lighting."),
            Map.of("name", "Maintenance and Inspection", "description", "Electrical checks, maintenance and testing.")
        );
    }
}
