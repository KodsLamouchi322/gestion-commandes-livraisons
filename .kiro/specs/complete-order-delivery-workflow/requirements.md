# Requirements Document

## Introduction

This document specifies the requirements for completing the order management and delivery business logic workflow in the LogiTrack e-commerce system. The system currently has CRUD operations for all entities but lacks the critical business logic connecting orders, payments, deliveries, suppliers, and transporters. This feature will implement the complete lifecycle from order creation through payment validation to delivery completion, including stock management with suppliers.

The system follows an admin-managed model where administrators handle all operations including payment confirmations, delivery creation, transporter assignments, and stock replenishment. There are no separate login accounts for suppliers or transporters.

## Glossary

- **Order_System**: The complete order management system including orders, payments, and deliveries
- **Payment_Validator**: The component responsible for validating and confirming payments
- **Delivery_Manager**: The component responsible for creating and managing deliveries
- **Stock_Manager**: The component responsible for tracking and updating product inventory
- **Purchase_Order_System**: The component responsible for managing purchase orders to suppliers
- **Admin**: System administrator who manages all operations
- **Client**: Customer who places orders
- **Transporter**: Third-party delivery service provider
- **Supplier**: Product supplier for stock replenishment
- **Order**: A customer order (Commande entity)
- **Payment**: A payment transaction (Paiement entity)
- **Delivery**: A delivery shipment (Livraison entity)
- **Purchase_Order**: A purchase order to a supplier (BonCommande entity)
- **Product**: An item available for purchase (Produit entity)

## Requirements

### Requirement 1: Fix Payment Confirmation Transaction Error

**User Story:** As an admin, I want payment confirmations to complete successfully without database transaction errors, so that I can validate customer payments reliably.

#### Acceptance Criteria

1. WHEN an admin confirms a payment with status EN_ATTENTE, THE Payment_Validator SHALL update the payment status to VALIDE without throwing a JPA transaction error
2. WHEN a payment status changes to VALIDE, THE Order_System SHALL update the associated order status to VALIDEE
3. WHEN a payment confirmation fails, THE Payment_Validator SHALL return a descriptive error message indicating the failure reason
4. THE Payment_Validator SHALL persist both payment and order status changes within a single database transaction
5. WHEN a payment is already VALIDE, THE Payment_Validator SHALL reject further status changes with an error message

### Requirement 2: Delivery Creation from Validated Orders

**User Story:** As an admin, I want to create deliveries for validated orders, so that I can initiate the shipping process.

#### Acceptance Criteria

1. WHEN an order has status VALIDEE and no existing delivery, THE Delivery_Manager SHALL allow creation of a new delivery
2. WHEN an order has status EN_ATTENTE, THE Delivery_Manager SHALL reject delivery creation with an error message
3. WHEN an order already has a delivery, THE Delivery_Manager SHALL reject duplicate delivery creation with an error message
4. WHEN creating a delivery, THE Delivery_Manager SHALL initialize the delivery address with the order's delivery address
5. WHEN creating a delivery, THE Delivery_Manager SHALL set the initial delivery status to EN_PREPARATION
6. WHEN creating a delivery, THE Delivery_Manager SHALL calculate the delivery cost based on the provided cost value
7. THE Delivery_Manager SHALL persist the delivery with a reference to the associated order

### Requirement 3: Transporter Assignment to Deliveries

**User Story:** As an admin, I want to assign transporters to deliveries, so that delivery responsibilities are clearly defined.

#### Acceptance Criteria

1. WHEN a delivery has status EN_PREPARATION, THE Delivery_Manager SHALL allow assignment of a transporter
2. WHEN a delivery has status LIVREE, THE Delivery_Manager SHALL reject transporter reassignment with an error message
3. WHEN assigning a transporter, THE Delivery_Manager SHALL validate that the transporter exists in the system
4. THE Delivery_Manager SHALL persist the transporter assignment to the delivery record
5. WHEN a transporter is assigned, THE Delivery_Manager SHALL maintain the current delivery status

### Requirement 4: Delivery Status Transitions

**User Story:** As an admin, I want to update delivery status through valid transitions, so that I can track the delivery lifecycle accurately.

#### Acceptance Criteria

1. WHEN a delivery has status EN_PREPARATION, THE Delivery_Manager SHALL allow transition to EXPEDIEE
2. WHEN a delivery has status EXPEDIEE, THE Delivery_Manager SHALL allow transition to LIVREE
3. WHEN a delivery has status LIVREE, THE Delivery_Manager SHALL reject any further status changes
4. WHEN a delivery transitions to an invalid status, THE Delivery_Manager SHALL reject the transition with an error message
5. THE Delivery_Manager SHALL persist the delivery status change
6. WHEN a delivery status changes, THE Delivery_Manager SHALL update the timestamp of the status change

### Requirement 5: Automatic Order Status Updates Based on Delivery Status

**User Story:** As an admin, I want order statuses to update automatically when delivery statuses change, so that order tracking remains synchronized with delivery progress.

#### Acceptance Criteria

1. WHEN a delivery status changes to EXPEDIEE, THE Order_System SHALL update the associated order status to EXPEDIEE
2. WHEN a delivery status changes to LIVREE, THE Order_System SHALL update the associated order status to LIVREE
3. THE Order_System SHALL persist both delivery and order status changes within a single database transaction
4. WHEN a delivery status update fails, THE Order_System SHALL rollback both delivery and order status changes
5. WHEN an order status is ANNULEE, THE Order_System SHALL prevent delivery status updates

### Requirement 6: Cash Payment Confirmation After Delivery

**User Story:** As an admin, I want to confirm cash payments only after delivery completion, so that payment validation reflects actual cash collection.

#### Acceptance Criteria

1. WHEN a payment has method ESPECES and the associated delivery has status LIVREE, THE Payment_Validator SHALL allow payment confirmation
2. WHEN a payment has method ESPECES and the associated delivery does not have status LIVREE, THE Payment_Validator SHALL reject payment confirmation with an error message
3. WHEN a payment has method CARTE, THE Payment_Validator SHALL allow immediate payment confirmation regardless of delivery status
4. WHEN confirming a cash payment, THE Payment_Validator SHALL update the payment status to VALIDE
5. WHEN confirming a cash payment, THE Payment_Validator SHALL update the payment date to the current timestamp

### Requirement 7: Card Payment Immediate Confirmation

**User Story:** As a client, I want card payments to be confirmed immediately after successful Stripe processing, so that my order can proceed to delivery preparation without delay.

#### Acceptance Criteria

1. WHEN a Stripe payment succeeds, THE Payment_Validator SHALL automatically update the payment status to VALIDE
2. WHEN a Stripe payment succeeds, THE Payment_Validator SHALL update the payment date to the current timestamp
3. WHEN a payment status changes to VALIDE via Stripe, THE Order_System SHALL update the associated order status to VALIDEE
4. WHEN a Stripe payment fails, THE Payment_Validator SHALL update the payment status to REFUSE
5. THE Payment_Validator SHALL persist payment status changes within a database transaction

### Requirement 8: Low Stock Detection and Alerts

**User Story:** As an admin, I want to be alerted when product stock levels are low, so that I can replenish inventory before stockouts occur.

#### Acceptance Criteria

1. WHEN a product quantity falls below a defined minimum threshold, THE Stock_Manager SHALL flag the product as low stock
2. THE Stock_Manager SHALL provide a list of all products flagged as low stock
3. WHEN querying low stock products, THE Stock_Manager SHALL return product details including current quantity and supplier information
4. THE Stock_Manager SHALL calculate the low stock threshold as 10 units for all products
5. WHEN a product quantity is updated, THE Stock_Manager SHALL re-evaluate the low stock status

### Requirement 9: Purchase Order Creation to Suppliers

**User Story:** As an admin, I want to create purchase orders to suppliers for low stock products, so that I can replenish inventory.

#### Acceptance Criteria

1. WHEN creating a purchase order, THE Purchase_Order_System SHALL validate that the supplier exists in the system
2. WHEN creating a purchase order, THE Purchase_Order_System SHALL set the initial status to EN_ATTENTE
3. WHEN creating a purchase order, THE Purchase_Order_System SHALL set the creation date to the current timestamp
4. THE Purchase_Order_System SHALL allow adding multiple product lines to a purchase order
5. WHEN adding a product line, THE Purchase_Order_System SHALL validate that the product is supplied by the specified supplier
6. WHEN adding a product line, THE Purchase_Order_System SHALL validate that the quantity is positive
7. THE Purchase_Order_System SHALL calculate the total amount of the purchase order based on product unit prices and quantities

### Requirement 10: Purchase Order Status Management

**User Story:** As an admin, I want to update purchase order statuses through valid transitions, so that I can track the procurement lifecycle.

#### Acceptance Criteria

1. WHEN a purchase order has status EN_ATTENTE, THE Purchase_Order_System SHALL allow transition to ENVOYE
2. WHEN a purchase order has status ENVOYE, THE Purchase_Order_System SHALL allow transition to RECU
3. WHEN a purchase order has status RECU, THE Purchase_Order_System SHALL reject any further status changes
4. WHEN a purchase order has status EN_ATTENTE, THE Purchase_Order_System SHALL allow transition to ANNULE
5. WHEN a purchase order has status ANNULE or RECU, THE Purchase_Order_System SHALL reject transition to other statuses

### Requirement 11: Automatic Stock Updates on Purchase Order Receipt

**User Story:** As an admin, I want product stock to update automatically when purchase orders are received, so that inventory levels reflect received goods without manual data entry.

#### Acceptance Criteria

1. WHEN a purchase order status changes to RECU, THE Stock_Manager SHALL increase the quantity of each product in the purchase order by the ordered quantity
2. THE Stock_Manager SHALL persist stock quantity updates within the same database transaction as the purchase order status change
3. WHEN a stock update fails, THE Stock_Manager SHALL rollback both the stock update and the purchase order status change
4. FOR ALL products in a received purchase order, the stock quantity after receipt SHALL equal the stock quantity before receipt plus the ordered quantity (invariant property)
5. WHEN a purchase order is marked as RECU multiple times, THE Stock_Manager SHALL update stock only on the first transition to RECU (idempotence property)

### Requirement 12: Stock Reservation on Order Creation

**User Story:** As a client, I want product stock to be reserved when I place an order, so that my order can be fulfilled and other customers cannot purchase unavailable items.

#### Acceptance Criteria

1. WHEN an order is created, THE Stock_Manager SHALL decrease the quantity of each ordered product by the ordered quantity
2. WHEN a product has insufficient stock for an order, THE Stock_Manager SHALL reject the order creation with an error message
3. THE Stock_Manager SHALL validate stock availability for all products before decreasing any quantities
4. THE Stock_Manager SHALL persist stock quantity updates within the same database transaction as the order creation
5. WHEN a stock reservation fails, THE Stock_Manager SHALL rollback both the stock update and the order creation

### Requirement 13: Stock Release on Order Cancellation

**User Story:** As an admin, I want product stock to be released when orders are cancelled, so that inventory becomes available for other customers.

#### Acceptance Criteria

1. WHEN an order status changes to ANNULEE, THE Stock_Manager SHALL increase the quantity of each ordered product by the ordered quantity
2. THE Stock_Manager SHALL persist stock quantity updates within the same database transaction as the order cancellation
3. WHEN a stock release fails, THE Stock_Manager SHALL rollback both the stock update and the order status change
4. FOR ALL products in a cancelled order, the stock quantity after cancellation SHALL equal the stock quantity before cancellation plus the ordered quantity (invariant property)
5. WHEN an order is cancelled multiple times, THE Stock_Manager SHALL release stock only on the first transition to ANNULEE (idempotence property)

### Requirement 14: Order Status Validation Rules

**User Story:** As an admin, I want order status transitions to follow business rules, so that orders progress through valid lifecycle states.

#### Acceptance Criteria

1. WHEN an order has status EN_ATTENTE, THE Order_System SHALL allow transition to VALIDEE or ANNULEE
2. WHEN an order has status VALIDEE, THE Order_System SHALL allow transition to EXPEDIEE or ANNULEE
3. WHEN an order has status EXPEDIEE, THE Order_System SHALL allow transition to LIVREE only
4. WHEN an order has status LIVREE or ANNULEE, THE Order_System SHALL reject any further status changes
5. WHEN an order status transition violates business rules, THE Order_System SHALL reject the transition with an error message

### Requirement 15: Payment Status Validation Rules

**User Story:** As an admin, I want payment status transitions to follow business rules, so that payment states remain consistent and valid.

#### Acceptance Criteria

1. WHEN a payment has status EN_ATTENTE, THE Payment_Validator SHALL allow transition to VALIDE or REFUSE
2. WHEN a payment has status VALIDE, THE Payment_Validator SHALL reject any status changes
3. WHEN a payment has status REFUSE, THE Payment_Validator SHALL allow transition to EN_ATTENTE for retry
4. WHEN a payment status transition violates business rules, THE Payment_Validator SHALL reject the transition with an error message
5. THE Payment_Validator SHALL validate payment status transitions before persisting changes

### Requirement 16: One-to-One Relationship Enforcement

**User Story:** As a system administrator, I want the system to enforce one-to-one relationships between orders, payments, and deliveries, so that data integrity is maintained.

#### Acceptance Criteria

1. WHEN creating a payment, THE Order_System SHALL validate that the order does not already have a VALIDE payment
2. WHEN creating a delivery, THE Order_System SHALL validate that the order does not already have a delivery
3. WHEN an order is deleted, THE Order_System SHALL cascade delete the associated payment and delivery
4. THE Order_System SHALL enforce database constraints preventing multiple payments per order
5. THE Order_System SHALL enforce database constraints preventing multiple deliveries per order

### Requirement 17: Delivery Cost Calculation

**User Story:** As an admin, I want delivery costs to be calculated when creating deliveries, so that shipping charges are accurately tracked.

#### Acceptance Criteria

1. WHEN creating a delivery, THE Delivery_Manager SHALL accept a delivery cost value
2. THE Delivery_Manager SHALL validate that the delivery cost is zero or positive
3. WHEN a delivery cost is not provided, THE Delivery_Manager SHALL default the cost to 0.0
4. THE Delivery_Manager SHALL persist the delivery cost with the delivery record
5. THE Delivery_Manager SHALL allow updating the delivery cost while the delivery status is EN_PREPARATION

### Requirement 18: Frontend Delivery Management Interface

**User Story:** As an admin, I want a user interface to manage deliveries, so that I can create, assign, and track deliveries efficiently.

#### Acceptance Criteria

1. WHEN viewing the admin panel, THE Order_System SHALL display a deliveries management section
2. WHEN viewing a validated order without a delivery, THE Order_System SHALL display a "Create Delivery" button
3. WHEN clicking "Create Delivery", THE Order_System SHALL display a form with delivery address, cost, and transporter selection
4. WHEN viewing a delivery with status EN_PREPARATION, THE Order_System SHALL display a "Mark as Shipped" button
5. WHEN viewing a delivery with status EXPEDIEE, THE Order_System SHALL display a "Mark as Delivered" button
6. WHEN viewing a delivery with status LIVREE, THE Order_System SHALL display the delivery as completed with no action buttons
7. THE Order_System SHALL display delivery status with appropriate visual indicators (badges/colors)

### Requirement 19: Frontend Cash Payment Confirmation Interface

**User Story:** As an admin, I want the payment interface to show cash payment confirmation only after delivery completion, so that I confirm payments at the correct time.

#### Acceptance Criteria

1. WHEN viewing a payment with method ESPECES and status EN_ATTENTE, THE Order_System SHALL display "💵 À la livraison" indicator
2. WHEN viewing a payment with method ESPECES, status EN_ATTENTE, and associated delivery status LIVREE, THE Order_System SHALL display a "Confirm Payment" button
3. WHEN viewing a payment with method ESPECES, status EN_ATTENTE, and associated delivery status not LIVREE, THE Order_System SHALL hide the "Confirm Payment" button
4. WHEN viewing a payment with method CARTE and status EN_ATTENTE, THE Order_System SHALL display a "Confirm Payment" button regardless of delivery status
5. WHEN a cash payment is confirmed, THE Order_System SHALL update the payment status display to VALIDE

### Requirement 20: Frontend Stock Management Interface

**User Story:** As an admin, I want a user interface to view low stock alerts and create purchase orders, so that I can manage inventory efficiently.

#### Acceptance Criteria

1. WHEN viewing the admin dashboard, THE Order_System SHALL display a low stock alert section showing products below the minimum threshold
2. WHEN viewing a low stock product, THE Order_System SHALL display current quantity, minimum threshold, and supplier information
3. WHEN clicking on a low stock product, THE Order_System SHALL provide an option to create a purchase order
4. WHEN creating a purchase order, THE Order_System SHALL display a form with supplier selection, product selection, and quantity input
5. WHEN viewing purchase orders, THE Order_System SHALL display status, supplier, creation date, and total amount
6. WHEN viewing a purchase order with status ENVOYE, THE Order_System SHALL display a "Mark as Received" button
7. WHEN marking a purchase order as received, THE Order_System SHALL update stock quantities and display a success confirmation

