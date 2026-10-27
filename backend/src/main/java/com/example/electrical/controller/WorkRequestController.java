package com.example.electrical.controller;

import com.example.electrical.entity.User;
import com.example.electrical.entity.WorkRequest;
import com.example.electrical.repository.UserRepository;
import com.example.electrical.repository.WorkRequestRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class WorkRequestController {
    private final WorkRequestRepository requests;
    private final UserRepository users;

    public WorkRequestController(WorkRequestRepository requests, UserRepository users) {
        this.requests = requests; this.users = users;
    }

    record WorkRequestInput(@NotBlank @Size(max=120) String workType,
                        @Size(max=80) String propertyType,
                        @Size(max=300) String address,
                        @NotBlank @Size(max=3000) String description,
                        LocalDate requiredDate) {}

    @PostMapping("/api/customer/work-requests")
    WorkRequest create(@Valid @RequestBody WorkRequestInput body, Authentication auth) {
        User customer = users.findByEmail(auth.getName()).orElseThrow();
        WorkRequest request = new WorkRequest();
        request.setCustomer(customer);
        request.setWorkType(body.workType().trim());
        request.setPropertyType(body.propertyType());
        request.setAddress(body.address());
        request.setDescription(body.description().trim());
        request.setRequiredDate(body.requiredDate());
        request.setStatus("NEW");
        return requests.save(request);
    }

    @GetMapping("/api/customer/work-requests")
    List<?> customerRequests(Authentication auth) {
        User customer = users.findByEmail(auth.getName()).orElseThrow();
        return requests.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream().map(this::dto).toList();
    }

    @GetMapping("/api/admin/work-requests")
    List<?> allRequests() {
        return requests.findAll().stream().map(this::dto).toList();
    }

    @PutMapping("/api/admin/work-requests/{id}/status")
    WorkRequest updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "");
        if (!List.of("NEW", "REVIEWING", "QUOTATION_SENT", "ACCEPTED", "REJECTED", "COMPLETED").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        WorkRequest request = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        request.setStatus(status);
        return requests.save(request);
    }

private Map<String, Object> dto(WorkRequest r) {
    Map<String, Object> response = new java.util.LinkedHashMap<>();

    response.put("id", r.getId());
    response.put("customerId", r.getCustomer().getId());
    response.put("customerName", r.getCustomer().getFullName());
    response.put("customerEmail", r.getCustomer().getEmail());
    response.put("customerPhone", r.getCustomer().getPhone());
    response.put("workType", r.getWorkType());
    response.put("propertyType",
            r.getPropertyType() == null ? "" : r.getPropertyType());
    response.put("address",
            r.getAddress() == null ? "" : r.getAddress());
    response.put("description", r.getDescription());
    response.put("requiredDate",
            r.getRequiredDate() == null ? "" : r.getRequiredDate().toString());
    response.put("status", r.getStatus());
    response.put("createdAt",
            r.getCreatedAt() == null ? "" : r.getCreatedAt().toString());

    return response;
}
}
