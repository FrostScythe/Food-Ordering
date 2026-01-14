package com.restaurantmanagement.order_api.service;

import com.restaurantmanagement.order_api.entity.MenuItem;
import com.restaurantmanagement.order_api.entity.Restaurant;
import com.restaurantmanagement.order_api.exception.BadRequestException;
import com.restaurantmanagement.order_api.exception.ForbiddenRequestException;
import com.restaurantmanagement.order_api.exception.NotFoundException;
import com.restaurantmanagement.order_api.repository.MenuItemRepository;
import com.restaurantmanagement.order_api.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// In MenuItemService.java
@Service
@Transactional
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // Return MenuItem instead of String
    public MenuItem createMenuItem(Long restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));

        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }

    // Return MenuItem instead of ResponseEntity<String>
    public MenuItem getMenuItem(Long restaurantId, Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("MenuItem", menuItemId));

        // Verify it belongs to the specified restaurant
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new ForbiddenRequestException("Access denied: Menu item " + menuItemId +
                    " does not belong to restaurant " + restaurantId);
        }

        return menuItem;
    }

    // Add this method for getting restaurant menu
    public List<MenuItem> getMenuByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }

        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    // Return MenuItem instead of String
    public MenuItem updateMenuItem(Long restaurantId, Long menuItemId, MenuItem updatedItem) {
        MenuItem existingItem = getMenuItem(restaurantId, menuItemId);

        existingItem.setName(updatedItem.getName());
        double cost = updatedItem.getPrice();
        if (cost < 0) {
            throw new BadRequestException("Price cannot be negative");
        } else existingItem.setPrice(cost);

        existingItem.setDescription(updatedItem.getDescription());

        return menuItemRepository.save(existingItem);
    }

    // Return void instead of String
    public void deleteMenuItem(Long restaurantId, Long menuItemId) {
        MenuItem menuItem = getMenuItem(restaurantId, menuItemId);
        menuItemRepository.delete(menuItem);
    }
}