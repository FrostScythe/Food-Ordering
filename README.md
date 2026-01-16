# Food Ordering API

A Spring Boot REST API for managing restaurants, users, menu items, and orders with advanced features including inventory management, concurrent order handling, and business hours validation.

## Technologies Used
- **Spring Boot 4.0**
- **PostgreSQL 42.7.8**
- **JPA/Hibernate 7.1.8**
- **Maven 3.9.11**
- **Java 21**

## Database Configuration
```yaml
Database: PostgreSQL
URL: jdbc:postgresql://localhost:5432/restaurant
Username: postgres
Password: root
```

## Base URL
```
http://localhost:8080
```

---

## Key Features

### Advanced Order Management
The system implements several production-ready features to handle real-world scenarios:

**Inventory Tracking:** Each menu item tracks its available stock quantity. When orders are placed, the system automatically reduces inventory. If an item is out of stock, the order is rejected with a clear error message indicating how many items are available.

**Concurrent Order Handling:** Using pessimistic locking at the database level, the system prevents race conditions when multiple customers order the same item simultaneously. This ensures that two customers cannot order the last remaining item at the same time, preventing overselling.

**Business Hours Validation:** Restaurants can configure their opening hours, closing hours, and preparation time. The system validates that orders are placed only during operating hours, accounting for the time needed to prepare orders before closing.

**Inventory Restoration:** When orders are cancelled, the system automatically returns the items back to inventory, ensuring stock quantities remain accurate.

---

## API Endpoints

### 1. User Management APIs

#### Base Path: `/api/users`

#### 1.1 Register New User
Creates a new user account in the system. All fields are required and email and phone number must be unique across all users.

- **Method:** `POST`
- **URL:** `/api/users/register`
- **Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street, City, Country"
}
```

- **Success Response (201 Created):**
```json
"User registered successfully"
```

- **Error Response (400 Bad Request):**
```json
"Email already exists"
```

#### 1.2 Get All Users
Retrieves a list of all registered users in the system. This endpoint is typically used by administrators.

- **Method:** `GET`
- **URL:** `/api/users`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main Street, City, Country"
  }
]
```

#### 1.3 Get User by ID
Fetches detailed information about a specific user.

- **Method:** `GET`
- **URL:** `/api/users/{id}`
- **Example:** `/api/users/1`
- **Response (200 OK):**
```json
"User Details: Name - John Doe, Email - john.doe@example.com"
```

- **Error Response (404 Not Found):**
```json
"User not found with id: 1"
```

#### 1.4 Update User
Updates user information. All fields in the request body are optional - only provided fields will be updated.

- **Method:** `PUT`
- **URL:** `/api/users/{id}`
- **Example:** `/api/users/1`
- **Request Body:**
```json
{
  "name": "John Updated",
  "email": "john.updated@example.com",
  "phoneNumber": "+1234567891",
  "address": "456 New Street, City, Country"
}
```

- **Success Response (200 OK):**
```json
"User details updated successfully"
```

#### 1.5 Delete User
Removes a user from the system. This will also cascade delete all orders associated with this user.

- **Method:** `DELETE`
- **URL:** `/api/users/{id}`
- **Example:** `/api/users/1`
- **Success Response (200 OK):**
```json
"User deleted successfully"
```

---

### 2. Restaurant Management APIs

#### Base Path: `/api/restaurants`

#### 2.1 Create Restaurant
Registers a new restaurant in the system with business hours. The opening time, closing time, and preparation time are required to ensure proper order validation.

- **Method:** `POST`
- **URL:** `/api/restaurants`
- **Request Body:**
```json
{
  "name": "Tasty Bites",
  "address": "789 Restaurant Avenue, Food City",
  "phoneNumber": "+1987654321",
  "openingTime": "09:00:00",
  "closingTime": "23:00:00",
  "preparationTimeMinutes": 30,
  "isOpen": true
}
```

**Field Explanations:**
- `openingTime`: The time when the restaurant starts accepting orders (format: HH:mm:ss)
- `closingTime`: The time when the restaurant closes for the day (format: HH:mm:ss)
- `preparationTimeMinutes`: How many minutes before closing the kitchen stops accepting orders (typically 30-60 minutes)
- `isOpen`: Manual override to close the restaurant for maintenance or special circumstances

- **Success Response (201 Created):**
```json
{
  "id": 1,
  "name": "Tasty Bites",
  "address": "789 Restaurant Avenue, Food City",
  "phoneNumber": "+1987654321",
  "openingTime": "09:00:00",
  "closingTime": "23:00:00",
  "preparationTimeMinutes": 30,
  "isOpen": true
}
```

#### 2.2 Get All Restaurants
Retrieves all restaurants with their operating information.

- **Method:** `GET`
- **URL:** `/api/restaurants`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Tasty Bites",
    "address": "789 Restaurant Avenue, Food City",
    "phoneNumber": "+1987654321",
    "openingTime": "09:00:00",
    "closingTime": "23:00:00",
    "preparationTimeMinutes": 30,
    "isOpen": true
  }
]
```

#### 2.3 Get Restaurant by ID
Fetches detailed information about a specific restaurant.

- **Method:** `GET`
- **URL:** `/api/restaurants/{id}`
- **Example:** `/api/restaurants/1`
- **Response (200 OK):**
```json
{
  "id": 1,
  "name": "Tasty Bites",
  "address": "789 Restaurant Avenue, Food City",
  "phoneNumber": "+1987654321",
  "openingTime": "09:00:00",
  "closingTime": "23:00:00",
  "preparationTimeMinutes": 30,
  "isOpen": true
}
```

#### 2.4 Get Restaurant Operating Status
Returns the current operating status of a restaurant, including whether it is currently accepting orders and when it will next be available.

- **Method:** `GET`
- **URL:** `/api/restaurants/{id}/status`
- **Example:** `/api/restaurants/1/status`
- **Response (200 OK):**
```json
{
  "restaurantId": 1,
  "restaurantName": "Tasty Bites",
  "isOpen": true,
  "status": "Open now. Last order at 22:30",
  "openingTime": "09:00:00",
  "closingTime": "23:00:00",
  "lastOrderTime": "22:30:00"
}
```

**Status Examples:**
- Before opening: `"Opens at 09:00"`
- During business hours: `"Open now. Last order at 22:30"`
- After last order time: `"Closed. Opens tomorrow at 09:00"`
- Maintenance mode: `"Closed for maintenance"`

#### 2.5 Toggle Restaurant Status
Allows restaurant owners to manually open or close their restaurant, useful for maintenance, special events, or emergencies.

- **Method:** `PATCH`
- **URL:** `/api/restaurants/{id}/toggle`
- **Example:** `/api/restaurants/1/toggle`
- **Response (200 OK):**
```json
{
  "restaurantId": 1,
  "restaurantName": "Tasty Bites",
  "isOpen": false,
  "message": "Restaurant is now closed"
}
```

#### 2.6 Update Restaurant
Updates restaurant details including operating hours.

- **Method:** `PUT`
- **URL:** `/api/restaurants/{id}`
- **Example:** `/api/restaurants/1`
- **Request Body:**
```json
{
  "name": "Tasty Bites Updated",
  "address": "999 New Location, Food City",
  "phoneNumber": "+1987654322",
  "openingTime": "08:00:00",
  "closingTime": "00:00:00",
  "preparationTimeMinutes": 45
}
```

#### 2.7 Delete Restaurant
Removes a restaurant from the system. This will cascade delete all menu items and orders associated with this restaurant.

- **Method:** `DELETE`
- **URL:** `/api/restaurants/{id}`
- **Example:** `/api/restaurants/1`
- **Success Response (204 No Content)**

---

### 3. Menu Item Management APIs

#### Base Path: `/api/restaurants/{restaurantId}/menu`

#### 3.1 Get Restaurant's Full Menu
Retrieves all menu items for a specific restaurant, including inventory information.

- **Method:** `GET`
- **URL:** `/api/restaurants/{restaurantId}/menu`
- **Example:** `/api/restaurants/1/menu`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "description": "Classic pizza with tomato sauce, mozzarella, and basil",
    "price": 12.99,
    "stockQuantity": 50,
    "available": true
  },
  {
    "id": 2,
    "name": "Caesar Salad",
    "description": "Fresh romaine lettuce with Caesar dressing and croutons",
    "price": 8.99,
    "stockQuantity": 0,
    "available": false
  }
]
```

**Inventory Fields:**
- `stockQuantity`: The number of items currently available in inventory
- `available`: Whether the item can be ordered (automatically set to false when stock reaches zero)

#### 3.2 Add Menu Item to Restaurant
Creates a new menu item with initial inventory. The default stock quantity is 100 units if not specified.

- **Method:** `POST`
- **URL:** `/api/restaurants/{restaurantId}/menu`
- **Example:** `/api/restaurants/1/menu`
- **Request Body:**
```json
{
  "name": "Spaghetti Carbonara",
  "description": "Traditional Italian pasta with eggs, cheese, and bacon",
  "price": 14.99,
  "stockQuantity": 100,
  "available": true
}
```

- **Success Response (201 Created):**
```json
{
  "id": 3,
  "name": "Spaghetti Carbonara",
  "description": "Traditional Italian pasta with eggs, cheese, and bacon",
  "price": 14.99,
  "stockQuantity": 100,
  "available": true
}
```

#### 3.3 Get Specific Menu Item
Retrieves detailed information about a single menu item.

- **Method:** `GET`
- **URL:** `/api/restaurants/{restaurantId}/menu/{menuItemId}`
- **Example:** `/api/restaurants/1/menu/1`
- **Response (200 OK):**
```json
{
  "id": 1,
  "name": "Margherita Pizza",
  "description": "Classic pizza with tomato sauce, mozzarella, and basil",
  "price": 12.99,
  "stockQuantity": 50,
  "available": true
}
```

- **Error Response (403 Forbidden):**
```json
"Access denied: Menu item 1 does not belong to restaurant 1"
```

#### 3.4 Update Menu Item
Updates menu item details including inventory. You can use this to restock items or adjust prices.

- **Method:** `PUT`
- **URL:** `/api/restaurants/{restaurantId}/menu/{menuItemId}`
- **Example:** `/api/restaurants/1/menu/1`
- **Request Body:**
```json
{
  "name": "Margherita Pizza Deluxe",
  "description": "Premium pizza with tomato sauce, buffalo mozzarella, and fresh basil",
  "price": 15.99,
  "stockQuantity": 75,
  "available": true
}
```

#### 3.5 Delete Menu Item
Removes a menu item from the restaurant's menu.

- **Method:** `DELETE`
- **URL:** `/api/restaurants/{restaurantId}/menu/{menuItemId}`
- **Example:** `/api/restaurants/1/menu/1`
- **Success Response (204 No Content)**

---

### 4. Order Management APIs

#### Base Path: `/api/orders`

#### 4.1 Place Order
Creates a new order with automatic inventory validation and business hours checking. The system performs multiple validations before accepting the order.

- **Method:** `POST`
- **URL:** `/api/orders/place-order?userId={userId}&restaurantId={restaurantId}`
- **Example:** `/api/orders/place-order?userId=1&restaurantId=1`
- **Request Body:**
```json
{
  "1": 2,
  "2": 1
}
```

**Request Body Format:** The body is a JSON object where keys are menu item IDs and values are quantities. In the example above, we're ordering 2 units of menu item 1 and 1 unit of menu item 2.

- **Success Response (200 OK):**
```json
{
  "id": 1,
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "restaurant": {
    "id": 1,
    "name": "Tasty Bites"
  },
  "orderedItems": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "price": 12.99
    },
    {
      "id": 1,
      "name": "Margherita Pizza",
      "price": 12.99
    },
    {
      "id": 2,
      "name": "Caesar Salad",
      "price": 8.99
    }
  ],
  "itemCount": 3,
  "totalPrice": 34.97,
  "status": "PLACED",
  "orderAt": "2025-01-16T14:30:00",
  "deliveryAt": "2025-01-16T14:30:00"
}
```

**Order Validation Process:**
1. Verifies the user exists
2. Verifies the restaurant exists
3. Checks if the restaurant is currently open and accepting orders
4. Validates that all menu items belong to the specified restaurant
5. Locks menu items in the database to prevent concurrent modification
6. Checks if sufficient inventory is available for each item
7. Reduces inventory by the ordered quantity
8. Calculates the total price
9. Creates and saves the order

**Error Responses:**

Restaurant Closed (503 Service Unavailable):
```json
{
  "error": "Restaurant Closed",
  "message": "Restaurant 'Tasty Bites' is currently closed. We open at 09:00. You can place orders then.",
  "restaurantName": "Tasty Bites",
  "reason": "We open at 09:00. You can place orders then.",
  "openingTime": "09:00:00",
  "closingTime": "23:00:00",
  "lastOrderTime": "22:30:00"
}
```

Out of Stock (400 Bad Request):
```json
"Item 'Margherita Pizza' is not available in requested quantity. Available: 1"
```

Invalid Quantity (400 Bad Request):
```json
"Invalid quantity for menu item: 1"
```

#### 4.2 Get Order by ID
Retrieves complete details of a specific order.

- **Method:** `GET`
- **URL:** `/api/orders/{orderId}`
- **Example:** `/api/orders/1`
- **Response (200 OK):**
```json
{
  "id": 1,
  "user": {
    "id": 1,
    "name": "John Doe"
  },
  "restaurant": {
    "id": 1,
    "name": "Tasty Bites"
  },
  "orderedItems": [...],
  "itemCount": 3,
  "totalPrice": 34.97,
  "status": "PLACED",
  "orderAt": "2025-01-16T14:30:00"
}
```

- **Error Response (404 Not Found):**
```json
"Order not found with id: 1"
```

#### 4.3 Get Orders by User
Retrieves all orders placed by a specific user, ordered by most recent first.

- **Method:** `GET`
- **URL:** `/api/orders/user/{userId}`
- **Example:** `/api/orders/user/1`
- **Response (200 OK):**
```json
[
  {
    "id": 2,
    "restaurant": {...},
    "orderedItems": [...],
    "totalPrice": 45.50,
    "status": "DELIVERED",
    "orderAt": "2025-01-16T18:00:00"
  },
  {
    "id": 1,
    "restaurant": {...},
    "orderedItems": [...],
    "totalPrice": 34.97,
    "status": "PLACED",
    "orderAt": "2025-01-16T14:30:00"
  }
]
```

#### 4.4 Update Order Status
Updates the status of an order. The system automatically restores inventory when an order is cancelled.

- **Method:** `PUT`
- **URL:** `/api/orders/{orderId}?status={newStatus}`
- **Example:** `/api/orders/1?status=CANCELLED`
- **Valid Status Values:**
  - `PLACED`: Order has been placed and is being prepared
  - `DELIVERED`: Order has been completed and delivered
  - `CANCELLED`: Order has been cancelled

- **Success Response (200 OK):**
```json
{
  "id": 1,
  "status": "CANCELLED",
  ...
}
```

**Inventory Restoration:** When an order status changes from PLACED to CANCELLED, the system automatically returns all ordered items back to inventory and marks them as available again.

**Error Responses:**

Cannot Update Delivered Order (409 Conflict):
```json
"Cannot update order - already delivered"
```

Cannot Update Cancelled Order (409 Conflict):
```json
"Cannot update order - already cancelled"
```

---

## How Concurrent Orders Are Handled

The system uses a combination of database locking and transaction management to prevent race conditions when multiple users order simultaneously.

**Pessimistic Locking:** When processing an order, the system locks the menu item rows in the database using `SELECT ... FOR UPDATE`. This prevents other transactions from reading or modifying the same rows until the current transaction completes.

**Transaction Isolation:** The order placement process runs in a SERIALIZABLE transaction, which is the highest isolation level. This ensures that even if multiple orders are processing simultaneously, they execute as if they were running one at a time.

**Example Scenario:** If two customers try to order the last pizza at the exact same time, here's what happens. Customer A's request arrives first and locks the menu item row. The system checks that one pizza is available, reduces the stock to zero, and creates the order. Only after this transaction completes does Customer B's request process. When it checks inventory, it finds zero pizzas available and rejects the order with a clear error message.

This approach prevents overselling while maintaining good performance for typical order volumes. The locks are held only for the duration of the order processing, typically a few milliseconds.

---

## Testing the Application

### Setting Up Test Data

First, create the necessary test data in the correct order to satisfy foreign key constraints.

**Step 1: Create a Restaurant**
```bash
curl -X POST http://localhost:8080/api/restaurants ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Pizza Palace\",\"address\":\"123 Food Street\",\"phoneNumber\":\"+1234567890\",\"openingTime\":\"09:00:00\",\"closingTime\":\"23:00:00\",\"preparationTimeMinutes\":30,\"isOpen\":true}"
```

**Step 2: Add Menu Items**
```bash
curl -X POST http://localhost:8080/api/restaurants/1/menu ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Margherita Pizza\",\"description\":\"Classic pizza\",\"price\":12.99,\"stockQuantity\":50,\"available\":true}"

curl -X POST http://localhost:8080/api/restaurants/1/menu ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Pepperoni Pizza\",\"description\":\"Spicy pizza\",\"price\":14.99,\"stockQuantity\":3,\"available\":true}"
```

**Step 3: Create a User**
```bash
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Alice Johnson\",\"email\":\"alice@example.com\",\"phoneNumber\":\"+1111111111\",\"address\":\"456 Customer Ave\"}"
```

### Testing Business Hours Validation

**Test 1: Check Restaurant Status**
```bash
curl -X GET http://localhost:8080/api/restaurants/1/status
```

**Test 2: Try Ordering When Open (between 09:00 and 22:30)**
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"1\":2,\"2\":1}"
```
Expected: Order succeeds with 200 OK

**Test 3: Close Restaurant for Maintenance**
```bash
curl -X PATCH http://localhost:8080/api/restaurants/1/toggle
```

**Test 4: Try Ordering When Closed**
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"1\":1}"
```
Expected: Error 503 with message about restaurant being closed for maintenance

### Testing Inventory Management

**Test 1: Check Initial Stock**
```bash
curl -X GET http://localhost:8080/api/restaurants/1/menu/2
```
Expected: Shows stockQuantity of 3

**Test 2: Order 2 Items**
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"2\":2}"
```
Expected: Order succeeds, stock reduces to 1

**Test 3: Try to Order More Than Available**
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"2\":5}"
```
Expected: Error 400 indicating only 1 item available

**Test 4: Cancel Order and Check Inventory Restoration**
```bash
# Get the order ID from the previous successful order (let's say it's 1)
curl -X PUT "http://localhost:8080/api/orders/1?status=CANCELLED"

# Check inventory again
curl -X GET http://localhost:8080/api/restaurants/1/menu/2
```
Expected: Stock quantity restored to 3

### Testing Concurrent Orders

To test concurrent order handling, you need to simulate multiple orders happening simultaneously. This is best done with a tool like Apache JMeter or by writing a simple script.

**Simple Test with Two Terminal Windows:**

Terminal 1:
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"2\":1}"
```

Terminal 2 (run immediately after):
```bash
curl -X POST "http://localhost:8080/api/orders/place-order?userId=1&restaurantId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"2\":1}"
```

If only 1 item is in stock, one request will succeed and the other will fail with an out-of-stock error.

---

## Running the Application

### Prerequisites
Ensure you have the following installed on your system:
- Java Development Kit (JDK) 21 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher (or use the included Maven wrapper)

### Database Setup

**Step 1: Create the Database**
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE restaurant;

-- Connect to the new database
\c restaurant

-- Exit psql
\q
```

The application will automatically create all necessary tables when it starts, thanks to Hibernate's auto-DDL feature configured in application.yaml.

### Starting the Application

**Using Maven Wrapper (Recommended):**

On Linux/Mac:
```bash
cd order-api
./mvnw spring-boot:run
```

On Windows:
```cmd
cd order-api
mvnw.cmd spring-boot:run
```

**Using System Maven:**
```bash
cd order-api
mvn spring-boot:run
```

The application will start on port 8080. You should see log messages indicating successful startup, including Hibernate creating the database schema.

### Verifying the Application

Once started, verify the application is running:
```bash
curl http://localhost:8080/api/restaurants
```

This should return an empty array if no restaurants have been created yet.

---

## Project Structure

```
order-api/
├── src/
│   ├── main/
│   │   ├── java/com/restaurantmanagement/order_api/
│   │   │   ├── controller/
│   │   │   │   ├── OrderController.java          # Order endpoints
│   │   │   │   ├── RestaurantController.java     # Restaurant & menu endpoints
│   │   │   │   └── UserController.java           # User endpoints
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── MenuItem.java                 # Menu item with inventory
│   │   │   │   ├── Order.java                    # Order entity
│   │   │   │   ├── OrderStatus.java              # Order status enum
│   │   │   │   ├── Restaurant.java               # Restaurant with hours
│   │   │   │   └── User.java                     # User entity
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── MenuItemRepository.java       # With locking queries
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RestaurantRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── MenuItemService.java          # Menu management logic
│   │   │   │   ├── OrderService.java             # Order interface
│   │   │   │   ├── RestaurantService.java        # Restaurant logic
│   │   │   │   ├── UserService.java              # User management
│   │   │   │   └── imp/
│   │   │   │       └── OrderServiceImp.java      # Order logic with locking
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── ForbiddenRequestException.java
│   │   │   │   ├── GlobalExceptionHandler.java   # Centralized error handling
│   │   │   │   ├── InvalidOrderStateException.java
│   │   │   │   ├── NotFoundException.java
│   │   │   │   └── RestaurantClosedException.java # Business hours exception
│   │   │   │
│   │   │   └── OrderApiApplication.java          # Main application class
│   │   │
│   │   └── resources/
│   │       └── application.yaml                  # Configuration file
│   │
│   └── test/
│       └── java/com/restaurantmanagement/order_api/
│           └── OrderApiApplicationTests.java     # Basic tests
│
└── pom.xml                                       # Maven dependencies
```

---

## Configuration

The application.yaml file contains all configuration settings:

```yaml
spring:
  application:
    name: order-api

  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant
    username: postgres
    password: root

  jpa:
    hibernate:
      ddl-auto: update  # Automatically updates database schema
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

**Important Configuration Notes:**

The `ddl-auto: update` setting tells Hibernate to automatically create and update database tables based on your entity classes. This is convenient for development but should be changed to `validate` in production environments, where schema changes should be managed through proper database migration tools.

---

## Technical Implementation Details

### Inventory Management

The inventory system tracks stock levels for each menu item and prevents overselling through careful validation. When a menu item is created, it starts with a default stock quantity of 100 units. As orders are placed, the stock quantity decreases automatically. When stock reaches zero, the item's available flag is set to false, preventing new orders until the item is restocked.

The MenuItem entity includes two key methods for inventory management. The `canOrder(quantity)` method checks if the requested quantity is available before allowing an order. The `reduceStock(quantity)` method decreases the stock and automatically marks the item as unavailable if stock reaches zero.

### Concurrent Order Handling

The system uses pessimistic locking to handle concurrent orders safely. When an order is being processed, the `findByIdWithLock()` method in MenuItemRepository locks the menu item rows in the database. This prevents other transactions from modifying the same items simultaneously.

The order placement method runs in a SERIALIZABLE transaction, which is the highest isolation level in PostgreSQL. This ensures that concurrent orders are processed as if they were running sequentially, preventing race conditions and data inconsistencies.

### Business Hours Validation

Restaurants define their operating hours through four fields: opening time, closing time, preparation time in minutes, and a manual open/closed flag. The last order time is calculated by subtracting the preparation time from the closing time. For example, a restaurant closing at 11 PM with 30 minutes preparation time stops accepting orders at 10:30 PM.

The `validateOperatingHours()` method in the Restaurant entity checks three conditions before allowing an order. First, it verifies the restaurant is not manually closed for maintenance. Second, it ensures the current time is after the opening time. Third, it confirms the current time is before the last order time. If any check fails, the method throws a RestaurantClosedException with a detailed explanation.

### Exception Handling

The application uses a global exception handler to provide consistent error responses across all endpoints. The GlobalExceptionHandler class catches specific exceptions and returns appropriate HTTP status codes with clear error messages.

For example, when a restaurant is closed, the handler returns a 503 Service Unavailable status with details about when the restaurant will reopen. When an item is out of stock, it returns a 400 Bad Request with information about available quantity. This approach ensures users receive helpful
