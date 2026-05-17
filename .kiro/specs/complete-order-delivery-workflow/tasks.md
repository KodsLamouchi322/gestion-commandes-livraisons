# Implementation Plan: Complete Order Delivery Workflow

## Overview

This implementation plan addresses the complete business logic workflow for the LogiTrack e-commerce order management system. The system currently has CRUD operations but lacks critical business logic connecting orders, payments, deliveries, suppliers, and stock management.

The implementation is organized by priority:
1. **CRITICAL**: Fix payment confirmation bug (blocking current functionality)
2. **HIGH**: Implement delivery workflow (core feature)
3. **MEDIUM**: Implement stock management (important but can be done after delivery)
4. **PARALLEL**: Frontend components (can be developed alongside backend work)

## Tasks

### Phase 1: Fix Payment Confirmation Bug (CRITICAL)

- [x] 1. Fix payment confirmation transaction errors
  - [x] 1.1 Fix PaiementService transaction management
    - Add `@Transactional` annotation to `changerStatut()` method
    - Ensure entities are properly managed within transaction scope
    - Load payment entity fresh from repository to avoid detached entity issues
    - Update payment status and date within transaction
    - Update associated order status to VALIDEE when payment becomes VALIDE
    - _Requirements: 1.1, 1.2, 1.4_
    - _Design: Property 1 (Payment Confirmation Transaction Atomicity)_
  
  - [ ]* 1.2 Write property test for payment confirmation atomicity
    - **Property 1: Payment Confirmation Transaction Atomicity**
    - **Validates: Requirements 1.1, 1.2, 1.4**
    - Test that payment status and order status are updated atomically
    - Use jqwik to generate random payment scenarios
    - Verify both updates succeed or both fail
    - _Requirements: 1.1, 1.2, 1.4_
  
  - [x] 1.3 Implement payment confirmation validation rules
    - Validate payment is in EN_ATTENTE status before confirmation
    - Reject confirmation if payment is already VALIDE
    - For ESPECES: validate delivery status is LIVREE before allowing confirmation
    - For CARTE: allow immediate confirmation regardless of delivery status
    - Return descriptive error messages for validation failures
    - _Requirements: 1.3, 1.5, 6.1, 6.2, 6.3_
    - _Design: Property 2 (Payment Confirmation Error Handling), Property 11 (Cash vs Card Payment Confirmation Rules)_
  
  - [ ]* 1.4 Write unit tests for payment confirmation validation
    - Test CARTE payment confirmation (should succeed immediately)
    - Test ESPECES payment confirmation before delivery (should fail)
    - Test ESPECES payment confirmation after delivery (should succeed)
    - Test confirmation of already VALIDE payment (should fail)
    - _Requirements: 1.3, 1.5, 6.1, 6.2, 6.3_
  
  - [x] 1.5 Add convenience method `confirmerPaiement()`
    - Create method that calls `changerStatut(id, VALIDE)`
    - Include all validation logic for payment method and delivery status
    - Update payment date to current timestamp
    - _Requirements: 1.2, 6.5, 7.2_
    - _Design: Property 12 (Cash Payment Confirmation Timestamp Update), Property 13 (Card Payment Timestamp Update)_

- [x] 2. Checkpoint - Verify payment confirmation works
  - Ensure all tests pass, ask the user if questions arise.

### Phase 2: Implement Delivery Workflow (HIGH PRIORITY)

- [x] 3. Implement delivery creation from validated orders
  - [x] 3.1 Enhance LivraisonService with delivery creation logic
    - Create `creerDepuisCommande(Integer commandeId, Double cout, Integer transporteurId)` method
    - Validate order exists and status is VALIDEE
    - Validate no existing delivery for the order
    - Validate transporter exists (if provided)
    - Initialize delivery with: address from order, status EN_PREPARATION, provided cost (default 0.0), estimated delivery date (now + 3 days)
    - Save delivery and return DTO
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_
    - _Design: Property 3 (Delivery Creation from Validated Orders), Property 4 (Delivery Creation Rejection Rules)_
  
  - [ ]* 3.2 Write property test for delivery creation
    - **Property 3: Delivery Creation from Validated Orders**
    - **Validates: Requirements 2.1, 2.4, 2.5, 2.6, 2.7**
    - Test delivery initialization with correct values
    - Use jqwik to generate random validated orders
    - Verify delivery address, status, cost, and order reference
    - _Requirements: 2.1, 2.4, 2.5, 2.6, 2.7_
  
  - [ ]* 3.3 Write unit tests for delivery creation validation
    - Test creation from VALIDEE order (should succeed)
    - Test creation from EN_ATTENTE order (should fail)
    - Test duplicate delivery creation (should fail)
    - Test with invalid transporter ID (should fail)
    - _Requirements: 2.2, 2.3, 3.3_

- [x] 4. Implement transporter assignment
  - [x] 4.1 Add transporter assignment method to LivraisonService
    - Create `assignerTransporteur(Integer livraisonId, Integer transporteurId)` method
    - Load delivery and validate status is EN_PREPARATION
    - Load and validate transporter exists
    - Assign transporter to delivery
    - Save and return DTO
    - _Requirements: 3.1, 3.2, 3.3_
    - _Design: Property 5 (Transporter Assignment Invariants), Property 6 (Transporter Assignment Rejection)_
  
  - [ ]* 4.2 Write unit tests for transporter assignment
    - Test assignment to EN_PREPARATION delivery (should succeed)
    - Test assignment to LIVREE delivery (should fail)
    - Test assignment with invalid transporter ID (should fail)
    - _Requirements: 3.1, 3.2, 3.3_

- [x] 5. Implement delivery status transitions
  - [x] 5.1 Add delivery status change method to LivraisonService
    - Create `changerStatut(Integer id, StatutLivraison statut)` method with `@Transactional`
    - Load delivery entity
    - Validate status transition rules: EN_PREPARATION → EXPEDIEE, EXPEDIEE → LIVREE, LIVREE → no transitions
    - Update delivery status and timestamp
    - Synchronize order status: EXPEDIEE → order.EXPEDIEE, LIVREE → order.LIVREE
    - Save both delivery and order entities in same transaction
    - Return DTO
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.6, 5.1, 5.2, 5.3_
    - _Design: Property 7 (Delivery Status Transition Rules), Property 8 (Delivery Status Change Timestamp Update), Property 9 (Delivery-to-Order Status Synchronization)_
  
  - [ ]* 5.2 Write property test for delivery-order status synchronization
    - **Property 9: Delivery-to-Order Status Synchronization**
    - **Validates: Requirements 5.1, 5.2, 5.3**
    - Test that delivery and order status update atomically
    - Use jqwik to generate random delivery status changes
    - Verify both updates succeed or both fail
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [ ]* 5.3 Write unit tests for delivery status transitions
    - Test EN_PREPARATION → EXPEDIEE (should succeed and update order)
    - Test EXPEDIEE → LIVREE (should succeed and update order)
    - Test LIVREE → any status (should fail)
    - Test invalid transitions (should fail)
    - Test status change for cancelled order (should fail)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.5_

- [x] 6. Add delivery controller endpoints
  - [x] 6.1 Enhance LivraisonController with new endpoints
    - Add `POST /api/livraisons/depuis-commande` endpoint for creating delivery from order
    - Add `PUT /api/livraisons/{id}/transporteur/{transporteurId}` endpoint for assigning transporter
    - Add `PUT /api/livraisons/{id}/expedier` endpoint for marking as shipped
    - Add `PUT /api/livraisons/{id}/livrer` endpoint for marking as delivered
    - Each endpoint should call corresponding service method and return appropriate HTTP status
    - Include error handling with descriptive messages
    - _Requirements: 2.1, 3.1, 4.1, 4.2_
  
  - [ ]* 6.2 Write integration tests for delivery endpoints
    - Test POST /api/livraisons/depuis-commande with valid and invalid orders
    - Test PUT /api/livraisons/{id}/transporteur/{transporteurId} with valid and invalid data
    - Test PUT /api/livraisons/{id}/expedier and /api/livraisons/{id}/livrer
    - Verify HTTP status codes and response bodies
    - _Requirements: 2.1, 3.1, 4.1, 4.2_

- [x] 7. Checkpoint - Verify delivery workflow works
  - Ensure all tests pass, ask the user if questions arise.

### Phase 3: Implement Stock Management (MEDIUM PRIORITY)

- [x] 8. Create StockService for centralized stock management
  - [x] 8.1 Create StockService class with stock management methods
    - Create `reserverStock(List<LigneCommande> lignes)` method with `@Transactional`
    - Validate all products have sufficient stock before decreasing any quantities
    - Decrease quantiteEnStock for each product
    - Throw exception if any product has insufficient stock (rollback all)
    - Create `libererStock(List<LigneCommande> lignes)` method with `@Transactional`
    - Increase quantiteEnStock for each product
    - Create `reapprovisionner(List<LigneBonCommande> lignes)` method with `@Transactional`
    - Increase quantiteEnStock for each product in purchase order
    - Create `produitsStockFaible()` method to return products where quantiteEnStock < 10
    - Create `estStockSuffisant(Integer produitId, Integer quantite)` validation method
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 12.1, 12.2, 12.3, 13.1_
    - _Design: Property 14 (Low Stock Detection), Property 23 (Order Creation Stock Reservation), Property 24 (Order Creation Stock Validation), Property 25 (Order Cancellation Stock Release)_
  
  - [ ]* 8.2 Write property test for stock reservation atomicity
    - **Property 23: Order Creation Stock Reservation**
    - **Validates: Requirements 12.1, 12.4**
    - Test that stock reservation is atomic with order creation
    - Use jqwik to generate random order lines
    - Verify all stock updates succeed or all fail
    - _Requirements: 12.1, 12.4_
  
  - [ ]* 8.3 Write property test for stock release invariant
    - **Property 26: Order Cancellation Stock Invariant**
    - **Validates: Requirements 13.4**
    - Test that stock after cancellation equals stock before + ordered quantity
    - Use jqwik to generate random cancellation scenarios
    - _Requirements: 13.4_
  
  - [ ]* 8.4 Write unit tests for StockService
    - Test reserverStock with sufficient stock (should succeed)
    - Test reserverStock with insufficient stock (should fail and rollback all)
    - Test libererStock restores quantities correctly
    - Test reapprovisionner increases stock correctly
    - Test produitsStockFaible returns correct products
    - _Requirements: 8.1, 8.2, 12.1, 12.2, 12.3, 13.1_

- [x] 9. Integrate stock management with order lifecycle
  - [x] 9.1 Enhance CommandeService with stock integration
    - Modify `ajouter(Commande c)` method to call `stockService.reserverStock(c.getLignesCommande())`
    - Ensure stock reservation happens in same transaction as order creation
    - Modify `changerStatut(Integer id, StatutCommande statut)` method
    - When changing to ANNULEE, call `stockService.libererStock(lignes)`
    - Ensure stock release happens in same transaction as status change
    - _Requirements: 12.1, 12.4, 12.5, 13.1, 13.2, 13.3_
    - _Design: Property 23 (Order Creation Stock Reservation), Property 25 (Order Cancellation Stock Release)_
  
  - [ ]* 9.2 Write integration tests for order-stock integration
    - Test order creation reserves stock
    - Test order creation with insufficient stock fails
    - Test order cancellation releases stock
    - Test transaction rollback on stock operation failure
    - _Requirements: 12.1, 12.2, 12.4, 12.5, 13.1, 13.2, 13.3_

- [x] 10. Implement purchase order workflow
  - [x] 10.1 Enhance BonCommandeService with purchase order logic
    - Create `creer(BonCommande bonCommande, List<LigneBonCommande> lignes)` method with `@Transactional`
    - Validate fournisseur exists
    - Set dateCreation = now, statut = EN_ATTENTE
    - For each line: validate product exists and is from this supplier, validate quantity > 0
    - Calculate montantTotal
    - Save bon commande and lines
    - Create `changerStatut(Integer id, BonCommande.Statut statut)` method with `@Transactional`
    - Validate status transition rules: EN_ATTENTE → ENVOYE or ANNULE, ENVOYE → RECU
    - If status changes to RECU, call `stockService.reapprovisionner(lignes)`
    - Save and return DTO
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 10.1, 10.2, 10.3, 10.4, 10.5, 11.1, 11.2_
    - _Design: Property 17 (Purchase Order Creation Validation), Property 18 (Purchase Order Initialization), Property 19 (Purchase Order Status Transition Rules), Property 20 (Purchase Order Receipt Stock Update)_
  
  - [ ]* 10.2 Write property test for purchase order stock update
    - **Property 20: Purchase Order Receipt Stock Update**
    - **Validates: Requirements 11.1, 11.2**
    - Test that stock update is atomic with purchase order receipt
    - Use jqwik to generate random purchase orders
    - Verify both updates succeed or both fail
    - _Requirements: 11.1, 11.2_
  
  - [ ]* 10.3 Write property test for purchase order idempotence
    - **Property 22: Purchase Order Receipt Idempotence**
    - **Validates: Requirements 11.5**
    - Test that marking purchase order as RECU multiple times only updates stock once
    - Use jqwik to generate random purchase order scenarios
    - _Requirements: 11.5_
  
  - [ ]* 10.4 Write unit tests for BonCommandeService
    - Test creer with valid supplier (should succeed)
    - Test creer with invalid supplier (should fail)
    - Test creer with wrong supplier product (should fail)
    - Test changerStatut to RECU updates stock
    - Test changerStatut with invalid transitions (should fail)
    - _Requirements: 9.1, 9.5, 9.6, 10.1, 10.2, 10.3, 10.4, 11.1_

- [x] 11. Add stock and purchase order controller endpoints
  - [x] 11.1 Create StockController with stock management endpoints
    - Add `GET /api/stock/faible` endpoint to return products with low stock
    - Add `GET /api/stock/produit/{id}` endpoint to return stock level for a product
    - Include error handling with descriptive messages
    - _Requirements: 8.1, 8.2, 8.3_
  
  - [x] 11.2 Enhance BonCommandeController with purchase order endpoints
    - Add `POST /api/bons-commande` endpoint for creating purchase order with lines
    - Add `PUT /api/bons-commande/{id}/envoyer` endpoint for marking as sent
    - Add `PUT /api/bons-commande/{id}/recevoir` endpoint for marking as received (updates stock)
    - Add `PUT /api/bons-commande/{id}/annuler` endpoint for cancelling purchase order
    - Each endpoint should call corresponding service method and return appropriate HTTP status
    - Include error handling with descriptive messages
    - _Requirements: 9.1, 10.1, 10.2, 11.1_
  
  - [ ]* 11.3 Write integration tests for stock and purchase order endpoints
    - Test GET /api/stock/faible returns correct products
    - Test POST /api/bons-commande with valid and invalid data
    - Test PUT /api/bons-commande/{id}/recevoir updates stock
    - Verify HTTP status codes and response bodies
    - _Requirements: 8.1, 9.1, 10.1, 11.1_

- [x] 12. Checkpoint - Verify stock management works
  - Ensure all tests pass, ask the user if questions arise.

### Phase 4: Frontend Components (Can be done in parallel with backend)

- [x] 13. Enhance Paiements component for cash payment confirmation
  - [x] 13.1 Update Paiements component template and logic
    - Add "💵 À la livraison" badge for ESPECES payments with status EN_ATTENTE
    - Implement `peutConfirmer(paiement)` method: return true if (CARTE and EN_ATTENTE) OR (ESPECES and EN_ATTENTE and delivery.LIVREE)
    - Show "Confirm Payment" button only when `peutConfirmer()` returns true
    - Add `confirmerPaiement(id)` method that calls `POST /api/paiements/{id}/confirmer`
    - Disable button while processing (track with `processingPaiementId`)
    - Refresh payment list after successful confirmation
    - Display error messages for failed confirmations
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5_
  
  - [ ]* 13.2 Write unit tests for Paiements component
    - Test `peutConfirmer()` logic for different payment methods and delivery statuses
    - Test button visibility based on payment state
    - Test confirmation action and UI updates
    - _Requirements: 19.1, 19.2, 19.3, 19.4_

- [x] 14. Create Livraisons component for delivery management
  - [x] 14.1 Create Livraisons component with delivery list and actions
    - Create component with table displaying: delivery ID, order ID, address, transporter, status, actions
    - Add status filter dropdown
    - Display status badges with appropriate colors (EN_PREPARATION: blue, EXPEDIEE: orange, LIVREE: green)
    - Show "Assign Transporter" button for EN_PREPARATION deliveries
    - Show "Mark as Shipped" button for EN_PREPARATION deliveries
    - Show "Mark as Delivered" button for EXPEDIEE deliveries
    - No action buttons for LIVREE deliveries
    - Implement `expedier(id)` method that calls `PUT /api/livraisons/{id}/expedier`
    - Implement `livrer(id)` method that calls `PUT /api/livraisons/{id}/livrer`
    - Refresh delivery list after status changes
    - Display error messages for failed operations
    - _Requirements: 18.1, 18.4, 18.5, 18.6, 18.7_
  
  - [ ]* 14.2 Write unit tests for Livraisons component
    - Test delivery list rendering
    - Test status badge display
    - Test action button visibility based on delivery status
    - Test status change actions
    - _Requirements: 18.1, 18.4, 18.5, 18.6, 18.7_

- [x] 15. Enhance Commandes component with delivery creation
  - [x] 15.1 Update Commandes component to support delivery creation
    - Add "Create Delivery" button for orders with status VALIDEE and no delivery
    - Show delivery status badge if delivery exists
    - Add link to delivery details
    - Implement `creerLivraison(commandeId)` method that opens a modal/form
    - Modal should include: delivery cost input, transporter selection dropdown
    - Call `POST /api/livraisons/depuis-commande` with commandeId, cout, transporteurId
    - Refresh order list after successful delivery creation
    - Display error messages for failed operations
    - _Requirements: 18.2, 18.3_
  
  - [ ]* 15.2 Write unit tests for Commandes component delivery features
    - Test "Create Delivery" button visibility
    - Test delivery status badge display
    - Test delivery creation flow
    - _Requirements: 18.2, 18.3_

- [x] 16. Create Stock component for low stock alerts
  - [x] 16.1 Create Stock component with low stock dashboard
    - Create component with dashboard widget showing low stock count
    - Display table of products below threshold (10 units)
    - Show: product name, current quantity, threshold (10), supplier name
    - Add "Create Purchase Order" button for each low stock product
    - Implement `creerBonCommande(produit)` method that opens a modal/form
    - Modal should include: supplier (pre-filled), product (pre-filled), quantity input, unit price input
    - Call `POST /api/bons-commande` with fournisseurId and lignes
    - Refresh stock list after successful purchase order creation
    - Display error messages for failed operations
    - _Requirements: 20.1, 20.2, 20.3, 20.4_
  
  - [ ]* 16.2 Write unit tests for Stock component
    - Test low stock alert display
    - Test purchase order creation flow
    - Test stock level updates after purchase order receipt
    - _Requirements: 20.1, 20.2, 20.3, 20.4_

- [x] 17. Create BonsCommande component for purchase order management
  - [x] 17.1 Create BonsCommande component with purchase order list and actions
    - Create component with table displaying: purchase order ID, supplier, creation date, total amount, status, actions
    - Add "New Purchase Order" button that opens creation form
    - Creation form should include: supplier selection, product lines (product, quantity, unit price), total calculation
    - Display status badges with appropriate colors (EN_ATTENTE: gray, ENVOYE: blue, RECU: green, ANNULE: red)
    - Show "Send to Supplier" and "Cancel" buttons for EN_ATTENTE purchase orders
    - Show "Mark as Received" button for ENVOYE purchase orders
    - No action buttons for RECU or ANNULE purchase orders
    - Implement `envoyer(id)` method that calls `PUT /api/bons-commande/{id}/envoyer`
    - Implement `recevoir(id)` method that calls `PUT /api/bons-commande/{id}/recevoir`
    - Implement `annuler(id)` method that calls `PUT /api/bons-commande/{id}/annuler`
    - Refresh purchase order list after status changes
    - Display error messages for failed operations
    - _Requirements: 20.5, 20.6, 20.7_
  
  - [ ]* 17.2 Write unit tests for BonsCommande component
    - Test purchase order list rendering
    - Test status badge display
    - Test action button visibility based on purchase order status
    - Test status change actions
    - Test purchase order creation flow
    - _Requirements: 20.5, 20.6, 20.7_

- [x] 18. Checkpoint - Verify frontend components work
  - Ensure all tests pass, ask the user if questions arise.

### Phase 5: Integration and End-to-End Testing

- [ ] 19. Implement end-to-end workflow tests
  - [ ]* 19.1 Write complete order lifecycle test
    - Test: Create order → Confirm payment → Create delivery → Ship → Deliver
    - Verify all status transitions and stock updates
    - _Requirements: All requirements_
  
  - [ ]* 19.2 Write cash payment workflow test
    - Test: Create order → Create delivery → Deliver → Confirm cash payment
    - Verify payment confirmation is blocked until delivery is LIVREE
    - _Requirements: 6.1, 6.2, 19.1, 19.2, 19.3_
  
  - [ ]* 19.3 Write order cancellation workflow test
    - Test: Create order → Cancel → Verify stock released
    - Verify stock quantities are restored correctly
    - _Requirements: 13.1, 13.2, 13.4_
  
  - [ ]* 19.4 Write purchase order workflow test
    - Test: Create purchase order → Send → Receive → Verify stock updated
    - Verify stock quantities increase correctly
    - _Requirements: 9.1, 10.1, 11.1, 11.4_
  
  - [ ]* 19.5 Write low stock alert workflow test
    - Test: Reduce stock → Verify alert → Create purchase order → Receive → Verify alert cleared
    - Verify low stock detection and resolution
    - _Requirements: 8.1, 8.5, 9.1, 11.1_

- [ ] 20. Final checkpoint - Complete system verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties using jqwik (Java) and fast-check (TypeScript)
- Unit tests validate specific examples and edge cases
- Integration tests verify end-to-end workflows
- Frontend and backend work can proceed in parallel after Phase 2
- All service methods use `@Transactional` to ensure atomicity
- Error handling returns descriptive messages with appropriate HTTP status codes
