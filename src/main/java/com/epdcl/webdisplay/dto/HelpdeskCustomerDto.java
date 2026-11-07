
package com.epdcl.webdisplay.dto;

public class HelpdeskCustomerDto {

    private Long customerId;
    private String customerName;
    private String phone;
    private String address;

    public HelpdeskCustomerDto(
            Long customerId,
            String customerName,
            String phone,
            String address) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
