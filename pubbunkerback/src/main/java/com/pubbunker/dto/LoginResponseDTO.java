package com.pubbunker.dto;

import com.pubbunker.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private Role role;
}