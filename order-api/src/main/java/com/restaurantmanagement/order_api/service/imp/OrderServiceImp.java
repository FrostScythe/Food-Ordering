package com.restaurantmanagement.order_api.service.imp;

import com.restaurantmanagement.order_api.entity.*;
import com.restaurantmanagement.order_api.entity.MenuItem;
import com.restaurantmanagement.order_api.repository.MenuItemRepository;
import com.restaurantmanagement.order_api.repository.OrderRepository;
import com.restaurantmanagement.order_api.repository.RestaurantRepository;
import com.restaurantmanagement.order_api.repository.UserRepository;
import com.restaurantmanagement.order_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
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
    public Order placeOrder(Long userId,Long restaurantId,Map<Long, Integer> itemsWithQuantity) {

        // 1️⃣ Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Fetch Restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        double totalPrice = 0;
        int totalItemCount = 0;
        List<MenuItem> orderedItems = new ArrayList<>();

        // 3️⃣ Calculate price using quantity
        for (Map.Entry<Long, Integer> entry : itemsWithQuantity.entrySet()) {

            Long menuItemId = entry.getKey();
            Integer quantity = entry.getValue();

            MenuItem item = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            totalPrice += item.getPrice() * quantity;
            totalItemCount += quantity;

            // add item once per quantity (simple approach)
            for (int i = 0; i < quantity; i++) {
                orderedItems.add(item);
            }
        }

        // 4️⃣ Create Order
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setOrderedItems(orderedItems);
        order.setItemCount(totalItemCount);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PLACED);

        // 5️⃣ Save and return
        return orderRepository.save(order);
    }


    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order info= orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus currStatus= info.getStatus();

        if(currStatus==OrderStatus.DELIVERED){
            throw new RuntimeException("Delivered orders cannot be updated");
        }

        if(currStatus== OrderStatus.CANCELLED){
            throw new RuntimeException("Cancelled orders cannot be updated");
        }

        info.setStatus(newStatus);

        return orderRepository.save(info);
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order info= orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        if(info.getStatus()==OrderStatus.DELIVERED){
            throw new RuntimeException("Delivered orders cannot be updated");
        }
        info.setStatus(OrderStatus.CANCELLED);
        return null;
    }
}