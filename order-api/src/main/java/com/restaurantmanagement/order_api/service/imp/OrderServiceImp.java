package com.restaurantmanagement.order_api.service.imp;

import com.restaurantmanagement.order_api.entity.*;
import com.restaurantmanagement.order_api.entity.MenuItem;
import com.restaurantmanagement.order_api.exception.BadRequestException;
import com.restaurantmanagement.order_api.exception.InvalidOrderStateException;
import com.restaurantmanagement.order_api.exception.NotFoundException;
import com.restaurantmanagement.order_api.repository.MenuItemRepository;
import com.restaurantmanagement.order_api.repository.OrderRepository;
import com.restaurantmanagement.order_api.repository.RestaurantRepository;
import com.restaurantmanagement.order_api.repository.UserRepository;
import com.restaurantmanagement.order_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Override
    public Order placeOrder(Long userId, Long restaurantId, Map<Long, Integer> itemsWithQuantity) {// Validate input early
        if (itemsWithQuantity == null || itemsWithQuantity.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        // Fetch user & restaurant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));

        // Validate quantities first
        for (Map.Entry<Long, Integer> entry : itemsWithQuantity.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new BadRequestException("Invalid quantity for menu item: " + entry.getKey());
            }
        }

        // Fetch all menu items in one query
        List<MenuItem> menuItems = menuItemRepository.findAllById(itemsWithQuantity.keySet());
        if (menuItems.size() != itemsWithQuantity.size()) {
            throw new NotFoundException("MenuItem", null);
        }

        double totalPrice = 0;
        int totalItemCount = 0;

        // Use Map instead of duplicates
        Map<MenuItem, Integer> orderedItems = new HashMap<>();

        for (MenuItem item : menuItems) {
            // Ensure item belongs to restaurant
            if (!item.getRestaurant().getId().equals(restaurantId)) {
                throw new BadRequestException("MenuItem " + item.getId() + " does not belong to this restaurant");
            }

            int qty = itemsWithQuantity.get(item.getId());
            orderedItems.put(item, qty);

            totalPrice += item.getPrice() * qty;
            totalItemCount += qty;
        }

        // Create Order
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setOrderedItems((List<MenuItem>) orderedItems);   // change type in entity
        order.setItemCount(totalItemCount);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PLACED);

        return orderRepository.save(order);
    }


    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        OrderStatus currStatus = order.getStatus();

        if (currStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot update order - already delivered");
        }

        if (currStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot update order - already cancelled");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}