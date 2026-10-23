package com.pubbunker.model;

import java.time.LocalDateTime;
import com.pubbunker.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Setter(AccessLevel.NONE)
    private String senha;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Column(name = "senha_com_hash")
    private Boolean senhaComHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void setSenha(String senha) {
        this.senha = senha;
        this.senhaComHash = false;
    }

    public void definirSenhaHash(String hash) {
        this.senha = hash;
        this.senhaComHash = true;
    }
}