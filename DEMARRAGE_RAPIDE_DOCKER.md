# 🚀 Démarrage Rapide avec Docker

## ⚡ En 3 Étapes

### 1️⃣ Vérifier que Docker Desktop est démarré
- Ouvrir Docker Desktop
- Attendre qu'il soit complètement démarré (icône verte)

### 2️⃣ Double-cliquer sur un des fichiers .bat

**Option A : Avec logs en direct (Recommandé pour la première fois)**
```
📄 start-docker.bat
```
- Affiche les logs en temps réel
- Permet de voir le démarrage
- Appuyer sur Ctrl+C pour arrêter

**Option B : En arrière-plan**
```
📄 start-docker-detached.bat
```
- Lance en arrière-plan
- Retourne immédiatement
- Utiliser `logs-docker.bat` pour voir les logs

### 3️⃣ Ouvrir le navigateur
```
http://localhost:8080
```

**Compte admin :**
- Email : `admin@gestion.com`
- Mot de passe : `admin123`

---

## 🛑 Arrêter l'Application

Double-cliquer sur :
```
📄 stop-docker.bat
```

---

## 📊 Voir les Logs

Double-cliquer sur :
```
📄 logs-docker.bat
```

---

## 🔧 Commandes Manuelles (Alternative)

### Démarrer
```bash
docker-compose up --build
```

### Démarrer en arrière-plan
```bash
docker-compose up -d --build
```

### Arrêter
```bash
docker-compose down
```

### Voir les logs
```bash
docker-compose logs -f
```

---

## 🌐 URLs Importantes

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:8080 | Interface utilisateur |
| **Backend** | http://localhost:8085 | API REST |
| **Swagger** | http://localhost:8085/swagger-ui/index.html | Documentation API |
| **MySQL** | localhost:3306 | Base de données |

---

## ⏱️ Temps de Démarrage

- **MySQL** : ~10-15 secondes
- **Backend** : ~30-40 secondes  
- **Frontend** : ~5 secondes
- **Total** : ~1 minute

---

## ✅ Vérifier que Tout Fonctionne

### 1. Vérifier les conteneurs
```bash
docker-compose ps
```

**Résultat attendu :**
```
NAME                STATUS
gestion_mysql       Up (healthy)
gestion_backend     Up
gestion_frontend    Up
```

### 2. Tester l'application
1. Ouvrir http://localhost:8080
2. Se connecter avec `admin@gestion.com` / `admin123`
3. Vérifier le dashboard
4. Créer un client de test
5. Créer un produit de test

---

## 🐛 Problèmes Courants

### ❌ "Port already in use"

**Solution :**
```bash
# Trouver le processus qui utilise le port
netstat -ano | findstr :8080

# Tuer le processus (remplacer <PID>)
taskkill /PID <PID> /F
```

### ❌ "Docker is not running"

**Solution :**
- Démarrer Docker Desktop
- Attendre qu'il soit complètement démarré
- Réessayer

### ❌ "Backend not responding"

**Solution :**
- Attendre 1-2 minutes (MySQL doit être prêt)
- Vérifier les logs : `docker-compose logs backend`
- Vérifier que MySQL est "healthy" : `docker-compose ps`

### ❌ "Cannot connect to database"

**Solution :**
```bash
# Recréer la base de données
docker-compose down -v
docker-compose up --build
```

---

## 🔄 Mettre à Jour l'Application

### Après avoir modifié le code :

1. Arrêter les conteneurs
```bash
docker-compose down
```

2. Reconstruire et redémarrer
```bash
docker-compose up --build
```

Ou simplement double-cliquer sur `start-docker.bat` et choisir "O" pour rebuild.

---

## 💾 Données Persistantes

Les données sont sauvegardées dans des volumes Docker :
- **mysql_data** : Base de données
- **uploads_data** : Images des produits

Pour supprimer toutes les données :
```bash
docker-compose down -v
```

⚠️ **Attention** : Cette commande supprime TOUTES les données !

---

## 📚 Documentation Complète

Pour plus de détails, consulter :
- **GUIDE_DOCKER.md** - Guide complet avec toutes les commandes
- **CORRECTIONS_CHARGEMENT.md** - Détails des corrections apportées
- **GUIDE_TEST_CORRECTIONS.md** - Guide de test de l'application

---

## 🎯 Résumé Ultra-Rapide

```bash
# Démarrer
docker-compose up --build

# Ouvrir
http://localhost:8080

# Se connecter
admin@gestion.com / admin123

# Arrêter
docker-compose down
```

**C'est tout ! 🎉**

---

## 📞 Besoin d'Aide ?

1. Vérifier les logs : `docker-compose logs -f`
2. Vérifier le status : `docker-compose ps`
3. Consulter GUIDE_DOCKER.md pour plus de détails
4. Redémarrer proprement : `docker-compose down && docker-compose up --build`

---

**Bon développement ! 🚀**
