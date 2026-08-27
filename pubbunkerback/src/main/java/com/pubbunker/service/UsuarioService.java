package com.pubbunker.service;

import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;

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
        return repository.save(usuario);
    }

    public void deletar(Long id) {
        Usuario usuario = buscarPorId(id);

        usuario.setDeletedAt(LocalDateTime.now());

        repository.save(usuario);
    }
}