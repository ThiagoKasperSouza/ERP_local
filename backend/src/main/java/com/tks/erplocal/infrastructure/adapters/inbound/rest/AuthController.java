package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.app.usecases.users.GoogleLoginUseCase;
import com.tks.erplocal.app.usecases.users.LoginUseCase;
import com.tks.erplocal.app.usecases.users.RegisterUseCase;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.infrastructure.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final JwtService jwtService;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase,
                          GoogleLoginUseCase googleLoginUseCase, JwtService jwtService) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest body) {
        User user = registerUseCase.execute(body.name(), body.email(), body.password());
        return new AuthResponse(jwtService.generate(user), UserResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest body) {
        User user = loginUseCase.execute(body.email(), body.password());
        return new AuthResponse(jwtService.generate(user), UserResponse.from(user));
    }

    @PostMapping("/google")
    public AuthResponse google(@RequestBody GoogleLoginRequest body) {
        User user = googleLoginUseCase.execute(body.idToken());
        return new AuthResponse(jwtService.generate(user), UserResponse.from(user));
    }

    @PostMapping("/google/code")
    public AuthResponse googleCode(@RequestBody GoogleCodeLoginRequest body) {
        User user = googleLoginUseCase.executeWithCode(body.code(), body.codeVerifier(), body.redirectUri());
        return new AuthResponse(jwtService.generate(user), UserResponse.from(user));
    }
}
