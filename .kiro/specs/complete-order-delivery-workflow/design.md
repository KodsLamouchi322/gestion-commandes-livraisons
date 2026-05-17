# Design Document: Complete Order Delivery Workflow

## Overview

This design implements the complete business logic workflow for the LogiTrack e-commerce order management system. The system currently has CRUD operations for all entities but lacks critical business logic connecting orders, payments, deliveries, suppliers, and stock management.

### Goals

1. **Fix Payment Transaction Errors**: Resolve JPA transaction issues when confirming payments
2. **Implement Delivery Lifecycle**: Enable creation, transporter assignment, and status tracking of deliveries
3. **Synchronize Order and Delivery States**: Automatically update order statuses based on delivery progress
4. **Manage Cash vs Card Payments**: Enforce cash payment confirmation only after delivery completion
5. **Implement Stock Management**: Track inventory, detect low stock, and manage purchase orders
6. **Automate Stock Updates**: Reserve stock on order creation, release on cancellation, replenish on purchase order receipt

### Key Business Rules

- **Order Status Flow**: EN_ATTENTE → VALIDEE → EXPEDIEE → LIVREE (or ANNULEE)
- **Payment Status Flow**: EN_ATTENTE → VALIDE (or REFUSE)
- **Delivery Status Flow**: EN_PREPARATION → EXPEDIEE → LIVREE
- **Purchase Order Flow**: EN_ATTENTE → ENVOYE → RECU (or ANNULE)
- **One-to-One Relationships**: Each order has exactly one payment and one delivery
- **Stock Synchronization**: Stock updates must be transactional with order/purchase order changes

### Technology Stack

- **Backend**: Spring Boot 3.x, JPA/Hibernate, PostgreSQL
- **Frontend**: Angular 17+, TypeScript
- **Payment**: Stripe API integration
- **Architecture**: Layered (Controller → Service → Repository)

---

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Angular)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Orders     │  │  Payments    │  │  Deliveries  │      │
│  │  Component   │  │  Component   │  │  Component   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │   Stock      │  │  Purchase    │                         │
│  │  Component   │  │   Orders     │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    Controllers                        │   │
│  │  CommandeController │ PaiementController │            │   │
│  │  LivraisonController │ BonCommandeController         │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                     Services                          │   │
│  │  CommandeService │ PaiementService │ LivraisonService│   │
│  │  StockService │ BonCommandeService │ StripeService   │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   Repositories                        │   │
│  │  CommandeRepository │ PaiementRepository │            │   │
│  │  LivraisonRepository │ BonCommandeRepository         │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │ JPA
┌─────────────────────────────────────────────────────────────┐
│                    Database (PostgreSQL)                     │
│  Commande │ Paiement │ Livraison │ Produit │ BonCommande   │
└─────────────────────────────────────────────────────────────┘
```

### Service Layer Responsibilities

**CommandeService**
- Order CRUD operations
- Order status validation and transitions
- Integration with stock reservation/release

**PaiementService**
- Payment creation and status management
- Payment method validation (CARTE vs ESPECES)
- Integration with order status updates
- Stripe payment verification

**LivraisonService**
- Delivery creation from validated orders
- Transporter assignment
- Delivery status transitions
- Integration with order status synchronization

**StockService** (New)
- Stock level tracking
- Low stock detection (threshold: 10 units)
- Stock reservation on order creation
- Stock release on order cancellation
- Stock replenishment on purchase order receipt

**BonCommandeService**
- Purchase order creation and management
- Purchase order status transitions
- Integration with stock updates on receipt

### Transaction Management Strategy

All state-changing operations use `@Transactional` to ensure atomicity:

1. **Payment Confirmation**: Payment status + Order status updated in single transaction
2. **Delivery Status Change**: Delivery status + Order status updated in single transaction
3. **Order Creation**: Order creation + Stock reservation in single transaction
4. **Order Cancellation**: Order status + Stock release in single transaction
5. **Purchase Order Receipt**: Purchase order status + Stock replenishment in single transaction

---

## Components and Interfaces

### Backend Services

#### 1. PaiementService (Enhanced)

**Purpose**: Fix transaction errors and implement payment confirmation logic

**Key Methods**:

```java
@Transactional
public PaiementDTO changerStatut(Integer id, StatutPaiement statut) {
    // 1. Load payment with proper entity management
    // 2. Validate status transition rules
    // 3. For ESPECES: validate delivery is LIVREE
    // 4. Update payment status and date
    // 5. If VALIDE: update order status to VALIDEE
    // 6. Return DTO
}

@Transactional
public PaiementDTO confirmerPaiement(Integer id) {
    // Convenience method that calls changerStatut(id, VALIDE)
    // Validates payment method and delivery status
}
```

**Transaction Fix**: Use `@Transactional` on service methods, ensure entities are properly managed within transaction scope, avoid detached entity issues by loading fresh from repository.

#### 2. LivraisonService (Enhanced)

**Purpose**: Implement complete delivery lifecycle management

**Key Methods**:

```java
@Transactional
public LivraisonDTO creerDepuisCommande(Integer commandeId, Double cout, Integer transporteurId) {
    // 1. Validate order exists and status is VALIDEE
    // 2. Validate no existing delivery for order
    // 3. Validate transporter exists (if provided)
    // 4. Create delivery with:
    //    - adresse from order
    //    - statut = EN_PREPARATION
    //    - cout = provided value (default 0.0)
    //    - dateLivraison = now + 3 days (estimated)
    // 5. Save and return DTO
}

@Transactional
public LivraisonDTO assignerTransporteur(Integer livraisonId, Integer transporteurId) {
    // 1. Load delivery
    // 2. Validate status is EN_PREPARATION
    // 3. Load and validate transporter
    // 4. Assign transporter
    // 5. Save and return DTO
}

@Transactional
public LivraisonDTO changerStatut(Integer id, StatutLivraison statut) {
    // 1. Load delivery
    // 2. Validate status transition
    // 3. Update delivery status
    // 4. Synchronize order status:
    //    - EXPEDIEE → order.EXPEDIEE
    //    - LIVREE → order.LIVREE
    // 5. Save both entities
    // 6. Return DTO
}
```

**Status Transition Rules**:
- EN_PREPARATION → EXPEDIEE (only)
- EXPEDIEE → LIVREE (only)
- LIVREE → no further transitions

#### 3. StockService (New)

**Purpose**: Centralize all stock management logic

**Key Methods**:

```java
@Transactional
public void reserverStock(List<LigneCommande> lignes) {
    // 1. For each line, validate product has sufficient stock
    // 2. If any product insufficient, throw exception (rollback all)
    // 3. Decrease quantiteEnStock for each product
    // 4. Save all products
}

@Transactional
public void libererStock(List<LigneCommande> lignes) {
    // 1. For each line, increase quantiteEnStock
    // 2. Save all products
}

@Transactional
public void reapprovisionner(List<LigneBonCommande> lignes) {
    // 1. For each line, increase quantiteEnStock
    // 2. Save all products
}

public List<ProduitDTO> produitsStockFaible() {
    // Return products where quantiteEnStock < 10
}

public boolean estStockSuffisant(Integer produitId, Integer quantite) {
    // Check if product has sufficient stock
}
```

**Idempotence Handling**: Track whether stock has been updated for an order/purchase order to prevent duplicate updates on retry.

#### 4. BonCommandeService (Enhanced)

**Purpose**: Implement purchase order lifecycle with stock integration

**Key Methods**:

```java
@Transactional
public BonCommandeDTO creer(BonCommande bonCommande, List<LigneBonCommande> lignes) {
    // 1. Validate fournisseur exists
    // 2. Set dateCreation = now, statut = EN_ATTENTE
    // 3. For each line:
    //    - Validate product exists and is from this supplier
    //    - Validate quantity > 0
    // 4. Calculate montantTotal
    // 5. Save bon commande and lines
    // 6. Return DTO
}

@Transactional
public BonCommandeDTO changerStatut(Integer id, BonCommande.Statut statut) {
    // 1. Load purchase order
    // 2. Validate status transition
    // 3. Update status
    // 4. If RECU: call stockService.reapprovisionner(lignes)
    // 5. Save and return DTO
}
```

**Status Transition Rules**:
- EN_ATTENTE → ENVOYE or ANNULE
- ENVOYE → RECU
- RECU → no further transitions
- ANNULE → no further transitions

#### 5. CommandeService (Enhanced)

**Purpose**: Integrate stock management into order lifecycle

**Modifications**:

```java
@Transactional
public CommandeDTO ajouter(Commande c) {
    // Existing validation...
    // NEW: Call stockService.reserverStock(c.getLignesCommande())
    // Save order
    // Return DTO
}

@Transactional
public CommandeDTO changerStatut(Integer id, StatutCommande statut) {
    // Existing validation...
    // NEW: If changing to ANNULEE, call stockService.libererStock(lignes)
    // Update status
    // Save and return DTO
}
```

### Backend Controllers

#### 1. PaiementController (Enhanced)

**New Endpoints**:

```java
// POST /api/paiements/{id}/confirmer
// Confirms a payment (validates method and delivery status)
@PostMapping("/{id}/confirmer")
public ResponseEntity<PaiementDTO> confirmerPaiement(@PathVariable Integer id)

// GET /api/paiements/{id}/peut-confirmer
// Checks if payment can be confirmed (for UI logic)
@GetMapping("/{id}/peut-confirmer")
public ResponseEntity<Map<String, Boolean>> peutConfirmer(@PathVariable Integer id)
```

#### 2. LivraisonController (Enhanced)

**New Endpoints**:

```java
// POST /api/livraisons/depuis-commande
// Creates delivery from validated order
@PostMapping("/depuis-commande")
public ResponseEntity<LivraisonDTO> creerDepuisCommande(
    @RequestBody Map<String, Object> request)
// Request: { commandeId, cout, transporteurId? }

// PUT /api/livraisons/{id}/transporter/{transporteurId}
// Assigns transporter to delivery
@PutMapping("/{id}/transporteur/{transporteurId}")
public ResponseEntity<LivraisonDTO> assignerTransporteur(
    @PathVariable Integer id, 
    @PathVariable Integer transporteurId)

// PUT /api/livraisons/{id}/expedier
// Marks delivery as shipped
@PutMapping("/{id}/expedier")
public ResponseEntity<LivraisonDTO> expedier(@PathVariable Integer id)

// PUT /api/livraisons/{id}/livrer
// Marks delivery as delivered
@PutMapping("/{id}/livrer")
public ResponseEntity<LivraisonDTO> livrer(@PathVariable Integer id)
```

#### 3. StockController (New)

**Endpoints**:

```java
// GET /api/stock/faible
// Returns products with low stock
@GetMapping("/faible")
public ResponseEntity<List<ProduitDTO>> produitsStockFaible()

// GET /api/stock/produit/{id}
// Returns stock level for a product
@GetMapping("/produit/{id}")
public ResponseEntity<Map<String, Object>> niveauStock(@PathVariable Integer id)
```

#### 4. BonCommandeController (Enhanced)

**New Endpoints**:

```java
// POST /api/bons-commande
// Creates purchase order with lines
@PostMapping
public ResponseEntity<BonCommandeDTO> creer(@RequestBody BonCommandeRequest request)
// Request: { fournisseurId, lignes: [{produitId, quantite, prixUnitaire}] }

// PUT /api/bons-commande/{id}/envoyer
// Marks purchase order as sent
@PutMapping("/{id}/envoyer")
public ResponseEntity<BonCommandeDTO> envoyer(@PathVariable Integer id)

// PUT /api/bons-commande/{id}/recevoir
// Marks purchase order as received (updates stock)
@PutMapping("/{id}/recevoir")
public ResponseEntity<BonCommandeDTO> recevoir(@PathVariable Integer id)

// PUT /api/bons-commande/{id}/annuler
// Cancels purchase order
@PutMapping("/{id}/annuler")
public ResponseEntity<BonCommandeDTO> annuler(@PathVariable Integer id)
```

### Frontend Components

#### 1. Paiements Component (Enhanced)

**New Features**:
- Display "💵 À la livraison" badge for ESPECES payments
- Show "Confirm Payment" button only when:
  - Payment method is CARTE (always), OR
  - Payment method is ESPECES AND delivery status is LIVREE
- Disable button while processing
- Refresh list after confirmation

**Template Changes**:
```html
<td>
  <span *ngIf="p.methodePaiement === 'ESPECES' && p.statut === 'EN_ATTENTE'" 
        class="badge badge-info">💵 À la livraison</span>
  <button *ngIf="peutConfirmer(p)" 
          (click)="confirmerPaiement(p.id)"
          [disabled]="processingPaiementId === p.id">
    Confirmer
  </button>
</td>
```

**Component Logic**:
```typescript
peutConfirmer(paiement: Paiement): boolean {
  if (paiement.statut !== 'EN_ATTENTE') return false;
  if (paiement.methodePaiement === 'CARTE') return true;
  if (paiement.methodePaiement === 'ESPECES') {
    return paiement.commande?.livraison?.statut === 'LIVREE';
  }
  return false;
}
```

#### 2. Livraisons Component (New)

**Purpose**: Manage delivery lifecycle

**Features**:
- List all deliveries with status badges
- Filter by status
- Display order details, transporter, address
- Action buttons based on status:
  - EN_PREPARATION: "Assign Transporter", "Mark as Shipped"
  - EXPEDIEE: "Mark as Delivered"
  - LIVREE: No actions (completed)

**Template Structure**:
```html
<div class="livraisons-list">
  <table>
    <tr *ngFor="let l of livraisons">
      <td>{{ l.id }}</td>
      <td>{{ l.commande.id }}</td>
      <td>{{ l.adresse }}</td>
      <td>{{ l.transporteur?.nom || 'Non assigné' }}</td>
      <td><span [class]="getBadgeClass(l.statut)">{{ l.statut }}</span></td>
      <td>
        <button *ngIf="l.statut === 'EN_PREPARATION'" 
                (click)="expedier(l.id)">Expédier</button>
        <button *ngIf="l.statut === 'EXPEDIEE'" 
                (click)="livrer(l.id)">Marquer livrée</button>
      </td>
    </tr>
  </table>
</div>
```

#### 3. Commandes Component (Enhanced)

**New Features**:
- Display "Create Delivery" button for orders with status VALIDEE and no delivery
- Show delivery status badge if delivery exists
- Link to delivery details

**Template Addition**:
```html
<td>
  <button *ngIf="c.statut === 'VALIDEE' && !c.livraison" 
          (click)="creerLivraison(c.id)">
    Créer livraison
  </button>
  <span *ngIf="c.livraison" 
        [class]="getDeliveryBadgeClass(c.livraison.statut)">
    {{ c.livraison.statut }}
  </span>
</td>
```

#### 4. Stock Component (New)

**Purpose**: Display low stock alerts and manage purchase orders

**Features**:
- Dashboard widget showing low stock count
- List of products below threshold (10 units)
- "Create Purchase Order" button for each low stock product
- Display current quantity, threshold, supplier

**Template Structure**:
```html
<div class="stock-alerts">
  <h3>⚠️ Stock faible ({{ produitsStockFaible.length }})</h3>
  <table>
    <tr *ngFor="let p of produitsStockFaible">
      <td>{{ p.nom }}</td>
      <td>{{ p.quantiteEnStock }} / 10</td>
      <td>{{ p.fournisseur?.nom }}</td>
      <td>
        <button (click)="creerBonCommande(p)">
          Réapprovisionner
        </button>
      </td>
    </tr>
  </table>
</div>
```

#### 5. BonsCommande Component (New)

**Purpose**: Manage purchase orders to suppliers

**Features**:
- List all purchase orders with status
- Create new purchase order (select supplier, add products)
- Action buttons based on status:
  - EN_ATTENTE: "Send to Supplier", "Cancel"
  - ENVOYE: "Mark as Received"
  - RECU/ANNULE: No actions

**Template Structure**:
```html
<div class="bons-commande-list">
  <button (click)="showCreateForm()">Nouveau bon de commande</button>
  <table>
    <tr *ngFor="let bc of bonsCommande">
      <td>{{ bc.id }}</td>
      <td>{{ bc.fournisseur.nom }}</td>
      <td>{{ bc.dateCreation | date }}</td>
      <td>{{ bc.montantTotal | currency }}</td>
      <td><span [class]="getBadgeClass(bc.statut)">{{ bc.statut }}</span></td>
      <td>
        <button *ngIf="bc.statut === 'ENVOYE'" 
                (click)="recevoir(bc.id)">
          Marquer reçu
        </button>
      </td>
    </tr>
  </table>
</div>
```

---

## Data Models

### Entity Relationships

```
Client 1──N Commande 1──1 Paiement
                │
                1──1 Livraison N──1 Transporteur
                │
                1──N LigneCommande N──1 Produit
                                        │
                                        N──1 Fournisseur
                                        │
                                        1──N LigneBonCommande N──1 BonCommande
```

### Database Schema Changes

#### 1. Livraison Table (Existing - No Changes)

Current schema is sufficient:
- `id` (PK)
- `date_livraison` (TIMESTAMP)
- `adresse` (VARCHAR 255)
- `cout` (DECIMAL)
- `statut` (ENUM: EN_PREPARATION, EN_TRANSIT, LIVREE)
- `commande_id` (FK, UNIQUE)
- `transporteur_id` (FK, nullable)

**Note**: The enum value `EN_TRANSIT` in the entity should be renamed to `EXPEDIEE` to match requirements, or we map EXPEDIEE to EN_TRANSIT in the business logic.

#### 2. Paiement Table (Existing - No Changes)

Current schema is sufficient:
- `id` (PK)
- `date_paiement` (TIMESTAMP)
- `montant` (DECIMAL)
- `methode_paiement` (ENUM: CARTE, ESPECES, VIREMENT)
- `statut` (ENUM: EN_ATTENTE, VALIDE, REFUSE)
- `commande_id` (FK, UNIQUE)

#### 3. BonCommande Table (Existing - No Changes)

Current schema is sufficient:
- `id` (PK)
- `date_creation` (TIMESTAMP)
- `statut` (ENUM: EN_ATTENTE, ENVOYE, RECU, ANNULE)
- `fournisseur_id` (FK)

#### 4. Produit Table (Existing - No Changes)

Current schema is sufficient:
- `id` (PK)
- `nom` (VARCHAR 100)
- `description` (VARCHAR 500)
- `prix_unitaire` (DECIMAL)
- `quantite_en_stock` (INTEGER)
- `image_url` (VARCHAR 255)
- `categorie_id` (FK)

**Stock Management**: The `quantite_en_stock` field will be updated by StockService.

### DTOs

#### 1. LivraisonDTO (Enhanced)

```java
public class LivraisonDTO {
    private Integer id;
    private LocalDateTime dateLivraison;
    private String adresse;
    private Double cout;
    private String statut; // EN_PREPARATION, EXPEDIEE, LIVREE
    private Integer commandeId;
    private TransporteurDTO transporteur; // Include transporter details
}
```

#### 2. PaiementDTO (Enhanced)

```java
public class PaiementDTO {
    private Integer id;
    private LocalDateTime datePaiement;
    private Double montant;
    private String methodePaiement; // CARTE, ESPECES
    private String statut; // EN_ATTENTE, VALIDE, REFUSE
    private Integer commandeId;
    private LivraisonDTO livraison; // Include for cash payment validation
}
```

#### 3. BonCommandeDTO (New)

```java
public class BonCommandeDTO {
    private Integer id;
    private LocalDateTime dateCreation;
    private String statut; // EN_ATTENTE, ENVOYE, RECU, ANNULE
    private FournisseurDTO fournisseur;
    private List<LigneBonCommandeDTO> lignes;
    private Double montantTotal;
}
```

#### 4. LigneBonCommandeDTO (New)

```java
public class LigneBonCommandeDTO {
    private Integer id;
    private Integer quantite;
    private Double prixUnitaire;
    private ProduitDTO produit;
}
```

#### 5. StockAlertDTO (New)

```java
public class StockAlertDTO {
    private ProduitDTO produit;
    private Integer quantiteActuelle;
    private Integer seuilMinimum;
    private FournisseurDTO fournisseur;
}
```

### Request Models

#### 1. CreerLivraisonRequest

```java
public class CreerLivraisonRequest {
    private Integer commandeId;
    private Double cout;
    private Integer transporteurId; // Optional
}
```

#### 2. CreerBonCommandeRequest

```java
public class CreerBonCommandeRequest {
    private Integer fournisseurId;
    private List<LigneBonCommandeRequest> lignes;
}

public class LigneBonCommandeRequest {
    private Integer produitId;
    private Integer quantite;
    private Double prixUnitaire;
}
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, I identified the following redundancies:

**Redundant Properties Eliminated:**
- 5.4 (rollback on failure) is redundant with 5.3 (transaction atomicity)
- 6.4 (payment status update) is redundant with 1.2 (payment confirmation behavior)
- 7.3 (Stripe payment triggers order validation) is redundant with 1.2
- 7.5 (persist payment changes) is covered by transaction properties
- 10.5 (terminal state rejection) is redundant with 10.3
- 11.3 (rollback on failure) is redundant with 11.2
- 12.5 (rollback on failure) is redundant with 12.4
- 13.3 (rollback on failure) is redundant with 13.2
- 14.5 (error handling) is covered by specific transition properties
- 15.2 (VALIDE immutability) is redundant with 1.5
- 15.4 (error handling) is covered by specific transition properties
- 15.5 (validation before persist) is covered by other properties
- 16.2 (duplicate delivery) is redundant with 2.3
- 16.4 (database constraint) is redundant with 16.1
- 16.5 (database constraint) is redundant with 2.3
- 17.4 (persist cost) is a basic requirement covered by other properties

**Combined Properties:**
- 2.4 and 2.7 can be combined into a single property about delivery initialization
- 3.4 and 3.5 can be combined into a property about transporter assignment invariants
- 6.1, 6.2, and 6.3 can be combined into a comprehensive cash vs card payment property
- 14.1, 14.2, 14.3, and 14.4 can be combined into a comprehensive order status transition property

### Property 1: Payment Confirmation Transaction Atomicity

*For any* payment with status EN_ATTENTE, when confirmed to VALIDE, both the payment status and the associated order status SHALL be updated to VALIDE and VALIDEE respectively within a single atomic transaction, such that either both updates succeed or both fail.

**Validates: Requirements 1.1, 1.2, 1.4**

### Property 2: Payment Confirmation Error Handling

*For any* payment confirmation attempt that violates business rules (e.g., already VALIDE, invalid method/delivery combination), the system SHALL reject the operation and return a descriptive error message without modifying any state.

**Validates: Requirements 1.3, 1.5**

### Property 3: Delivery Creation from Validated Orders

*For any* order with status VALIDEE and no existing delivery, creating a delivery SHALL succeed and initialize the delivery with: (1) address matching the order's delivery address, (2) status EN_PREPARATION, (3) cost equal to the provided value or 0.0 if not provided, and (4) a reference to the order.

**Validates: Requirements 2.1, 2.4, 2.5, 2.6, 2.7**

### Property 4: Delivery Creation Rejection Rules

*For any* order that either (1) has status other than VALIDEE, or (2) already has an existing delivery, attempting to create a delivery SHALL be rejected with an error message.

**Validates: Requirements 2.2, 2.3**

### Property 5: Transporter Assignment Invariants

*For any* delivery with status EN_PREPARATION, assigning a valid transporter SHALL succeed and result in: (1) the delivery referencing the assigned transporter, and (2) the delivery status remaining unchanged.

**Validates: Requirements 3.1, 3.3, 3.4, 3.5**

### Property 6: Transporter Assignment Rejection

*For any* delivery with status LIVREE, attempting to assign or reassign a transporter SHALL be rejected with an error message.

**Validates: Requirements 3.2**

### Property 7: Delivery Status Transition Rules

*For any* delivery, status transitions SHALL follow these rules: (1) EN_PREPARATION may transition only to EXPEDIEE, (2) EXPEDIEE may transition only to LIVREE, (3) LIVREE may not transition to any other status, and (4) invalid transitions SHALL be rejected with an error message.

**Validates: Requirements 4.1, 4.2, 4.3, 4.4**

### Property 8: Delivery Status Change Timestamp Update

*For any* delivery status change, the system SHALL update the delivery's timestamp to reflect the current time of the status change.

**Validates: Requirements 4.6**

### Property 9: Delivery-to-Order Status Synchronization

*For any* delivery status change to EXPEDIEE or LIVREE, the associated order status SHALL be updated to EXPEDIEE or LIVREE respectively within the same atomic transaction, such that either both updates succeed or both fail.

**Validates: Requirements 5.1, 5.2, 5.3**

### Property 10: Cancelled Order Delivery Protection

*For any* order with status ANNULEE, attempting to update the associated delivery status SHALL be rejected with an error message.

**Validates: Requirements 5.5**

### Property 11: Cash vs Card Payment Confirmation Rules

*For any* payment, confirmation SHALL be allowed if and only if: (1) the payment method is CARTE (regardless of delivery status), OR (2) the payment method is ESPECES AND the associated delivery has status LIVREE. All other combinations SHALL be rejected.

**Validates: Requirements 6.1, 6.2, 6.3**

### Property 12: Cash Payment Confirmation Timestamp Update

*For any* cash payment (method ESPECES) that is confirmed, the payment date SHALL be updated to the current timestamp.

**Validates: Requirements 6.5**

### Property 13: Card Payment Timestamp Update

*For any* card payment (method CARTE) that is confirmed via Stripe, the payment date SHALL be updated to the current timestamp.

**Validates: Requirements 7.2**

### Property 14: Low Stock Detection

*For any* product with quantiteEnStock less than 10, the product SHALL be flagged as low stock and included in the low stock products list.

**Validates: Requirements 8.1, 8.2**

### Property 15: Low Stock Query Result Structure

*For any* product returned in the low stock query, the result SHALL include the product details, current quantity, and supplier information.

**Validates: Requirements 8.3**

### Property 16: Low Stock Status Re-evaluation

*For any* product whose quantity is updated, the low stock status SHALL be re-evaluated such that the product appears in the low stock list if and only if the new quantity is less than 10.

**Validates: Requirements 8.5**

### Property 17: Purchase Order Creation Validation

*For any* purchase order creation attempt, the system SHALL validate that: (1) the supplier exists, (2) all product lines reference products supplied by that supplier, and (3) all quantities are positive. Violations SHALL result in rejection with an error message.

**Validates: Requirements 9.1, 9.5, 9.6**

### Property 18: Purchase Order Initialization

*For any* newly created purchase order, the system SHALL initialize: (1) status to EN_ATTENTE, (2) creation date to the current timestamp, and (3) total amount to the sum of (quantity × price) for all product lines.

**Validates: Requirements 9.2, 9.3, 9.7**

### Property 19: Purchase Order Status Transition Rules

*For any* purchase order, status transitions SHALL follow these rules: (1) EN_ATTENTE may transition to ENVOYE or ANNULE, (2) ENVOYE may transition only to RECU, (3) RECU and ANNULE may not transition to any other status, and (4) invalid transitions SHALL be rejected with an error message.

**Validates: Requirements 10.1, 10.2, 10.3, 10.4**

### Property 20: Purchase Order Receipt Stock Update

*For any* purchase order transitioning to RECU, the system SHALL increase the quantiteEnStock of each product in the purchase order by the ordered quantity, within the same atomic transaction as the status change, such that either both updates succeed or both fail.

**Validates: Requirements 11.1, 11.2**

### Property 21: Purchase Order Receipt Stock Invariant

*For any* product in a purchase order that transitions to RECU, the stock quantity after receipt SHALL equal the stock quantity before receipt plus the ordered quantity.

**Validates: Requirements 11.4**

### Property 22: Purchase Order Receipt Idempotence

*For any* purchase order, marking it as RECU multiple times SHALL update stock only on the first transition to RECU, ensuring that subsequent attempts do not further increase stock quantities.

**Validates: Requirements 11.5**

### Property 23: Order Creation Stock Reservation

*For any* order creation, the system SHALL decrease the quantiteEnStock of each ordered product by the ordered quantity, within the same atomic transaction as the order creation, such that either both updates succeed or both fail.

**Validates: Requirements 12.1, 12.4**

### Property 24: Order Creation Stock Validation

*For any* order creation attempt, the system SHALL validate that all products have sufficient stock (quantiteEnStock ≥ ordered quantity) before decreasing any quantities. If any product has insufficient stock, the entire order creation SHALL be rejected with an error message.

**Validates: Requirements 12.2, 12.3**

### Property 25: Order Cancellation Stock Release

*For any* order transitioning to ANNULEE, the system SHALL increase the quantiteEnStock of each ordered product by the ordered quantity, within the same atomic transaction as the status change, such that either both updates succeed or both fail.

**Validates: Requirements 13.1, 13.2**

### Property 26: Order Cancellation Stock Invariant

*For any* product in an order that transitions to ANNULEE, the stock quantity after cancellation SHALL equal the stock quantity before cancellation plus the ordered quantity.

**Validates: Requirements 13.4**

### Property 27: Order Cancellation Idempotence

*For any* order, cancelling it multiple times SHALL release stock only on the first transition to ANNULEE, ensuring that subsequent attempts do not further increase stock quantities.

**Validates: Requirements 13.5**

### Property 28: Order Status Transition Rules

*For any* order, status transitions SHALL follow these rules: (1) EN_ATTENTE may transition to VALIDEE or ANNULEE, (2) VALIDEE may transition to EXPEDIEE or ANNULEE, (3) EXPEDIEE may transition only to LIVREE, (4) LIVREE and ANNULEE may not transition to any other status, and (5) invalid transitions SHALL be rejected with an error message.

**Validates: Requirements 14.1, 14.2, 14.3, 14.4**

### Property 29: Payment Status Transition Rules

*For any* payment, status transitions SHALL follow these rules: (1) EN_ATTENTE may transition to VALIDE or REFUSE, (2) VALIDE may not transition to any other status, (3) REFUSE may transition to EN_ATTENTE for retry, and (4) invalid transitions SHALL be rejected with an error message.

**Validates: Requirements 15.1, 15.3**

### Property 30: One Payment Per Order Enforcement

*For any* order that already has a payment with status VALIDE, attempting to create another payment SHALL be rejected with an error message.

**Validates: Requirements 16.1**

### Property 31: Cascade Deletion of Order Dependencies

*For any* order that is deleted, the associated payment and delivery (if they exist) SHALL also be deleted within the same transaction.

**Validates: Requirements 16.3**

### Property 32: Delivery Cost Validation

*For any* delivery creation or cost update, the system SHALL validate that the cost is zero or positive. Negative costs SHALL be rejected with an error message.

**Validates: Requirements 17.2**

### Property 33: Delivery Cost Default Value

*For any* delivery created without a specified cost, the system SHALL default the cost to 0.0.

**Validates: Requirements 17.3**

### Property 34: Delivery Cost Update During Preparation

*For any* delivery with status EN_PREPARATION, updating the delivery cost SHALL succeed. Deliveries with other statuses SHALL reject cost updates.

**Validates: Requirements 17.5**

---

## Error Handling

### Error Categories

**1. Validation Errors (HTTP 400 Bad Request)**
- Invalid status transitions
- Missing required fields
- Negative quantities or costs
- Insufficient stock
- Invalid payment method/delivery status combinations

**2. Not Found Errors (HTTP 404 Not Found)**
- Order, payment, delivery, product, supplier, or transporter not found
- Used when referencing non-existent entities

**3. Conflict Errors (HTTP 409 Conflict)**
- Duplicate payment for order
- Duplicate delivery for order
- Attempting to modify immutable entities (VALIDE payment, LIVREE delivery)

**4. Transaction Errors (HTTP 500 Internal Server Error)**
- Database transaction failures
- Rollback scenarios
- Unexpected persistence errors

### Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for product: Laptop (requested: 5, available: 2)",
  "path": "/api/commandes"
}
```

### Specific Error Scenarios

**Payment Confirmation Errors:**
- "Payment already VALIDE and cannot be modified"
- "Cash payment cannot be confirmed before delivery completion"
- "Payment not found with ID: {id}"

**Delivery Creation Errors:**
- "Order must be VALIDEE to create delivery"
- "Order already has a delivery"
- "Transporter not found with ID: {id}"

**Stock Management Errors:**
- "Insufficient stock for product: {name} (requested: {qty}, available: {stock})"
- "Product {name} is not supplied by supplier {supplierName}"
- "Quantity must be positive"

**Status Transition Errors:**
- "Invalid status transition from {current} to {target}"
- "Cannot update delivery for cancelled order"
- "Terminal status {status} cannot be changed"

### Transaction Rollback Strategy

All service methods use `@Transactional` annotation to ensure:
1. **Atomicity**: All database changes succeed or all fail
2. **Consistency**: Business rules are enforced before commit
3. **Isolation**: Concurrent transactions don't interfere
4. **Durability**: Committed changes are permanent

**Rollback Triggers:**
- Any uncaught exception
- Validation failures (via `ResponseStatusException`)
- Database constraint violations
- Stock insufficiency

---

## Testing Strategy

### Dual Testing Approach

This feature requires both **unit tests** and **property-based tests** for comprehensive coverage:

**Unit Tests** focus on:
- Specific examples and edge cases
- Integration points between services
- Error conditions and exception handling
- Stripe API integration (with mocks)
- Controller endpoint behavior

**Property-Based Tests** focus on:
- Universal properties across all inputs
- Comprehensive input coverage through randomization
- Invariant validation (stock calculations, status transitions)
- Idempotence properties (duplicate operations)
- Transaction atomicity

### Property-Based Testing Configuration

**Library Selection:**
- **Java**: Use **jqwik** (modern property-based testing for JUnit 5)
- **TypeScript/Frontend**: Use **fast-check** (property-based testing for JavaScript/TypeScript)

**Test Configuration:**
- Minimum **100 iterations** per property test
- Each test tagged with reference to design property
- Tag format: `@Tag("Feature: complete-order-delivery-workflow, Property {number}: {property_text}")`

**Example Property Test Structure:**

```java
@Property
@Tag("Feature: complete-order-delivery-workflow, Property 1: Payment Confirmation Transaction Atomicity")
void paymentConfirmationUpdatesOrderAtomically(@ForAll("validPayments") Paiement payment) {
    // Arrange: Create order and payment in EN_ATTENTE
    // Act: Confirm payment
    // Assert: Both payment and order status updated, or both unchanged on failure
}
```

### Unit Test Coverage

**PaiementService Tests:**
- `testConfirmerPaiement_CartePayment_Success()`
- `testConfirmerPaiement_EspecesBeforeDelivery_Rejected()`
- `testConfirmerPaiement_EspecesAfterDelivery_Success()`
- `testConfirmerPaiement_AlreadyValide_Rejected()`
- `testChangerStatut_TransactionRollback_OnFailure()`

**LivraisonService Tests:**
- `testCreerDepuisCommande_ValideeOrder_Success()`
- `testCreerDepuisCommande_EnAttenteOrder_Rejected()`
- `testCreerDepuisCommande_DuplicateDelivery_Rejected()`
- `testAssignerTransporteur_EnPreparation_Success()`
- `testAssignerTransporteur_Livree_Rejected()`
- `testChangerStatut_SynchronizesOrderStatus()`
- `testChangerStatut_InvalidTransition_Rejected()`

**StockService Tests:**
- `testReserverStock_SufficientStock_Success()`
- `testReserverStock_InsufficientStock_Rejected()`
- `testReserverStock_PartialFailure_RollbackAll()`
- `testLibererStock_RestoresQuantities()`
- `testReapprovisionner_IncreasesStock()`
- `testProduitsStockFaible_ReturnsCorrectProducts()`

**BonCommandeService Tests:**
- `testCreer_ValidSupplier_Success()`
- `testCreer_InvalidSupplier_Rejected()`
- `testCreer_WrongSupplierProduct_Rejected()`
- `testChangerStatut_ToRecu_UpdatesStock()`
- `testChangerStatut_InvalidTransition_Rejected()`

**CommandeService Tests:**
- `testAjouter_ReservesStock()`
- `testAjouter_InsufficientStock_Rejected()`
- `testChangerStatut_ToAnnulee_ReleasesStock()`
- `testChangerStatut_ValidTransitions_Success()`
- `testChangerStatut_InvalidTransitions_Rejected()`

### Integration Tests

**End-to-End Workflow Tests:**
1. **Complete Order Lifecycle**: Create order → Confirm payment → Create delivery → Ship → Deliver
2. **Cash Payment Workflow**: Create order → Create delivery → Deliver → Confirm cash payment
3. **Order Cancellation**: Create order → Cancel → Verify stock released
4. **Purchase Order Workflow**: Create purchase order → Send → Receive → Verify stock updated
5. **Low Stock Alert**: Reduce stock → Verify alert → Create purchase order → Receive → Verify alert cleared

**Transaction Rollback Tests:**
- Simulate database failures during multi-entity updates
- Verify all changes rolled back
- Verify system state remains consistent

### Frontend Component Tests

**Paiements Component:**
- Test `peutConfirmer()` logic for different payment methods and delivery statuses
- Test button visibility based on payment state
- Test confirmation action and UI updates

**Livraisons Component:**
- Test delivery list rendering
- Test status badge display
- Test action button visibility based on delivery status
- Test status change actions

**Stock Component:**
- Test low stock alert display
- Test purchase order creation flow
- Test stock level updates after purchase order receipt

### Test Data Generators

**For Property-Based Tests:**

```java
@Provide
Arbitrary<Paiement> validPayments() {
    return Combinators.combine(
        Arbitraries.integers().between(1, 1000),
        Arbitraries.of(MethodePaiement.values()),
        Arbitraries.of(StatutPaiement.EN_ATTENTE)
    ).as((id, method, status) -> {
        Paiement p = new Paiement();
        p.setId(id);
        p.setMethodePaiement(method);
        p.setStatut(status);
        // ... set other fields
        return p;
    });
}

@Provide
Arbitrary<Commande> validatedOrders() {
    return Arbitraries.integers().between(1, 1000).map(id -> {
        Commande c = new Commande();
        c.setId(id);
        c.setStatut(StatutCommande.VALIDEE);
        // ... set other fields
        return c;
    });
}
```

### Performance Testing

**Load Tests:**
- Concurrent order creation with stock reservation
- Concurrent payment confirmations
- Concurrent delivery status updates

**Stress Tests:**
- High-volume order processing
- Rapid stock updates from multiple purchase orders
- Database connection pool exhaustion scenarios

---

