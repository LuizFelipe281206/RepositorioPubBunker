package com.pubbunker.service;

import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import com.pubbunker.exception.RegraNegocioException;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;

    private final PasswordEncoder passwordEncoder;

    private String senhaFicticiaHash;

    @PostConstruct
    void prepararHashFicticio() {
        senhaFicticiaHash =
                passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public Usuario buscarPorId(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: " + id
                        )
                );
    }

    public Usuario buscarPorEmail(String email) {
        return repository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado."
                        )
                );
    }

    public List<Usuario> listarTodos() {
        return repository.findByDeletedAtIsNull();
    }

    public Usuario salvar(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getSenhaComHash())) {
            String senha = usuario.getSenha();

            if (senha == null || senha.isBlank()) {
                throw new RegraNegocioException("Informe a senha.");
            }

            int tamanho = senha.codePointCount(0, senha.length());

            if (tamanho < 15 || tamanho > 128) {
                throw new RegraNegocioException(
                        "A nova senha deve ter entre 15 e 128 caracteres."
                );
            }

            usuario.definirSenhaHash(passwordEncoder.encode(senha));
        }

        return repository.save(usuario);
    }

    public boolean verificarSenha(
            Usuario usuario,
            String senhaInformada
    ) {
        boolean contaValida =
                usuario != null &&
                        usuario.getDeletedAt() == null &&
                        usuario.getRole() != null &&
                        Boolean.TRUE.equals(usuario.getSenhaComHash()) &&
                        usuario.getSenha() != null;

        String hash = contaValida
                ? usuario.getSenha()
                : senhaFicticiaHash;

        String senha = senhaInformada == null
                ? ""
                : senhaInformada;

        try {
            boolean confere = passwordEncoder.matches(senha, hash);

            return contaValida &&
                    senhaInformada != null &&
                    confere;
        } catch (IllegalArgumentException exception) {
            passwordEncoder.matches(senha, senhaFicticiaHash);
            return false;
        }
    }

    public int migrarSenhasAntigas() {
        int migradas = 0;

        for (Usuario usuario : repository.findAll()) {
            if (Boolean.TRUE.equals(usuario.getSenhaComHash())) {
                continue;
            }

            String senha = usuario.getSenha();

            if (senha == null || senha.isBlank()) {
                continue;
            }

            usuario.definirSenhaHash(passwordEncoder.encode(senha));
            repository.save(usuario);
            migradas++;
        }

        return migradas;
    }

    public void deletar(Long id) {
        Usuario usuario = buscarPorId(id);

        usuario.setDeletedAt(LocalDateTime.now());

        repository.save(usuario);
    }
}