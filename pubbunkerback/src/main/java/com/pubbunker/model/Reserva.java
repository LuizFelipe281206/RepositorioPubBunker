package com.pubbunker.model;

import com.pubbunker.enums.StatusReserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Usuario funcionario;

    @Column(
            name = "nome_cliente",
            nullable = false,
            length = 100
    )
    private String nomeCliente;

    @Column(
            name = "quantidade_pessoas",
            nullable = false
    )
    private Integer quantidadePessoas;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusReserva status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void preencherValoresIniciais() {
        if (status == null) {
            status = StatusReserva.RESERVADA;
        }

        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}