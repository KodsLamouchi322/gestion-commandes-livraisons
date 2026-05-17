# 🐳 Guide de Démarrage Docker

## 📋 Prérequis

- Docker Desktop installé et démarré
- Au moins 4 GB de RAM disponible
- Ports libres : 3306 (MySQL), 8080 (Frontend), 8085 (Backend)

---

## 🚀 Démarrage Rapide

### Option 1 : Tout en une commande (Recommandé)
```bash
docker-compose up --build
```

### Option 2 : En arrière-plan
```bash
docker-compose up -d --build
```

### Option 3 : Sans rebuild (si déjà construit)
```bash
docker-compose up
```

---

## 📦 Architecture Docker

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  Frontend (Nginx)                                   │
│  Port: 8080                                         │
│  http://localhost:8080                              │
│                                                     │
└──────────────────┬──────────────────────────────────┘
                   │
                   │ Proxy /api/ → backend:8085
                   │
┌──────────────────▼──────────────────────────────────┐
│                                                     │
│  Backend (Spring Boot)                              │
│  Port: 8085                                         │
│  http://localhost:8085                              │
│                                                     │
└──────────────────┬──────────────────────────────────┘
                   │
                   │ JDBC Connection
                   │
┌──────────────────▼──────────────────────────────────┐
│                                                     │
│  MySQL (MariaDB)                                    │
│  Port: 3306                                         │
│  Database: gestion_commandes_v2                     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔍 Vérification du Démarrage

### 1. Vérifier que tous les conteneurs sont démarrés
```bash
docker-compose ps
```

**Résultat attendu :**
```
NAME                IMAGE                    STATUS
gestion_mysql       mariadb:latest           Up (healthy)
gestion_backend     projetjee-backend        Up
gestion_frontend    projetjee-frontend       Up
```

### 2. Vérifier les logs

**Tous les services :**
```bash
docker-compose logs -f
```

**Backend uniquement :**
```bash
docker-compose logs -f backend
```

**Frontend uniquement :**
```bash
docker-compose logs -f frontend
```

**MySQL uniquement :**
```bash
docker-compose logs -f mysql
```

### 3. Vérifier que le backend est prêt
```bash
docker-compose logs backend | findstr "Started CommandesApplication"
```

**Résultat attendu :**
```
Started CommandesApplication in X.XXX seconds
```

---

## 🌐 Accès à l'Application

### Frontend (Interface Utilisateur)
```
http://localhost:8080
```

### Backend API (Direct)
```
http://localhost:8085/api
```

### Swagger UI (Documentation API)
```
http://localhost:8085/swagger-ui/index.html
```

### Base de Données MySQL
```
Host: localhost
Port: 3306
Database: gestion_commandes_v2
User: root
Password: root
```

---

## 👤 Comptes par Défaut

### Administrateur
```
Email: admin@gestion.com
Mot de passe: admin123
```

### Client Test (créé automatiquement si configuré)
```
Email: client@test.com
Mot de passe: client123
```

---

## 🛠️ Commandes Utiles

### Arrêter tous les conteneurs
```bash
docker-compose down
```

### Arrêter et supprimer les volumes (⚠️ Supprime les données)
```bash
docker-compose down -v
```

### Reconstruire un service spécifique
```bash
docker-compose build backend
docker-compose build frontend
```

### Redémarrer un service
```bash
docker-compose restart backend
docker-compose restart frontend
```

### Voir les logs en temps réel
```bash
docker-compose logs -f --tail=100
```

### Exécuter une commande dans un conteneur
```bash
# Backend
docker-compose exec backend sh

# Frontend
docker-compose exec frontend sh

# MySQL
docker-compose exec mysql mysql -u root -proot gestion_commandes_v2
```

---

## 🔧 Dépannage

### Problème : Port déjà utilisé

**Erreur :**
```
Error: bind: address already in use
```

**Solution :**
```bash
# Vérifier quel processus utilise le port
netstat -ano | findstr :8080
netstat -ano | findstr :8085
netstat -ano | findstr :3306

# Tuer le processus (remplacer <PID> par le numéro)
taskkill /PID <PID> /F
```

### Problème : Backend ne démarre pas

**Vérifier les logs :**
```bash
docker-compose logs backend
```

**Solutions courantes :**
1. MySQL pas encore prêt → Attendre le healthcheck
2. Port 8085 occupé → Libérer le port
3. Erreur de compilation → Vérifier le code Java

### Problème : Frontend ne se connecte pas au backend

**Vérifier nginx.conf :**
```bash
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
```

**Vérifier que le proxy fonctionne :**
```bash
curl http://localhost:8080/api/health
```

### Problème : Base de données vide

**Recréer la base :**
```bash
docker-compose down -v
docker-compose up -d
```

### Problème : Images Docker corrompues

**Nettoyer et reconstruire :**
```bash
docker-compose down
docker system prune -a
docker-compose up --build
```

---

## 📊 Monitoring

### Voir l'utilisation des ressources
```bash
docker stats
```

### Voir les volumes
```bash
docker volume ls
```

### Inspecter un volume
```bash
docker volume inspect projetjee_mysql_data
docker volume inspect projetjee_uploads_data
```

---

## 🔐 Configuration de Production

### Variables d'Environnement à Modifier

Dans `docker-compose.yml`, modifier :

```yaml
environment:
  # Changer le secret JWT (32+ caractères aléatoires)
  JWT_SECRET: "VOTRE_SECRET_SUPER_LONG_ET_ALEATOIRE_ICI"
  
  # Changer le mot de passe MySQL
  MYSQL_ROOT_PASSWORD: "VOTRE_MOT_DE_PASSE_SECURISE"
  SPRING_DATASOURCE_PASSWORD: "VOTRE_MOT_DE_PASSE_SECURISE"
  
  # Configurer Stripe avec votre vraie clé
  STRIPE_SECRET_KEY: "sk_live_VOTRE_CLE_STRIPE"
  
  # URL du frontend en production
  FRONTEND_URL: "https://votre-domaine.com"
```

---

## 🚀 Déploiement Production

### 1. Créer un fichier docker-compose.prod.yml
```yaml
services:
  mysql:
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
  
  backend:
    restart: always
    environment:
      JWT_SECRET: ${JWT_SECRET}
      STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY}
  
  frontend:
    restart: always
```

### 2. Utiliser un fichier .env
```bash
# .env
MYSQL_PASSWORD=votre_mot_de_passe_securise
JWT_SECRET=votre_secret_jwt_long_et_aleatoire
STRIPE_SECRET_KEY=sk_live_votre_cle_stripe
```

### 3. Démarrer en production
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 📈 Performance

### Optimisations Appliquées

✅ **Multi-stage builds** - Images plus légères
✅ **Alpine Linux** - Images minimales
✅ **Healthchecks** - Démarrage ordonné
✅ **Volumes** - Persistance des données
✅ **Nginx** - Proxy inverse performant
✅ **CORS cache** - Moins de requêtes OPTIONS

### Temps de Démarrage Typiques

- MySQL : ~10-15 secondes
- Backend : ~30-40 secondes
- Frontend : ~5 secondes
- **Total : ~1 minute**

---

## 🧪 Tests Après Démarrage

### 1. Test de Santé Backend
```bash
curl http://localhost:8085/api/health
```

### 2. Test de Santé Frontend
```bash
curl http://localhost:8080
```

### 3. Test du Proxy
```bash
curl http://localhost:8080/api/health
```

### 4. Test de Connexion MySQL
```bash
docker-compose exec mysql mysql -u root -proot -e "SHOW DATABASES;"
```

### 5. Test Complet
1. Ouvrir http://localhost:8080
2. Se connecter avec admin@gestion.com / admin123
3. Vérifier le dashboard
4. Créer un client
5. Créer un produit
6. Créer une commande

---

## 📝 Logs et Débogage

### Niveaux de Logs

**Backend (Spring Boot) :**
```yaml
environment:
  LOGGING_LEVEL_ROOT: INFO
  LOGGING_LEVEL_COM_GESTION: DEBUG
```

**Frontend (Nginx) :**
```nginx
error_log /var/log/nginx/error.log debug;
access_log /var/log/nginx/access.log;
```

### Voir les Logs Nginx
```bash
docker-compose exec frontend tail -f /var/log/nginx/access.log
docker-compose exec frontend tail -f /var/log/nginx/error.log
```

---

## 🔄 Mise à Jour de l'Application

### 1. Arrêter les conteneurs
```bash
docker-compose down
```

### 2. Mettre à jour le code
```bash
git pull
# ou copier les nouveaux fichiers
```

### 3. Reconstruire et redémarrer
```bash
docker-compose up --build -d
```

### 4. Vérifier les logs
```bash
docker-compose logs -f
```

---

## 💾 Sauvegarde et Restauration

### Sauvegarder la Base de Données
```bash
docker-compose exec mysql mysqldump -u root -proot gestion_commandes_v2 > backup.sql
```

### Restaurer la Base de Données
```bash
docker-compose exec -T mysql mysql -u root -proot gestion_commandes_v2 < backup.sql
```

### Sauvegarder les Uploads
```bash
docker cp gestion_backend:/app/uploads ./uploads_backup
```

### Restaurer les Uploads
```bash
docker cp ./uploads_backup/. gestion_backend:/app/uploads
```

---

## ✅ Checklist de Démarrage

- [ ] Docker Desktop est démarré
- [ ] Ports 3306, 8080, 8085 sont libres
- [ ] Fichier .env configuré (si production)
- [ ] Clé Stripe configurée (si paiement par carte)
- [ ] Lancer `docker-compose up --build`
- [ ] Attendre ~1 minute
- [ ] Vérifier `docker-compose ps` (tous "Up")
- [ ] Ouvrir http://localhost:8080
- [ ] Se connecter avec admin@gestion.com / admin123
- [ ] Tester le dashboard

---

## 🎉 Résumé

**Commande principale :**
```bash
docker-compose up --build
```

**Accès application :**
```
http://localhost:8080
```

**Compte admin :**
```
admin@gestion.com / admin123
```

**Arrêter :**
```bash
docker-compose down
```

---

## 📞 Support

### Problèmes Courants

1. **"Port already in use"** → Libérer le port ou changer dans docker-compose.yml
2. **"Backend not responding"** → Attendre le healthcheck MySQL
3. **"Cannot connect to database"** → Vérifier les credentials MySQL
4. **"404 on /api"** → Vérifier nginx.conf et proxy_pass

### Logs à Vérifier

- Backend : `docker-compose logs backend`
- Frontend : `docker-compose logs frontend`
- MySQL : `docker-compose logs mysql`

---

**Bon déploiement ! 🚀**
