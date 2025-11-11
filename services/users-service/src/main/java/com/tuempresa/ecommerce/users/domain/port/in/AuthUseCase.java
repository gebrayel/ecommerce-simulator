package com.tuempresa.ecommerce.users.domain.port.in;

import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;

public interface AuthUseCase {

    LoginResult login(LoginCommand command);
}


