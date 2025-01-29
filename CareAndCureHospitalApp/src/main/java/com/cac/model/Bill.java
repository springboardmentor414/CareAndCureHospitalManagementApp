package com.cac.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;

@Entity
public class Bill {
    @Id
    private int billId;

    @JsonIgnore
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private Set<Payment> payments;

    
   

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }
}
