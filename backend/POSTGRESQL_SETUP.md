# PostgreSQL Setup Guide

Ce guide vous explique comment configurer PostgreSQL pour le projet AI Jira Chatbot.

## Prérequis

- PostgreSQL 12+ installé sur votre machine
- Java 17+
- Maven 3.6+

## Installation de PostgreSQL

### Windows
1. Téléchargez PostgreSQL depuis [postgresql.org](https://www.postgresql.org/download/windows/)
2. Lancez l'installeur et suivez les instructions
3. Notez le mot de passe que vous définissez pour l'utilisateur `postgres`
4. Le port par défaut est `5432`

### macOS
```bash
brew install postgresql@15
brew services start postgresql@15
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

## Configuration de la base de données

### 1. Créer la base de données

Connectez-vous à PostgreSQL :
```bash
# Windows
psql -U postgres

# macOS/Linux
sudo -u postgres psql
```

Créez la base de données :
```sql
CREATE DATABASE jirachatbot;
```

Créez un utilisateur dédié (optionnel mais recommandé) :
```sql
CREATE USER jirachatbot_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE jirachatbot TO jirachatbot_user;
```

Quittez psql :
```sql
\q
```

### 2. Configurer les variables d'environnement

Copiez le fichier `.env.example` vers `.env` :
```bash
cp .env.example .env
```

Modifiez le fichier `.env` avec vos informations :
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=jirachatbot
DB_USER=postgres
DB_PASSWORD=votre_mot_de_passe
```

**Important** : Le fichier `.env` est ignoré par Git pour des raisons de sécurité. Ne le commitez jamais !

### 3. Configuration Spring Boot

Le fichier `application.properties` est déjà configuré pour utiliser PostgreSQL avec les variables d'environnement du fichier `.env`.

Les variables d'environnement sont automatiquement chargées par Spring Boot :
- `${DB_HOST:localhost}` - Host de la base de données (défaut: localhost)
- `${DB_PORT:5432}` - Port PostgreSQL (défaut: 5432)
- `${DB_NAME:jirachatbot}` - Nom de la base de données (défaut: jirachatbot)
- `${DB_USER:postgres}` - Utilisateur PostgreSQL (défaut: postgres)
- `${DB_PASSWORD:postgres}` - Mot de passe PostgreSQL (défaut: postgres)

## Démarrage de l'application

### 1. Installer les dépendances Maven
```bash
cd backend
mvn clean install
```

### 2. Lancer l'application
```bash
mvn spring-boot:run
```

L'application va automatiquement :
- Se connecter à PostgreSQL
- Créer les tables nécessaires (grâce à `spring.jpa.hibernate.ddl-auto=update`)
- Initialiser les données de base

## Vérification de la connexion

### Vérifier que PostgreSQL est en cours d'exécution
```bash
# Windows
pg_isready -U postgres

# macOS/Linux
pg_isready
```

### Vérifier les tables créées
```bash
psql -U postgres -d jirachatbot -c "\dt"
```

Vous devriez voir les tables suivantes :
- `users`
- `jira_connections`
- `chat_sessions`
- `chat_messages`
- `saved_searches`

### Logs de l'application

Si l'application démarre correctement, vous verrez dans les logs :
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table users (...)
```

## Migrations de données

### Depuis H2 vers PostgreSQL

Si vous aviez des données dans H2 et souhaitez les migrer :

1. **Exporter les données depuis H2** (si H2 console était activée) :
   - Accédez à `http://localhost:8081/h2-console`
   - Exportez les données en SQL

2. **Importer dans PostgreSQL** :
   ```bash
   psql -U postgres -d jirachatbot -f export.sql
   ```

## Opérations CRUD

Les opérations CRUD fonctionnent automatiquement avec PostgreSQL via Spring Data JPA :

- **Create** : `userRepository.save(user)`
- **Read** : `userRepository.findById(id)`, `userRepository.findAll()`
- **Update** : `userRepository.save(existingUser)`
- **Delete** : `userRepository.deleteById(id)`

## Résolution des problèmes

### Erreur : "Connection refused"
- Vérifiez que PostgreSQL est démarré : `pg_isready`
- Vérifiez le port dans `.env` (défaut: 5432)
- Vérifiez que le firewall autorise les connexions sur le port 5432

### Erreur : "Authentication failed"
- Vérifiez le mot de passe dans `.env`
- Vérifiez que l'utilisateur existe : `psql -U postgres -c "\du"`

### Erreur : "Database does not exist"
- Créez la base de données : `CREATE DATABASE jirachatbot;`

### Les tables ne sont pas créées
- Vérifiez `spring.jpa.hibernate.ddl-auto=update` dans `application.properties`
- Vérifiez les logs pour les erreurs Hibernate

## Commandes utiles PostgreSQL

```bash
# Lister les bases de données
psql -U postgres -c "\l"

# Se connecter à la base de données
psql -U postgres -d jirachatbot

# Lister les tables
\dt

# Décrire une table
\d users

# Voir les données d'une table
SELECT * FROM users;

# Quitter psql
\q
```

## Sécurité

⚠️ **Important** :
- Ne commitez JAMAIS le fichier `.env` sur Git
- Utilisez des mots de passe forts en production
- Changez le `JWT_SECRET` en production
- Limitez les privilèges de l'utilisateur PostgreSQL en production
- Utilisez SSL pour les connexions en production

## Production

Pour la production, configurez les variables d'environnement directement sur le serveur :
```bash
export DB_HOST=your-production-host
export DB_PORT=5432
export DB_NAME=jirachatbot_prod
export DB_USER=jirachatbot_user
export DB_PASSWORD=strong_password_here
```

Ou utilisez un service de gestion de secrets comme AWS Secrets Manager, Azure Key Vault, ou HashiCorp Vault.
