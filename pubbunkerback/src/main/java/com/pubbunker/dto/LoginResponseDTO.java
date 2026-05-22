package com.pubbunker.dto;
import com.pubbunker.enums.Role;

public class LoginResponseDTO {
    private String nome;
    private String email;
    private Role role;

    public LoginResponseDTO(String nome, String email, Role role) {
        this.nome = nome;
        this.email = email;
        this.role = role;
    }
    public String getNome() {
        return nome;
    }
    public String getEmail() {
        return email;
    }
    public Role getRole() {
        return role;
    }
}
