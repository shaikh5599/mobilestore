package com.mobilestore.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobilestore.entity.CartItem;
import com.mobilestore.entity.Category;
import com.mobilestore.entity.Order;
import com.mobilestore.entity.OrderItem;
import com.mobilestore.entity.Product;
import com.mobilestore.entity.StoreSetting;
import com.mobilestore.entity.User;
import com.mobilestore.repository.CartRepository;
import com.mobilestore.repository.CategoryRepository;
import com.mobilestore.repository.OrderRepository;
import com.mobilestore.repository.ProductRepository;
import com.mobilestore.repository.StoreRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class HomeController {

    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private StoreRepository storeRepo;
    @Autowired private CategoryRepository categoryRepo; // Purana wala
    @Autowired private CategoryRepository categoryRepository; 
    
    @Autowired private CartRepository cartRepo; // Isse Java ko Cart table ka access mil jayega
    
    
    @GetMapping("/") // Ya jo bhi aapka home path hai
    public String index(Model model) {
        // DB se saare products lekar aao
        List<Product> allProducts = productRepo.findAll();
     // Database se settings nikalo
        StoreSetting store = storeRepo.findById(1L).orElse(null);
        model.addAttribute("store", store); // Ye line zaroori hai
        // Model mein add karo taaki HTML index.html par show ho sake
        model.addAttribute("products", allProducts);
     // Database se saari categories nikalo
        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("categories", allCategories);
        
        // Featured products bhi bhej do
        model.addAttribute("products", productRepo.findTop8ByOrderByIdDesc());
        
        return "index"; 
    }
    
 // 1. Account / Profile Page ke liye mapping
    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        // (Future me aap yahan user ka data database se laa kar model me daalenge)
        
        return "profile"; // Ye src/main/resources/templates/profile.html ko load karega
    }

    // 2. Orders Page ke liye mapping (Jis wajah se aapko error aaya)
   
    
  
  
    @GetMapping("/category/{id}")
    public String showProductsByCategory(@PathVariable("id") int id, Model model) {
        
        // 1. Us specific category ke products fetch karo
        List<Product> products = productRepo.findByCategoryId((long) id);
        
        // 2. Category ka naam bhi nikal lo title ke liye
        String categoryName = categoryRepository.findById((long) id).get().getName();
        
        // 3. Data model mein dalo
        model.addAttribute("products", products);
        model.addAttribute("categoryName", categoryName);
        
        // 4. Sirf EK page return karo jo sabke liye common hai
        return "shop"; 
    }
    
    // 2. MOBILES & ACCESSORIES LINK (Naam se category dhundho)
    @GetMapping("/category/name/{categoryName}")
    public String showCategoryByName(@PathVariable String categoryName, Model model) {
        // Database se category ka naam dhundho (e.g., "Smartphone" ya "Accessories")
        Category category = categoryRepo.findByNameIgnoreCase(categoryName);
        
        if (category == null) {
            return "redirect:/"; // Agar category nahi mili toh home pe bhej do
        }

        // Us category ke products nikalo aur shop.html pe bhejo
        List<Product> products = productRepo.findByCategoryId(category.getId());
        
        model.addAttribute("products", products);
        model.addAttribute("categoryName", category.getName());
        
        // Hum purana shop.html hi reuse kar rahe hain!
        return "shop"; 
    }

    // 3. DEALS LINK (Maan lijiye Deals mein hum sabse saste ya random products dikha rahe hain)
    @GetMapping("/deals")
    public String showDeals(Model model) {
        // Abhi ke liye saare products bhej dete hain, baad me discount logic laga sakte hain
        List<Product> dealsProducts = productRepo.findAll(); 
        
        model.addAttribute("products", dealsProducts);
        model.addAttribute("pageTitle", "Today's Top Deals"); // Title change kar diya
        
        return "shop"; // Deals ke liye bhi wahi shop page use hoga
    }

    // 4. CONTACT LINK
    @GetMapping("/contact")
    public String showContactPage() {
        return "contact"; // Iske liye ek naya contact.html banana padega
    }
    

    @Transactional
    @PostMapping("/order/place")
    public String placeOrder(@ModelAttribute Order order, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartRepo.findByUser(user);

        double totalAmount = 0;
        StringBuilder details = new StringBuilder();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if(product.getStock() < cartItem.getQuantity()){
                return "redirect:/cart?stockError=true";
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepo.save(product);

            totalAmount += product.getPrice() * cartItem.getQuantity();

            details.append(product.getName())
                   .append(" x ")
                   .append(cartItem.getQuantity())
                   .append(", ");
        }

        order.setStatus("Pending");
        order.setTotalAmount(totalAmount);
        order.setProductDetails(details.toString());
        order.setUser(user);

        orderRepo.save(order);

        cartRepo.deleteAll(cartItems);

        return "redirect:/?orderSuccess=true";
    } 
    
    @Transactional
    public void processOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            int rowsUpdated = productRepo.reduceStock(item.getProduct().getId(), item.getQuantity());
            
            if (rowsUpdated == 0) {
                // Agar stock kam nahi hua, matlab stock khatam ho gaya
                throw new RuntimeException("Out of stock for product: " + item.getProduct().getName());
            }
        }
    }
    
    // 1. Search Functionality
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        List<Product> searchResults = productRepo.searchProducts(keyword);
        model.addAttribute("products", searchResults);
        model.addAttribute("keyword", keyword); // Taki search bar mein text dikhta rahe
        return "index";
    }

    // 2. Brand Filter Functionality
    @GetMapping("/brand/{name}")
    public String filterByBrand(@PathVariable("name") String name, Model model) {
        List<Product> brandProducts = productRepo.findByBrandIgnoreCase(name);
        model.addAttribute("products", brandProducts);
        return "index"; 
    }
    @GetMapping("/product/detail/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {
        Product product = productRepo.findById(id).orElse(null);
        
        if (product == null) {
            return "redirect:/";
        }

        List<Product> relatedProducts = new ArrayList<>();
        if (product.getBrand() != null) {
            // Brand ke basis pe products lao
            relatedProducts = productRepo.findByBrandIgnoreCase(product.getBrand());
            // Current product ko list se hatao
            relatedProducts.removeIf(p -> p.getId().equals(id));
        }

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts); 
        
        return "product-details"; // Ye file name niche check karo
    }   
 // Aapka wo method jahan Order ban raha hai (Checkout)
    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable Long id) {

        // 1. Order ko find karein
        Order order = orderRepo.findById(id).orElse(null);

        // 2. Check karein ki order mil gaya aur uska status "Pending" hai
        if (order != null && "Pending".equalsIgnoreCase(order.getStatus())) {

            // 3. Order ke andar jitne bhi items hain, unpar loop chalayein
            // Maan lijiye aapke Order model mein 'orderItems' naam ki List hai
            for (OrderItem item : order.getOrderItems()) {
                
                Product product = item.getProduct(); // OrderItem se product nikalein
                
                if (product != null) {
                    // 4. Stock wapas badhayein (Stock + Cancelled Quantity)
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepo.save(product);
                }
            }

            // 5. Order ka status update karein
            order.setStatus("Cancelled");
            orderRepo.save(order);
        }

        return "redirect:/my-orders";
    }

   
}