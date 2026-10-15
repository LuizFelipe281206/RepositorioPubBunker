package com.pubbunker.service;

import com.pubbunker.dto.AtualizarFuncionarioDTO;
import com.pubbunker.dto.CriarFuncionarioDTO;
import com.pubbunker.enums.Role;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.UsuarioRepository;
import com.pubbunker.security.IdentidadeUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FuncionarioService {
    private final UsuarioRepository repository;
    private final UsuarioService usuarioService;
    private final IdentidadeUsuario identidadeUsuario;

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        exigirAdmin();

        return repository.findByRoleAndDeletedAtIsNullOrderByNomeAsc(
                Role.FUNCIONARIO
        );
    }

    public Usuario criar(CriarFuncionarioDTO dto) {
        exigirAdmin();

        if (repository.existsByEmailIgnoreCase(dto.email())) {
            throw new RegraNegocioException("Este email já está cadastrado.");
        }

        Usuario funcionario = new Usuario();
        funcionario.setNome(dto.nome());
        funcionario.setEmail(dto.email());
        funcionario.setRole(Role.FUNCIONARIO);
        funcionario.setSenha(dto.senha());

        return salvar(funcionario, true);
    }

    public Usuario atualizar(Long id, AtualizarFuncionarioDTO dto) {
        exigirAdmin();
        Usuario funcionario = buscarFuncionario(id);

        if (repository.existsByEmailIgnoreCaseAndIdNot(dto.email(), id)) {
            throw new RegraNegocioException("Este email já está cadastrado.");
        }

        funcionario.setNome(dto.nome());
        funcionario.setEmail(dto.email());

        return salvar(funcionario, false);
    }

    public void desativar(Long id) {
        exigirAdmin();
        Usuario funcionario = buscarFuncionario(id);

        funcionario.setDeletedAt(LocalDateTime.now());
        repository.save(funcionario);
    }

    private Usuario buscarFuncionario(Long id) {
        return repository.findByIdAndRoleAndDeletedAtIsNull(
                id, Role.FUNCIONARIO
        ).orElseThrow(() -> new RecursoNaoEncontradoException(
                "Funcionário não encontrado."
        ));
    }

    private Usuario salvar(Usuario funcionario, boolean novaConta) {
        try {
            Usuario salvo = novaConta
                    ? usuarioService.salvar(funcionario)
                    : repository.save(funcionario);

            repository.flush();
            return salvo;
        } catch (DataIntegrityViolationException exception) {
            throw new RegraNegocioException(
                    "Não foi possível salvar. Verifique se o email já está em uso."
            );
        }
    }

    private void exigirAdmin() {
        if (identidadeUsuario.atual().role() != Role.ADMIN) {
            throw new AccessDeniedException("Operação não permitida.");
        }
    }
}