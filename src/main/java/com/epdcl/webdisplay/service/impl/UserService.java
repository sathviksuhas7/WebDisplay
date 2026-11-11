
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;

public interface UserService {

    User findByUsername(String username);

    User authenticateUser(String username, String password);

    User createCustomerLogin(
            String username,
            String email,
            String password
    );

    User createStaffLogin(
            String username,
            String email,
            String password,
            Role role
    );
}
