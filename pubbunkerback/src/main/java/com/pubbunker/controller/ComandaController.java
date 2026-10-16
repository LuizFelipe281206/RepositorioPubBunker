package com.pubbunker.controller;

import com.pubbunker.dto.ComandaResponseDTO;
import com.pubbunker.dto.AcessarComandaDTO;
import com.pubbunker.dto.CriarComandaDTO;
import com.pubbunker.service.ComandaService;
import com.pubbunker.security.AcessoComandaSessao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comandas")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService service;
    private final AcessoComandaSessao acessoSessao;

    @GetMapping
    public List<ComandaResponseDTO> listarTodas() {
        return service.listarTodas()
                .stream()
                .map(comanda -> new ComandaResponseDTO(comanda, true))
                .toList();
    }

    @GetMapping("/{id}")
    public ComandaResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.buscarPorId(id), true
        );
    }

    @GetMapping("/acesso/{codigoAcesso}")
    public ComandaResponseDTO acessar(
            @PathVariable String codigoAcesso
    ) {
        return new ComandaResponseDTO(
                service.buscarAtivaPorCodigo(
                        codigoAcesso
                )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComandaResponseDTO criar(
            @Valid @RequestBody CriarComandaDTO dto
    ) {
        return new ComandaResponseDTO(
                service.criar(dto), true
        );
    }

    @PatchMapping("/{id}/abrir")
    public ComandaResponseDTO abrir(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.abrir(id), true
        );
    }

    @PatchMapping("/{id}/fechar")
    public ComandaResponseDTO fechar(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.fechar(id), true
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }

    @PostMapping("/acesso/{codigoAcesso}")
    public ComandaResponseDTO entrar(
            @PathVariable String codigoAcesso,
            @Valid @RequestBody AcessarComandaDTO dados,
            HttpServletRequest request
    ) {
        // Serializa entradas do mesmo navegador; a trava no banco decide
        // o primeiro acesso entre navegadores/sessões diferentes.
        synchronized (request.getSession()) {
            var comanda = service.autorizarAcesso(codigoAcesso, dados.codigoEntrada());
            acessoSessao.autorizar(comanda, request);
            return new ComandaResponseDTO(comanda);
        }
    }
}
