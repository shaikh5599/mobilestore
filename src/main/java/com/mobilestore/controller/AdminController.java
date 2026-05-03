package com.mobilestore.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mobilestore.entity.Category;
import com.mobilestore.entity.Order;
import com.mobilestore.entity.Product;
import com.mobilestore.entity.StoreSetting;
import com.mobilestore.entity.User;
import com.mobilestore.repository.AdminRepository;
import com.mobilestore.repository.CategoryRepository;
import com.mobilestore.repository.OrderRepository;
import com.mobilestore.repository.ProductRepository;
import com.mobilestore.repository.StoreRepository;
import com.mobilestore.repository.UserRepository;
import com.mobilestore.service.FileStorageService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProductRepository productRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private FileStorageService fileService;
    @Autowired private AdminRepository adminRepo;
    @Autowired private StoreRepository storeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder; 

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productRepo.count());
        model.addAttribute("totalCategories", categoryRepo.count());
        model.addAttribute("totalOrders", orderRepo.count());
        model.addAttribute("recentOrders", orderRepo.findAll());
        
        Double totalAmount = orderRepo.getTotalRevenue();
        model.addAttribute("totalAmount", totalAmount != null ? totalAmount : 0.0);

        return "admin/dashboard"; 
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productRepo.findAll()); 
        return "admin/manage-products";
    }

    @GetMapping("/products/add")
    public String addProductPage(Model model) {
        model.addAttribute("categories", categoryRepo.findAll()); 
        model.addAttribute("product", new Product()); 
        return "admin/add-product";
    }

    @PostMapping("/products/add")
    public String saveProduct(@ModelAttribute Product product, 
                              @RequestParam("img") MultipartFile file, 
                              HttpSession session) {
        try {
            String savedFileName = fileService.saveImage(file);
            product.setImageUrl(savedFileName);
            
            productRepo.save(product);
            session.setAttribute("succMsg", "Product Added Successfully!");
            
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Something went wrong! " + e.getMessage());
        }
        return "redirect:/admin/products"; 
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        try {
            Product p = productRepo.findById(id).orElse(null);
            if(p != null) {
                fileService.deleteImage(p.getImageUrl());
            }
            orderRepo.deleteByProductId(id);
            productRepo.deleteById(id);
            session.setAttribute("succMsg", "Product successfully deleted from everywhere!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products"; 
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model) {
        Product product = productRepo.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/edit-product"; 
    }

    @PostMapping("/products/update") 
    public String updateProduct(@ModelAttribute Product product, 
                                @RequestParam("img") MultipartFile img, 
                                HttpSession session) {
        try {
            Product oldProduct = productRepo.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + product.getId()));

            oldProduct.setName(product.getName());
            oldProduct.setBrand(product.getBrand());
            oldProduct.setPrice(product.getPrice());
            oldProduct.setMrp(product.getMrp());
            oldProduct.setStock(product.getStock()); 
            oldProduct.setWarranty(product.getWarranty());
            oldProduct.setHighlights(product.getHighlights());
            oldProduct.setFreeDelivery(product.isFreeDelivery());
            oldProduct.setReturnPolicy(product.isReturnPolicy());
            oldProduct.setStorage(product.getStorage());

            if (!img.isEmpty()) {
                String savedFileName = fileService.saveImage(img);
                oldProduct.setImageUrl(savedFileName);
            }

            productRepo.save(oldProduct);
            session.setAttribute("succMsg", "Product updated successfully!");

        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/admin/products"; 
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/categories"; 
    }

    @PostMapping("/categories/add")
    public String saveCategory(@RequestParam("name") String name, HttpSession session) {
        Category cat = new Category();
        cat.setName(name);
        categoryRepo.save(cat);
        session.setAttribute("succMsg", "Category Added!");
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session) {
        try {
            categoryRepo.deleteById(id);
            session.setAttribute("succMsg", "Category Deleted Successfully!");
        } catch (DataIntegrityViolationException e) {
            session.setAttribute("errorMsg", "Cannot delete! Pehle is category ke saare products delete karein.");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/admin/categories"; 
    }
    
    @GetMapping("/customers")
    public String manageCustomers(Model model) {
        List<User> customers = userRepo.findByRole("ROLE_USER");
        if (customers == null) {
            customers = new ArrayList<>();
        }
        model.addAttribute("customers", customers); 
        return "admin/customers"; 
    }

    @GetMapping("/orders")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderRepo.findAll());
        return "admin/orders"; 
    }

    @GetMapping("/order-details/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/admin/orders";
        
        model.addAttribute("order", order);
        return "admin/order-details"; 
    }

    @GetMapping("/invoice/print/{id}")
    public String printInvoice(@PathVariable Long id, Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        model.addAttribute("order", order);
        return "admin/invoice-print"; 
    }
    
    @GetMapping("/settings")
    public String settingsPage(Model model, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        model.addAttribute("admin", admin); 
        model.addAttribute("store", storeRepo.findById(1L).orElse(new StoreSetting()));
        return "admin/settings";
    }

    @PostMapping("/update-store-settings")
    public String updateStore(@ModelAttribute StoreSetting store, 
                            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                            HttpSession session) {
        try {
            StoreSetting oldStore = storeRepo.findById(1L).orElse(new StoreSetting());
            oldStore.setStoreName(store.getStoreName());
            oldStore.setStoreEmail(store.getStoreEmail());
            oldStore.setContactNo(store.getContactNo());
            oldStore.setAddress(store.getAddress());
            oldStore.setFacebookUrl(store.getFacebookUrl());
            oldStore.setInstagramUrl(store.getInstagramUrl());
            oldStore.setFooterText(store.getFooterText());

            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = fileService.saveImage(logoFile);
                oldStore.setLogo(fileName);
            }
            
            storeRepo.save(oldStore);
            session.setAttribute("succMsg", "Shop Details Updated Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Failed to save: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/update-admin-profile")
    public String updateAdminProfile(@ModelAttribute User user, HttpSession session) {
        if (user.getId() == null) {
            session.setAttribute("errorMsg", "Fatal Error: Admin ID is missing from form!");
            return "redirect:/admin/settings";
        }

        try {
            User existingAdmin = userRepo.findById(user.getId()).orElse(null);

            if (existingAdmin != null) {
                existingAdmin.setName(user.getName());
                existingAdmin.setEmail(user.getEmail());
                existingAdmin.setMobileNumber(user.getMobileNumber());
                
                userRepo.save(existingAdmin);

                session.setAttribute("loggedInUser", existingAdmin);
                session.setAttribute("succMsg", "Admin Profile Updated Successfully!");
            } else {
                session.setAttribute("errorMsg", "Admin ID " + user.getId() + " not found in database!");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; 
        }

        try {
            boolean isPasswordMatch = passwordEncoder.matches(oldPassword, loggedInUser.getPassword());

            if (isPasswordMatch) {
                String encryptedNewPassword = passwordEncoder.encode(newPassword);
                loggedInUser.setPassword(encryptedNewPassword);
                userRepo.save(loggedInUser); 
                
                session.setAttribute("loggedInUser", loggedInUser);
                session.setAttribute("succMsg", "Password Changed Successfully!");
            } else {
                session.setAttribute("errorMsg", "Old Password is Incorrect!");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Something went wrong!");
        }

        return "redirect:/admin/settings"; 
    }

    @PostMapping("/update-banner")
    public String updateBanner(@RequestParam(value = "bannerFile", required = false) MultipartFile file, 
                               @RequestParam("bannerLink") String link, 
                               HttpSession session) {
        try {
            StoreSetting store = storeRepo.findById(1L).orElse(new StoreSetting());

            if (file != null && !file.isEmpty()) {
                String fileName = fileService.saveImage(file);
                store.setBannerImage(fileName);
            }
            
            store.setBannerLink(link);
            storeRepo.save(store);
            session.setAttribute("succMsg", "Banner Updated Successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Banner Error: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
    
    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam("id") Long id, @RequestParam("status") String status, HttpSession session) {
        try {
            Order order = orderRepo.findById(id).orElse(null); 
            
            if (order != null) {
                order.setStatus(status);
                orderRepo.save(order);
                session.setAttribute("succMsg", "Order Status Updated to: " + status);
            } else {
                session.setAttribute("errorMsg", "Order not found!");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/orders"; 
    }
}