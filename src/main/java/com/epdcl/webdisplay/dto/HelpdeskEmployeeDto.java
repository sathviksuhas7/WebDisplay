
package com.epdcl.webdisplay.dto;

public class HelpdeskEmployeeDto {

    private Long employeeId;
    private String employeeName;

    public HelpdeskEmployeeDto() {
    }

    public HelpdeskEmployeeDto(Long employeeId, String employeeName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
