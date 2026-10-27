package com.example.electrical.controller;

import com.example.electrical.entity.Quotation;
import com.example.electrical.entity.WorkRequest;
import com.example.electrical.repository.QuotationRepository;
import com.example.electrical.repository.WorkRequestRepository;
import com.example.electrical.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class QuotationController {
    private final QuotationRepository quotations;
    private final WorkRequestRepository requests;
    private final UserRepository users;

    public QuotationController(QuotationRepository quotations, WorkRequestRepository requests, UserRepository users) {
        this.quotations = quotations; this.requests = requests; this.users = users;
    }

    record QuotationBody(@DecimalMin("0.01") double totalAmount, LocalDate validUntil, String terms) {}

    @PostMapping("/api/admin/quotations/{requestId}")
    Quotation create(@PathVariable Long requestId, @Valid @RequestBody QuotationBody body) {
        if (quotations.existsByWorkRequestId(requestId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A quotation already exists for this request");
        WorkRequest request = requests.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        Quotation q = new Quotation();
        q.setWorkRequest(request);
        q.setQuotationNumber("QT-" + request.getId() + "-" + System.currentTimeMillis());
        q.setTotalAmount(body.totalAmount());
        q.setValidUntil(body.validUntil());
        q.setTerms(body.terms());
        q.setStatus("SENT");
        request.setStatus("QUOTATION_SENT");
        requests.save(request);
        return quotations.save(q);
    }

    @GetMapping("/api/admin/quotations")
    List<?> allQuotations() {
        return quotations.findAll().stream().map(this::dto).toList();
    }

    @GetMapping("/api/customer/quotations")
    List<?> customerQuotations(Authentication auth) {
        var user = users.findByEmail(auth.getName()).orElseThrow();
        return quotations.findByWorkRequestCustomerIdOrderByCreatedAtDesc(user.getId()).stream().map(this::dto).toList();
    }

    @PutMapping("/api/customer/quotations/{id}/decision")
    Quotation decision(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        var user = users.findByEmail(auth.getName()).orElseThrow();
        Quotation q = quotations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.getWorkRequest().getCustomer().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This quotation does not belong to you");
        String decision = body.getOrDefault("decision", "");
        if (!List.of("ACCEPTED", "REJECTED").contains(decision))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be ACCEPTED or REJECTED");
        q.setStatus(decision);
        q.getWorkRequest().setStatus(decision);
        requests.save(q.getWorkRequest());
        return quotations.save(q);
    }

private Map<String, Object> dto(Quotation q) {
    WorkRequest r = q.getWorkRequest();

    Map<String, Object> response = new java.util.LinkedHashMap<>();

    response.put("id", q.getId());
    response.put("quotationNumber", q.getQuotationNumber());
    response.put("requestId", r.getId());
    response.put("customerId", r.getCustomer().getId());
    response.put("customerName", r.getCustomer().getFullName());
    response.put("workType", r.getWorkType());
    response.put("totalAmount", q.getTotalAmount());
    response.put("validUntil",
            q.getValidUntil() == null ? "" : q.getValidUntil().toString());
    response.put("terms", q.getTerms() == null ? "" : q.getTerms());
    response.put("status", q.getStatus());
    response.put("createdAt",
            q.getCreatedAt() == null ? "" : q.getCreatedAt().toString());

    return response;
}
}
