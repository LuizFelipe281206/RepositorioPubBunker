package com.pubbunker.model;

import com.pubbunker.enums.StatusPedido;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToMany
    @JoinTable(
            name = "pedido_produto",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private List<produto> produtos;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;
    private Double valorTotal;
    private LocalDateTime dataPedido;
    private LocalDateTime deletedAt;

    public Pedido() {
    }
    public Long getId() {
        return id;
    }
    public Usuario getCliente() {
        return cliente;
    }
    public List<produto> getProdutos() {
        return produtos;
    }
    public StatusPedido getStatus() {
        return status;
    }
    public Double getValorTotal() {
        return valorTotal;
    }
    public LocalDateTime getDataPedido() {
        return dataPedido;
    }
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }
    public void setProdutos(List<produto> produtos) {
        this.produtos = produtos;
    }
    public void setStatus(StatusPedido status) {
        this.status = status;
    }
    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }
    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}