package com.example.electrical.repository;
import com.example.electrical.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findByWorkRequestCustomerIdOrderByCreatedAtDesc(Long customerId);
    boolean existsByWorkRequestId(Long requestId);
}
