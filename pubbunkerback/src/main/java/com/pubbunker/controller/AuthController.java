package com.pubbunker.controller;

import com.pubbunker.dto.LoginRequestDTO;
import com.pubbunker.dto.LoginResponseDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Usuario;
import com.pubbunker.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO dto
    ) {
        try {
            Usuario usuario =
                    usuarioService.buscarPorEmail(dto.getEmail());

            if (!usuario.getSenha().equals(dto.getSenha())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Senha inválida");
            }

            LoginResponseDTO response =
                    new LoginResponseDTO(
                            usuario.getNome(),
                            usuario.getEmail(),
                            usuario.getRole()
                    );

            return ResponseEntity.ok(response);

        } catch (RecursoNaoEncontradoException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não encontrado");
        }
    }
}