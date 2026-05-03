package com.mobilestore.entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; 

    private Integer quantity;
    private double price; 
    private double priceAtOrder; 
    
    // Naya field Variant ke liye
    private String selectedVariant;

    @Column(name = "product_id", insertable = false, updatable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; 

    // Getters and Setters
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public int getQuantity(int quantity) { return this.quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(double priceAtOrder) { this.priceAtOrder = priceAtOrder; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSelectedVariant() { return selectedVariant; }
    public void setSelectedVariant(String selectedVariant) { this.selectedVariant = selectedVariant; }
}