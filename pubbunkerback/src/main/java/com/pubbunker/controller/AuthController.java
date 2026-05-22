package com.pubbunker.controller;
import com.pubbunker.dto.LoginRequestDTO;
import com.pubbunker.dto.LoginResponseDTO;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.usuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {
    private final usuarioRepository usuarioRepository;

    public AuthController(usuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByEmail(dto.getEmail());
        if(usuarioOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não encontrado");
        }
        Usuario usuario = usuarioOptional.get();
        if(!usuario.getSenha().equals(dto.getSenha())) {
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
    }
}
