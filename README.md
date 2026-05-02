! Pull / Clone the Master branch !

⚙️ Prérequis Docker + Docker Compose Java 17+ / Maven

🐳 1. Lancer les bases (Docker)

Depuis le dossier racine du backend (où se trouve docker-compose.yml) :

docker-compose up -d

👉 Lance : MongoDB (port 27018) Redis (port 6379) Neo4j (ports 7474 + 7687)

🔙 2. Lancer le backend (Spring Boot)

Dans le dossier backend :

./mvnw spring-boot:run ou via IntelliJ → Run

👉 API disponible sur : http://localhost:8080

🧪 3. Accès aux outils MongoDB Compass → mongodb://localhost:27018 Neo4j UI → http://localhost:7474 user: neo4j password: password Redis CLI : docker exec -it sth-redis redis-cli

📌 Notes Les données sont initialisées automatiquement au démarrage du backend (DataInitializer) Le cache Redis utilise un TTL : /offers → 60s /offers/{id} → 300s Les sessions (/login) expirent après 900s

▶️ Arrêt docker-compose down
