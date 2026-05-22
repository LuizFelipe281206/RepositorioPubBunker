package com.pubbunker.model;
import com.pubbunker.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @Column(unique = true)
    private String email;
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;
    public Usuario() {
    }
    public Usuario(Long id, String nome, String email, String senha, Role role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }
    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getEmail() {
        return email;
    }
    public String getSenha() {
        return senha;
    }
    public Role getRole() {
        return role;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public void setRole(Role role) {
        this.role = role;
    }
}