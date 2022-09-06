package com.era.onlinesignature.model.auth;

import com.era.onlinesignature.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class AuthResponse {

    private String authToken;
    private String tokenType = "Bearer";
    private Set<Role> roles;

    public AuthResponse(String authToken, Set<Role> roles) {
        this.authToken = authToken;
        this.roles = roles;
    }
}
