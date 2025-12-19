package com.restaurantmanagement.order_api.controller;

import com.restaurantmanagement.order_api.entity.Order;
import com.restaurantmanagement.order_api.entity.OrderStatus;
import com.restaurantmanagement.order_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Place Order
    @PostMapping("/place")
    public Order placeOrder(@RequestParam Long userId,
                            @RequestParam Long restaurantId,
                            @RequestBody Map<Long, Integer> itemsWithQuantity) {

        return orderService.placeOrder(userId, restaurantId, itemsWithQuantity);
    }

    // Get Order by ID
    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    // Get all orders of a user
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    //update order status
    @PutMapping("/{orderId}")
    public Order updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    //Cancel Order
    @PutMapping("/{orderId}")
    public Order cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }
}
