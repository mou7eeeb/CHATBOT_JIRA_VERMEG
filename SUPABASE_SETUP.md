# Supabase PostgreSQL Configuration Guide

## Étape 1 : Créer un projet Supabase

1. Allez sur https://supabase.com
2. Créez un compte ou connectez-vous
3. Cliquez sur "New Project"
4. Remplissez les informations :
   - **Name** : jira-chatbot
   - **Database Password** : (choisissez un mot de passe fort, notez-le)
   - **Region** : choisissez la région la plus proche de vous
5. Attendez que le projet soit créé (2-3 minutes)

## Étape 2 : Récupérer les informations de connexion

1. Dans votre projet Supabase, allez dans **Settings** → **Database**
2. Copiez les informations suivantes :
   - **Connection string** (format JDBC)
   - **Host**
   - **Database name**
   - **Username**
   - **Password**

## Étape 3 : Configurer les variables d'environnement

### Option A : Windows PowerShell (pour développement)

```powershell
$env:DB_URL="jdbc:postgresql://db.xxx.supabase.co:5432/postgres"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="votre_mot_de_passe_supabase"
mvn spring-boot:run
```

### Option B : Créer un fichier `.env` (recommandé)

Créez un fichier `backend/.env` :

```env
DB_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=votre_mot_de_passe_supabase
```

Ajoutez le plugin dotenv dans `pom.xml` (déjà présent) et configurez-le.

### Option C : Variables d'environnement système (Windows)

```powershell
setx DB_URL "jdbc:postgresql://db.xxx.supabase.co:5432/postgres"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "votre_mot_de_passe_supabase"
```

## Étape 4 : Configurer SSL (requis par Supabase)

Modifiez l'URL de connexion pour inclure SSL :

```env
DB_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require
```

## Étape 5 : Démarrer le backend

```bash
cd backend
mvn spring-boot:run
```

Hibernate créera automatiquement toutes les tables nécessaires :
- `users`
- `jira_connections`
- `chat_sessions`
- `chat_messages`

## Étape 6 : Vérifier dans Supabase

1. Allez dans **Table Editor** dans Supabase
2. Vous devriez voir les tables créées automatiquement
3. Vérifiez que les colonnes sont correctes

## Sécurité Supabase

### Désactiver l'accès public

1. Allez dans **Authentication** → **Settings**
2. Désactivez "Enable email confirmations" si nécessaire
3. Configurez "Site URL" avec votre URL frontend

### RLS (Row Level Security)

Pour cet projet, la sécurité est gérée par Spring Boot côté backend.
Les tables Supabase servent uniquement de stockage.
RLS n'est pas nécessaire car l'accès se fait via l'API Spring Boot.

## Dépannage

### Erreur de connexion SSL

Si vous avez une erreur SSL, ajoutez ceci à l'URL :
```
?sslmode=require&sslrootcert=/path/to/cert
```

Ou utilisez :
```
?sslmode=no-verify
```
(pour développement uniquement)

### Erreur "relation does not exist"

Hibernate créera les tables automatiquement au premier démarrage.
Si l'erreur persiste, vérifiez que `spring.jpa.hibernate.ddl-auto=update` est configuré.

### Timeout de connexion

Supabase a un timeout par défaut. Pour les longues requêtes, ajoutez :
```properties
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10
```

## Variables d'environnement requises

- `DB_URL` : URL de connexion JDBC PostgreSQL
- `DB_USERNAME` : Nom d'utilisateur PostgreSQL (généralement "postgres")
- `DB_PASSWORD` : Mot de passe de la base de données Supabase

## Notes importantes

- Ne commitez JAMAIS le fichier `.env` dans Git
- Utilisez des mots de passe forts
- Changez le mot de passe par défaut de Supabase
- Activez SSL pour la production
- Sauvegardez régulièrement vos données Supabase
