package com.example.electrical.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
public class Quotation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private WorkRequest workRequest;
    @Column(nullable = false) private String quotationNumber;
    @Column(nullable = false) private double totalAmount;
    private LocalDate validUntil;
    @Column(length = 3000) private String terms;
    @Column(nullable = false) private String status = "SENT";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public WorkRequest getWorkRequest() { return workRequest; }
    public void setWorkRequest(WorkRequest workRequest) { this.workRequest = workRequest; }
    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
