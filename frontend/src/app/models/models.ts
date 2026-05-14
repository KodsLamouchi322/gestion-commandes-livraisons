export type UserRole = 'CLIENT' | 'ADMIN';

export interface AuthResponse {
    token: string;
    type?: string;
    id: number;
    email: string;
    nom: string;
    prenom?: string;
    role: UserRole;
    // Alias pour compatibilité
    clientId?: number;
}

// ── CLIENT ────────────────────────────────────────────────────
// Le backend renvoie ClientDTO : jamais de motDePasse en réponse.
// motDePasse reste optionnel ici pour les formulaires de création/modification.
export interface Client {
    id?: number;
    nom: string;
    prenom?: string;
    email: string;
    telephone?: string;
    adresse: string;
    motDePasse?: string;   // Envoyé au backend uniquement (création/modif), jamais reçu
    role?: UserRole;
}

export interface Categorie {
    id?: number;
    nom: string;
    description?: string;
}

export interface Produit {
    id?: number;
    nom: string;
    description?: string;
    prixUnitaire: number;
    quantiteEnStock: number;
    imageUrl?: string;
    categorie?: Categorie;
    noteMoyenne?: number;
}

export interface Avis {
    id?: number;
    client?: Client;
    produit?: Produit;
    note: number;
    commentaire?: string;
    date?: string;
    dateAvis?: string;
}

export interface LignePanier {
    id?: number;
    produit: Produit;
    quantite: number;
    sousTotal?: number;
}

export interface Panier {
    id?: number;
    client?: Client;
    lignes: LignePanier[];
    total?: number;
}

// ── COMMANDE ──────────────────────────────────────────────────
// Le backend renvoie CommandeDTO :
//   - client est remplacé par clientId + clientNom + clientEmail
//   - lignesCommande est une liste de LigneCommandeDTO
// On garde client? optionnel pour la compatibilité des formulaires
// (ex: commanderPanier() envoie { client: { id } } au backend)
export interface Commande {
    id?: number;
    // Champs renvoyés par le backend (CommandeDTO)
    clientId?: number;
    clientNom?: string;
    clientEmail?: string;
    // Compatibilité : certains formulaires construisent { client: { id } }
    client?: Partial<Client>;
    dateCommande?: string;
    date?: string;  // alias legacy
    statut?: 'EN_ATTENTE' | 'VALIDEE' | 'EXPEDIEE' | 'LIVREE' | 'ANNULEE';
    montantTotal?: number;
    adresseLivraison?: string;
    lignesCommande?: LigneCommandeDTO[];
}

// ── LIGNE COMMANDE ────────────────────────────────────────────
// Utilisée pour envoyer une ligne au backend (POST /api/lignes-commande)
export interface LigneCommande {
    id?: number;
    commande?: Commande | { id?: number };
    produit: Produit;
    quantite: number;
    prixUnitaire: number;
    sousTotal?: number;
}

// DTO reçu dans CommandeDTO.lignesCommande
export interface LigneCommandeDTO {
    id?: number;
    quantite: number;
    prixUnitaire: number;
    sousTotal?: number;
    produitId?: number;
    produitNom?: string;
    produitPrix?: number;
}

// ── TRANSPORTEUR ──────────────────────────────────────────────
// Le backend renvoie TransporteurDTO avec nombreLivraisons.
// note est supprimé du backend → optionnel ici pour la rétrocompatibilité du template.
export interface Transporteur {
    id?: number;
    nom: string;
    telephone: string;
    email?: string;
    note?: number;             // Utilisé dans le formulaire, ignoré par le backend
    nombreLivraisons?: number; // Renvoyé par TransporteurDTO
}

// ── LIVRAISON ─────────────────────────────────────────────────
// Le backend renvoie LivraisonDTO :
//   - commande est remplacé par commandeId
//   - transporteur est remplacé par transporteurId + transporteurNom
export interface Livraison {
    id?: number;
    // Champs renvoyés par le backend (LivraisonDTO)
    commandeId?: number;
    transporteurId?: number;
    transporteurNom?: string;
    // Compatibilité : createLivraison() envoie { commande: { id }, transporteur: { id } }
    commande?: Partial<Commande> | { id?: number };
    transporteur?: Partial<Transporteur> | { id?: number };
    dateLivraison?: string;
    adresse?: string;
    cout: number;
    statut?: 'EN_PREPARATION' | 'EN_TRANSIT' | 'LIVREE';
}

// ── PAIEMENT ──────────────────────────────────────────────────
// Le backend renvoie PaiementDTO :
//   - commande est remplacé par commandeId
export interface Paiement {
    id?: number;
    // Champs renvoyés par le backend (PaiementDTO)
    commandeId?: number;
    // Compatibilité : createPaiement() envoie { commande: { id } }
    commande?: Partial<Commande> | { id?: number };
    datePaiement?: string;
    date?: string;  // alias legacy
    montant?: number;
    statut?: 'EN_ATTENTE' | 'VALIDE' | 'REFUSE';
    methodePaiement: 'CARTE' | 'VIREMENT' | 'ESPECES';
}

export interface Fournisseur {
    id?: number;
    nom: string;
    email: string;
    telephone: string;
    adresse: string;
}

export interface BonCommande {
    id?: number;
    fournisseur: Fournisseur;
    dateCreation?: string;
    statut?: 'EN_ATTENTE' | 'ENVOYE' | 'RECU' | 'ANNULE';
    lignes?: LigneBonCommande[];
}

export interface LigneBonCommande {
    id?: number;
    bonCommande?: BonCommande | { id: number };
    produit?: Produit;
    quantite: number;
    prixAchat: number;
}