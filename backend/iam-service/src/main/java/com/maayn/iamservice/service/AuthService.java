package com.maayn.iamservice.service;

import maayn.veld.generated.models.GenericResponse;
import maayn.veld.generated.models.JwtResponse;
import maayn.veld.generated.models.LoginRequest;
import maayn.veld.generated.models.RegisterRequest;
import maayn.veld.generated.models.UserResponse;

public interface AuthService {

    JwtResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    GenericResponse logout();
}