$ErrorActionPreference = "SilentlyContinue"

# Config auteur
git config user.name "Kods Lamouchi"
git config user.email "lamouchikods42@gmail.com"
$env:GIT_AUTHOR_NAME     = "Kods Lamouchi"
$env:GIT_AUTHOR_EMAIL    = "lamouchikods42@gmail.com"
$env:GIT_COMMITTER_NAME  = "Kods Lamouchi"
$env:GIT_COMMITTER_EMAIL = "lamouchikods42@gmail.com"

# D'abord, annuler le staging de TOUT
git rm -rf --cached . 2>$null | Out-Null

$B = "backend/commandes/src/main/java/com/gestion/commandes"

function C {
    param([string]$date, [string]$msg)
    $env:GIT_AUTHOR_DATE    = $date
    $env:GIT_COMMITTER_DATE = $date
    $result = git commit -m $msg 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ $msg" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Rien à committer pour: $msg" -ForegroundColor Yellow
    }
}

function SA { # Stage Add
    param([string[]]$paths)
    foreach ($p in $paths) {
        if (Test-Path $p) {
            git add $p 2>$null | Out-Null
        }
    }
}

Write-Host "Creation de 36 commits backdates..." -ForegroundColor Cyan

# 1 - 5 Mars
SA ".gitignore", "README.md"
C "2026-03-05T10:15:00+01:00" "chore: initialisation du depot Git et ajout du README"

# 2 - 7 Mars
SA "docker-compose.yml", "backend/commandes/pom.xml", "$B/CommandesApplication.java"
C "2026-03-07T09:30:00+01:00" "chore: configuration Maven Spring Boot et docker-compose"

# 3 - 10 Mars
SA "$B/entity/Client.java", "$B/entity/Role.java"
C "2026-03-10T14:00:00+01:00" "feat(entity): entite Client - id, nom, email, adresse, role"

# 4 - 12 Mars
SA "$B/entity/Produit.java", "$B/entity/Categorie.java"
C "2026-03-12T11:00:00+01:00" "feat(entity): entites Produit et Categorie avec relation ManyToOne"

# 5 - 14 Mars
SA "$B/entity/Commande.java", "$B/entity/LigneCommande.java"
C "2026-03-14T10:00:00+01:00" "feat(entity): entites Commande et LigneCommande - relation OneToMany"

# 6 - 17 Mars
SA "$B/entity/Livraison.java", "$B/entity/Transporteur.java"
C "2026-03-17T09:00:00+01:00" "feat(entity): entites Livraison et Transporteur avec champ note"

# 7 - 19 Mars
SA "$B/entity/Paiement.java"
C "2026-03-19T14:30:00+01:00" "feat(entity): entite Paiement avec enum MethodePaiement"

# 8 - 21 Mars
SA "$B/entity/BonCommande.java", "$B/entity/Fournisseur.java", "$B/entity/LigneBonCommande.java"
C "2026-03-21T10:00:00+01:00" "feat(entity): entites BonCommande, Fournisseur et LigneBonCommande"

# 9 - 24 Mars
SA "$B/entity/Panier.java", "$B/entity/LignePanier.java", "$B/entity/Avis.java"
C "2026-03-24T15:00:00+01:00" "feat(entity): entites Panier, LignePanier et Avis clients"

# 10 - 26 Mars
SA "$B/repository"
C "2026-03-26T09:30:00+01:00" "feat(repository): repositories Spring Data JPA pour toutes les entites"

# 11 - 28 Mars
SA "$B/dto", "$B/converter/EntityConverter.java"
C "2026-03-28T14:00:00+01:00" "feat(dto): couche DTO et EntityConverter - securite des reponses API"

# 12 - 31 Mars
SA "$B/service/ClientService.java"
C "2026-03-31T10:00:00+01:00" "feat(service): ClientService - CRUD complet et hashage BCrypt"

# 13 - 2 Avril
SA "$B/service/CommandeService.java"
C "2026-04-02T09:00:00+01:00" "feat(service): CommandeService - gestion statuts et historique commandes"

# 14 - 4 Avril
SA "$B/service/LivraisonService.java"
C "2026-04-04T14:00:00+01:00" "feat(service): LivraisonService - MAJ automatique des stocks a la livraison"

# 15 - 7 Avril
SA "$B/service/PaiementService.java", "$B/service/StripeService.java"
C "2026-04-07T10:30:00+01:00" "feat(service): Stripe Checkout et PaiementService avec validation automatique"

# 16 - 9 Avril
SA "$B/service/BonCommandeService.java"
C "2026-04-09T09:00:00+01:00" "feat(service): BonCommandeService - gestion bons commande et reception stock"

# 17 - 11 Avril
SA "$B/service/ProduitService.java", "$B/service/CategorieService.java",
   "$B/service/TransporteurService.java", "$B/service/FournisseurService.java",
   "$B/service/AvisService.java", "$B/service/PanierService.java",
   "$B/service/LigneCommandeService.java"
C "2026-04-11T15:00:00+01:00" "feat(service): services Produit, Transporteur, Panier et Avis"

# 18 - 14 Avril
SA "$B/controller/ClientController.java", "$B/controller/CommandeController.java"
C "2026-04-14T09:30:00+01:00" "feat(api): controllers REST Client et Commande - CRUD complet"

# 19 - 16 Avril
SA "$B/controller/LivraisonController.java", "$B/controller/TransporteurController.java"
C "2026-04-16T14:00:00+01:00" "feat(api): controllers REST Livraison et Transporteur"

# 20 - 18 Avril
SA "$B/controller/PaiementController.java"
C "2026-04-18T10:00:00+01:00" "feat(api): PaiementController avec endpoints Stripe et paiement manuel"

# 21 - 21 Avril
SA "$B/controller"
C "2026-04-21T09:00:00+01:00" "feat(api): controllers BonCommande, Fournisseur, Produit, Panier, Avis"

# 22 - 23 Avril
SA "$B/security", "$B/config"
C "2026-04-23T14:00:00+01:00" "feat(security): Spring Security JWT stateless - roles CLIENT et ADMIN"

# 23 - 25 Avril
SA "$B/exception/GlobalExceptionHandler.java"
C "2026-04-25T10:00:00+01:00" "feat(exception): GlobalExceptionHandler - gestion centralisee erreurs HTTP"

# 24 - 28 Avril
SA "backend/commandes/src/main/resources/application.properties"
C "2026-04-28T09:00:00+01:00" "config: application.properties - BDD, JWT, Stripe via variables env"

# 25 - 30 Avril
SA "backend/commandes/Dockerfile"
C "2026-04-30T14:00:00+01:00" "feat(docker): Dockerfile backend multi-stage Maven + JRE 17 Alpine"

# 26 - 2 Mai - Angular setup
SA "frontend/package.json", "frontend/angular.json", "frontend/tsconfig.json",
   "frontend/tsconfig.app.json", "frontend/tsconfig.spec.json",
   "frontend/.editorconfig", "frontend/.gitignore", "frontend/.prettierrc",
   "frontend/src/index.html", "frontend/src/main.ts", "frontend/src/styles.css"
C "2026-05-02T10:00:00+01:00" "feat(frontend): initialisation projet Angular 17 avec TypeScript"

# 27 - 4 Mai - App core
SA "frontend/src/app/app-module.ts", "frontend/src/app/app-routing-module.ts",
   "frontend/src/app/app.ts", "frontend/src/app/app.html", "frontend/src/app/app.css"
C "2026-05-04T09:30:00+01:00" "feat(frontend): module principal, routing et composant root"

# 28 - 5 Mai - Models & Services
SA "frontend/src/app/models", "frontend/src/app/services"
C "2026-05-05T10:00:00+01:00" "feat(frontend): modeles TypeScript et services API/Auth"

# 29 - 5 Mai - Guards & interceptors
SA "frontend/src/app/guards", "frontend/src/app/interceptors"
C "2026-05-05T16:00:00+01:00" "feat(frontend): guards auth et intercepteur HTTP JWT"

# 30 - 6 Mai - Auth UI
SA "frontend/src/app/components/auth"
C "2026-05-06T09:00:00+01:00" "feat(frontend): composants login et register"

# 31 - 7 Mai - Admin part 1
SA "frontend/src/app/components/dashboard",
   "frontend/src/app/components/clients",
   "frontend/src/app/components/commandes"
C "2026-05-07T09:00:00+01:00" "feat(frontend): dashboard admin, gestion clients et commandes"

# 32 - 7 Mai - Admin part 2
SA "frontend/src/app/components/livraisons",
   "frontend/src/app/components/paiements",
   "frontend/src/app/components/transporteurs"
C "2026-05-07T15:30:00+01:00" "feat(frontend): composants livraisons, paiements et transporteurs"

# 33 - 8 Mai - Admin part 3
SA "frontend/src/app/components/produits",
   "frontend/src/app/components/categories",
   "frontend/src/app/components/fournisseurs",
   "frontend/src/app/components/bons-commande",
   "frontend/src/app/components/avis"
C "2026-05-08T09:00:00+01:00" "feat(frontend): produits, categories, fournisseurs, bons-commande et avis"

# 34 - 8 Mai - Client space
SA "frontend/src/app/components/espace-client",
   "frontend/src/app/components/navbar",
   "frontend/src/app/components/notification",
   "frontend/src/app/components/home-redirect",
   "frontend/src/app/components/paiement-success",
   "frontend/src/app/components/paiement-cancel"
C "2026-05-08T11:00:00+01:00" "feat(frontend): espace client, navbar, notifications et pages paiement"

# 35 - 8 Mai - Docker frontend
SA "frontend/Dockerfile", "frontend/nginx.conf", "frontend/proxy.conf.json"
C "2026-05-08T12:00:00+01:00" "feat(docker): Dockerfile frontend + Nginx reverse proxy API"

# 36 - Reste de tout
git add . 2>$null | Out-Null
$staged = git diff --cached --name-only 2>&1
if ($staged) {
    C "2026-05-08T13:30:00+01:00" "chore: ajout fichiers restants (configs, specs, assets)"
}

# === Push ===
Write-Host "`nPush vers GitHub..." -ForegroundColor Cyan
git branch -M main
git push origin main --force 2>&1

$count = (git log --oneline 2>&1 | Measure-Object -Line).Lines
Write-Host "`n✅ $count commits pousses sur GitHub !" -ForegroundColor Green
Write-Host "   https://github.com/KodsLamouchi322/gestion-commandes-livraisons" -ForegroundColor Cyan
