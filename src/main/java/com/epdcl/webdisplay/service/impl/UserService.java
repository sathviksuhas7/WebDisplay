package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.User;

public interface UserService {

    User findByUsername(String username);

    User authenticateUser(String username, String password);

}