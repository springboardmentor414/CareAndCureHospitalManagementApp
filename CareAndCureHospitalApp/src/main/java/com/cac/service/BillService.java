package com.cac.service;

import com.cac.model.Bill;
import com.cac.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public Bill findByBillId(int billId) {
        return billRepository.findByBillId(billId);
    }
}
