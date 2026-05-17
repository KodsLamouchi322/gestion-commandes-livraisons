╔════════════════════════════════════════════════════════════════╗
║                                                                ║
║          🐳 DÉMARRAGE DOCKER - GUIDE ULTRA-RAPIDE             ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝


📋 PRÉREQUIS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ Docker Desktop installé et démarré
✅ Ports libres : 3306, 8080, 8085


🚀 DÉMARRAGE EN 2 CLICS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1️⃣  Double-cliquer sur :
    📄 start-docker.bat

2️⃣  Ouvrir le navigateur :
    🌐 http://localhost:8080

    👤 Compte admin :
       Email    : admin@gestion.com
       Password : admin123


🛑 ARRÊTER L'APPLICATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Double-cliquer sur :
📄 stop-docker.bat


📊 VOIR LES LOGS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Double-cliquer sur :
📄 logs-docker.bat


🌐 URLS IMPORTANTES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Frontend  : http://localhost:8080
Backend   : http://localhost:8085
Swagger   : http://localhost:8085/swagger-ui/index.html
MySQL     : localhost:3306


⏱️  TEMPS DE DÉMARRAGE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

MySQL    : ~15 secondes
Backend  : ~40 secondes
Frontend : ~5 secondes
─────────────────────────
Total    : ~1 minute


✅ VÉRIFIER QUE TOUT FONCTIONNE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Commande :
    docker-compose ps

Résultat attendu :
    NAME                STATUS
    gestion_mysql       Up (healthy)
    gestion_backend     Up
    gestion_frontend    Up


🐛 PROBLÈMES COURANTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

❌ "Port already in use"
   → Libérer le port ou changer dans docker-compose.yml

❌ "Docker is not running"
   → Démarrer Docker Desktop et réessayer

❌ "Backend not responding"
   → Attendre 1-2 minutes (MySQL doit être prêt)

❌ "Cannot connect to database"
   → docker-compose down -v
   → docker-compose up --build


🔄 METTRE À JOUR APRÈS MODIFICATION DU CODE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Arrêter :
   docker-compose down

2. Reconstruire et redémarrer :
   docker-compose up --build


📚 DOCUMENTATION COMPLÈTE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📄 DEMARRAGE_RAPIDE_DOCKER.md  - Guide simplifié
📄 GUIDE_DOCKER.md              - Guide complet
📄 CORRECTIONS_CHARGEMENT.md    - Détails techniques
📄 GUIDE_TEST_CORRECTIONS.md    - Tests de l'application


🎯 COMMANDES ESSENTIELLES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Démarrer (avec rebuild) :
    docker-compose up --build

Démarrer en arrière-plan :
    docker-compose up -d --build

Arrêter :
    docker-compose down

Voir les logs :
    docker-compose logs -f

Voir le status :
    docker-compose ps

Redémarrer un service :
    docker-compose restart backend


💡 ASTUCES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✨ Première fois : Utiliser start-docker.bat pour voir les logs

✨ Développement : Utiliser start-docker-detached.bat pour 
   travailler en arrière-plan

✨ Débogage : Utiliser logs-docker.bat pour voir les logs en 
   temps réel

✨ Nettoyage complet : docker-compose down -v (⚠️ supprime les 
   données)


🎉 RÉSUMÉ ULTRA-RAPIDE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Double-clic sur start-docker.bat
2. Attendre ~1 minute
3. Ouvrir http://localhost:8080
4. Se connecter : admin@gestion.com / admin123
5. C'est tout ! 🚀


╔════════════════════════════════════════════════════════════════╗
║                                                                ║
║  ✅ Tout est configuré et prêt à l'emploi !                   ║
║                                                                ║
║  📦 3 conteneurs : MySQL + Backend + Frontend                 ║
║  🔒 Sécurisé avec JWT                                         ║
║  💳 Paiement Stripe configuré                                 ║
║  📊 Dashboard admin complet                                   ║
║  🚀 Performance optimisée                                     ║
║                                                                ║
║  Bon développement ! 🎊                                       ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
