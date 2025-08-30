package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/user")
@SessionAttributes("cart")
public class UserController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderRepository orderRepository;

    // Initialize cart in session
    @ModelAttribute("cart")
    public List<CartItem> cart() {
        return new ArrayList<>();
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "user-dashboard";
    }

    @GetMapping("/add-to-cart/{id}")
    public String addToCart(@PathVariable Long id, @ModelAttribute("cart") List<CartItem> cart,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/user/dashboard";
        }

        // Get or initialize a temporary stock map in session
        Map<Long, Integer> reservedStock = (Map<Long, Integer>) session.getAttribute("reservedStock");
        if (reservedStock == null) {
            reservedStock = new HashMap<>();
        }

        int alreadyReserved = reservedStock.getOrDefault(product.getId(), 0);

        // Check against real stock in DB
        if (alreadyReserved < product.getStock()) {
            boolean found = false;
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(product.getId())) {
                    item.incrementQuantity();
                    found = true;
                    break;
                }
            }
            if (!found) {
                cart.add(new CartItem(product, 1)); // Add new item with quantity 1
            }

            // Reserve 1 quantity (not save in DB)
            reservedStock.put(product.getId(), alreadyReserved + 1);
            session.setAttribute("reservedStock", reservedStock);

            redirectAttributes.addFlashAttribute("success", "Product added to cart.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Product is out of stock.");
        }

        return "redirect:/user/dashboard";
    }

    @GetMapping("/cart")
    public String viewCart(@ModelAttribute("cart") List<CartItem> cart, Model model) {
        double total = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable Long productId, @ModelAttribute("cart") List<CartItem> cart,
                             HttpSession session) {
        Iterator<CartItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(productId)) {
                int quantity = item.getQuantity();
                iterator.remove();

                // Restore reserved stock in session
                Map<Long, Integer> reservedStock = (Map<Long, Integer>) session.getAttribute("reservedStock");
                if (reservedStock == null) {
                    reservedStock = new HashMap<>();
                }

                int reservedQty = reservedStock.getOrDefault(productId, 0);
                reservedStock.put(productId, Math.max(reservedQty - quantity, 0));
                session.setAttribute("reservedStock", reservedStock);
                break;
            }
        }

        return "redirect:/user/cart";
    }

    @GetMapping("/checkout")
    public String checkout(@ModelAttribute("cart") List<CartItem> cart, Model model,
                           Authentication authentication, SessionStatus sessionStatus) {
        if (cart.isEmpty()) {
            model.addAttribute("message", "Cart is empty!");
            return "redirect:/user/cart";
        }

        Order order = new Order();
        order.setUserEmail(authentication.getName());
        order.setOrderDate(LocalDateTime.now());
        double total = 0;

        for (CartItem cartItem : cart) {
            Product dbProduct = productRepository.findById(cartItem.getProduct().getId()).orElse(null);
            if (dbProduct == null || dbProduct.getStock() < cartItem.getQuantity()) {
                model.addAttribute("message", "Insufficient stock for: " + cartItem.getProduct().getName());
                return "redirect:/user/cart";
            }

            // Decrement actual stock based on cart item quantity
            dbProduct.setStock(dbProduct.getStock() - cartItem.getQuantity());
            productRepository.save(dbProduct);

            // Create order item
            OrderItem item = new OrderItem();
            item.setProduct(dbProduct);
            item.setPrice(dbProduct.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setOrder(order);
            total += cartItem.getTotalPrice();
            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        // Clear cart
        sessionStatus.setComplete();

        // Send email
        emailService.sendOrderEmail(
                authentication.getName(),
                "Order Confirmation - E-commerce",
                "Thank you for your order! Order ID: " + order.getId()
        );

        return "redirect:/user/orders";
    }

    @GetMapping("/orders")
    public String userOrders(Model model, Authentication authentication) {
        List<Order> orders = orderRepository.findByUserEmail(authentication.getName());
        model.addAttribute("orders", orders);
        return "order-history";
    }
}
