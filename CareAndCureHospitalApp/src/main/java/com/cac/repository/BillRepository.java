package com.cac.repository;

import com.cac.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    Bill findByBillId(int billId);
}
