# 🔒 SÉCURITÉ STRIPE - IMPORTANT

## ⚠️ VOUS AVEZ PARTAGÉ VOTRE CLÉ PUBLIQUEMENT

Votre clé Stripe a été exposée publiquement. Suivez ces étapes **IMMÉDIATEMENT** :

---

## 🚨 ÉTAPE 1 : Révoquer la clé compromise (URGENT)

1. Allez sur https://dashboard.stripe.com/test/apikeys
2. Trouvez la **Clé secrète** (sk_test_51THM2HGm5N...)
3. Cliquez sur les **3 points** (•••) à droite de la clé
4. Cliquez sur **"Supprimer"** ou **"Roll key"**
5. Confirmez la suppression

⚠️ **Faites cela MAINTENANT avant de continuer !**

---

## ✅ ÉTAPE 2 : Créer une nouvelle clé

1. Sur la même page (API keys)
2. La clé sera automatiquement recréée
3. Cliquez sur **"Reveal test key"** pour voir la nouvelle clé
4. Copiez-la (elle commence par `sk_test_...`)

---

## 🔐 ÉTAPE 3 : Configuration sécurisée

### Option A : Variable d'environnement (RECOMMANDÉ)

**Windows PowerShell :**
```powershell
$env:STRIPE_SECRET_KEY="sk_test_VOTRE_NOUVELLE_CLE"
```

**Linux/Mac :**
```bash
export STRIPE_SECRET_KEY="sk_test_VOTRE_NOUVELLE_CLE"
```

Puis modifiez `application.properties` :
```properties
stripe.secret.key=${STRIPE_SECRET_KEY:}
```

### Option B : Fichier local (NON COMMITÉ)

1. Créez `application-local.properties` (déjà dans .gitignore)
2. Ajoutez :
```properties
stripe.secret.key=sk_test_VOTRE_NOUVELLE_CLE
```

3. Modifiez `application.properties` :
```properties
spring.profiles.include=local
stripe.secret.key=${STRIPE_SECRET_KEY:}
```

---

## 🛡️ RÈGLES DE SÉCURITÉ

### ❌ NE JAMAIS :
- Partager vos clés Stripe publiquement (chat, forum, GitHub)
- Commiter les clés dans Git
- Envoyer les clés par email
- Mettre les clés en clair dans le code

### ✅ TOUJOURS :
- Utiliser des variables d'environnement
- Ajouter les fichiers sensibles au .gitignore
- Révoquer immédiatement une clé compromise
- Utiliser le mode Test pour le développement

---

## 📋 Checklist de sécurité

- [ ] Clé compromise révoquée sur Stripe Dashboard
- [ ] Nouvelle clé créée
- [ ] Variable d'environnement configurée OU fichier local créé
- [ ] application.properties modifié pour utiliser ${STRIPE_SECRET_KEY:}
- [ ] .gitignore mis à jour
- [ ] Vérification : la clé n'apparaît plus en clair dans les fichiers

---

## 🧪 Tester la configuration

1. Démarrez le backend :
```bash
cd backend/commandes
./mvnw spring-boot:run
```

2. Vérifiez les logs :
```
✅ Stripe initialized successfully
```

3. Si erreur "Invalid API Key" :
   - Vérifiez que la variable d'environnement est définie
   - Vérifiez que la clé commence par `sk_test_`
   - Redémarrez le terminal après avoir défini la variable

---

## 📞 Support

Si vous avez des questions sur la sécurité Stripe :
- Documentation : https://stripe.com/docs/keys
- Support : https://support.stripe.com

---

**🔒 La sécurité de vos clés API est CRITIQUE. Ne les partagez JAMAIS !**
