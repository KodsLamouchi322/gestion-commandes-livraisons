# 🛒 Système de Gestion des Commandes et Livraisons

Application web complète de gestion des commandes, livraisons et paiements, développée dans le cadre d'un projet JEE.

---

## 📋 Description du projet

Cette application permet de gérer l'ensemble du cycle de vie d'une commande :
- Gestion des **clients** et de leur espace personnel
- Gestion du **catalogue produits** avec catégories et images
- Gestion du **panier** et **passation de commandes**
- Suivi des **livraisons** avec mise à jour automatique des statuts
- Gestion des **paiements** (manuel + Stripe Checkout en ligne)
- Gestion des **bons de commande fournisseurs** avec mise à jour automatique des stocks
- Tableau de bord **admin** complet

---

## 🧱 Technologies utilisées

### Back-end
| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | 3.x | Framework principal |
| Spring Security | 6.x | Authentification JWT |
| Spring Data JPA | 3.x | Accès base de données |
| Hibernate | 6.x | ORM |
| MySQL / MariaDB | 8+ / latest | Base de données relationnelle |
| Lombok | latest | Réduction du boilerplate |
| SpringDoc OpenAPI | 2.x | Documentation Swagger |
| Stripe Java SDK | latest | Paiements en ligne |
| Maven | 3.9+ | Build & gestion des dépendances |

### Front-end
| Technologie | Version | Rôle |
|---|---|---|
| Angular | 17+ | Framework SPA |
| TypeScript | 5.x | Langage |
| CSS Vanilla | — | Styles |
| Nginx | alpine | Serveur web en production |

### DevOps
| Outil | Rôle |
|---|---|
| Docker | Containerisation |
| Docker Compose | Orchestration des services |

---

## 🏗️ Architecture

```
projetJEE/
├── backend/commandes/          # API REST Spring Boot
│   ├── controller/             # Endpoints HTTP
│   ├── service/                # Logique métier
│   ├── repository/             # Accès données (Spring Data JPA)
│   ├── entity/                 # Entités JPA (modèle BDD)
│   ├── dto/                    # Objets de transfert
│   ├── converter/              # Entité ↔ DTO
│   ├── config/                 # Security, CORS, Swagger
│   └── security/               # JWT Filter & Service
├── frontend/                   # SPA Angular
│   ├── src/app/components/     # 18 composants UI
│   ├── src/app/services/       # Services Angular (API, Auth)
│   ├── src/app/guards/         # Protection des routes
│   └── src/app/interceptors/   # JWT HTTP interceptor
├── docker-compose.yml          # Orchestration 3 services
└── README.md
```

---

## 🚀 Installation et exécution

### Prérequis
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé et démarré

### ▶️ Lancement avec Docker (recommandé)

```bash
# Cloner le dépôt
git clone <url-du-repo>
cd projetJEE

# Lancer l'application complète (3 services)
docker-compose up --build
```

L'application est accessible sur :
- **Frontend (UI)** : http://localhost:8080
- **Backend (API)** : http://localhost:8085
- **Swagger UI** : http://localhost:8085/swagger-ui.html

> Le premier lancement prend quelques minutes (build Maven + téléchargement des images Docker).

### ⚙️ Lancement en développement local

#### Prérequis locaux
- Java 17, Maven 3.9+
- Node.js 20+, Angular CLI
- MySQL 8+ avec une base de données `gestion_commandes_v2`

#### Back-end
```bash
cd backend/commandes
mvn spring-boot:run
```
API disponible sur http://localhost:8085

#### Front-end
```bash
cd frontend
npm install
ng serve
```
UI disponible sur http://localhost:4200

---

## 🔑 Comptes par défaut

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | admin@gestion.com | admin123 |
| Client | Créer via `/auth/register` | — |

---

## 🌐 Variables d'environnement (Docker)

| Variable | Description | Défaut |
|---|---|---|
| `JWT_SECRET` | Secret JWT (changer en prod) | valeur par défaut dev |
| `STRIPE_SECRET_KEY` | Clé secrète Stripe | vide (désactive Stripe) |
| `STRIPE_WEBHOOK_SECRET` | Secret webhook Stripe | vide |
| `FRONTEND_URL` | URL du frontend | http://localhost:4200 |
| `UPLOAD_DIR` | Répertoire upload images | uploads |

---

## 📚 Documentation API

La documentation Swagger est disponible après démarrage :
- **Local** : http://localhost:8085/swagger-ui.html
- **Docker** : http://localhost:8085/swagger-ui.html

---

## 📦 Entités principales

| Entité | Description |
|---|---|
| `Client` | Utilisateur du système (CLIENT ou ADMIN) |
| `Commande` | Commande passée par un client |
| `LigneCommande` | Ligne de produit dans une commande |
| `Livraison` | Livraison associée à une commande |
| `Transporteur` | Société de transport |
| `Paiement` | Paiement d'une commande |
| `Produit` | Article du catalogue |
| `Categorie` | Catégorie de produits |
| `BonCommande` | Bon de commande fournisseur |
| `Fournisseur` | Fournisseur de produits |

---

## ✅ Fonctionnalités principales

- ✅ CRUD complet sur toutes les entités
- ✅ Authentification JWT (inscription / connexion)
- ✅ Validation des données avec Spring Validator (`@Valid`, `@NotBlank`, `@Email`, etc.)
- ✅ Création et validation des bons de commande fournisseur
- ✅ Mise à jour automatique des stocks à la réception
- ✅ Historique des commandes par fournisseur
- ✅ Suivi des commandes et livraisons (transitions de statut automatiques)
- ✅ Gestion des paiements en ligne via **Stripe Checkout**
- ✅ Déploiement Docker avec docker-compose
- ✅ Documentation API avec Swagger/OpenAPI
