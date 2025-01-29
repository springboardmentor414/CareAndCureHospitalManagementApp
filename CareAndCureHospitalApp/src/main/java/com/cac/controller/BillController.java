package com.cac.controller;

import com.cac.model.Bill;
import com.cac.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @CrossOrigin(origins = "http://localhost:8082")
    @GetMapping("/{billId}")
    public Bill getBillByBillId(@PathVariable int billId) {
        return billService.findByBillId(billId);
    }
}
