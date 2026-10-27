package com.example.electrical.repository;
import com.example.electrical.entity.WorkRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WorkRequestRepository extends JpaRepository<WorkRequest, Long> {
    List<WorkRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
