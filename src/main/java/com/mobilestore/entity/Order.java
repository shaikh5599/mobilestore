package com.mobilestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_order") // 'Order' SQL ka reserved word hai, isliye table name badalna achha hai
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String mobileNumber;
    private String address;
    private String city;      // <--- YE ADD KIYA
    private String pincode;   // <--- YE ADD KIYA
    private String status;    // Pending, Shipped, Delivered
    private double totalAmount;
    private LocalDateTime orderDate;
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading performance ke liye achhi hai
    @JoinColumn(name = "user_id")      // Database mein 'user_id' column banega
    private User user;
  
 
	private String ProductDetails;

    public String getProductDetails() {
		return ProductDetails;
	}
	public void setProductDetails(String productDetails) {
		ProductDetails = productDetails;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	// Ek Order mein bahut saare Items ho sakte hain
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id") 
    private List<OrderItem> orderItems; // <--- Bill mein loop isi par chalega

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
	
}