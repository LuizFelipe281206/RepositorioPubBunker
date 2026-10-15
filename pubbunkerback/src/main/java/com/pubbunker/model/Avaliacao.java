package com.pubbunker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
@DynamicUpdate
public class Avaliacao {

    @Column(name = "arquivado_em")
    private LocalDateTime arquivadoEm;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    @Column(name = "data_abertura_atendimento")
    private LocalDateTime dataAberturaAtendimento;

    @Column(name = "nome_cliente", length = 100)
    private String nomeCliente;

    @Column(nullable = false)
    private Integer nota;

    @Column(nullable = false, length = 255)
    private String comentario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void preencherDataHora() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}