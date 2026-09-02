package com.pubbunker.model;

import com.pubbunker.enums.StatusComanda;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comandas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comanda_numero",
                        columnNames = "numero"
                ),
                @UniqueConstraint(
                        name = "uk_comanda_codigo_acesso",
                        columnNames = "codigo_acesso"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Column(
            name = "codigo_acesso",
            nullable = false,
            length = 36
    )
    private String codigoAcesso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusComanda status;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}