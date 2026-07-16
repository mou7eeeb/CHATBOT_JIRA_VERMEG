# Migration vers PostgreSQL Supabase - Résumé des modifications

## Modifications Backend

### 1. Configuration Base de Données
**Fichier** : `backend/src/main/resources/application.properties`

- Changé de H2 en mémoire vers PostgreSQL Supabase
- Configuration avec variables d'environnement :
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
- Dialect PostgreSQL configuré
- `ddl-auto=update` pour création automatique des tables

### 2. Entités JPA - Prévention des boucles JSON

**Fichiers modifiés** :
- `User.java`
- `JiraConnection.java`
- `ChatSession.java`
- `ChatMessage.java`

**Modifications** :
- Ajout de `@JsonIgnore` sur les relations bidirectionnelles
- Import de `com.fasterxml.jackson.annotation.JsonIgnore`
- Cela empêche les boucles infinies lors de la sérialisation JSON

### 3. Services Backend - Sécurité

**Fichiers existants** (déjà sécurisés) :
- `JiraConnectionService.java` - utilise `getCurrentUserId()` pour l'isolation
- `ChatSessionService.java` - utilise `getCurrentUserId()` pour l'isolation

**Fonctionnalités** :
- Chaque requête récupère l'utilisateur connecté depuis JWT
- Les repositories filtrent par userId
- Un utilisateur ne peut voir que ses propres données

### 4. Protection du Token Jira

**Fichier** : `JiraConnectionService.java`

**Implémentation existante** :
- Le token est stocké en base de données
- Dans `mapToDTO()`, le token est masqué : `dto.setJiraApiToken("********")`
- Le token n'est jamais renvoyé au frontend

### 5. Endpoints CRUD

**Jira Connections** (`JiraConnectionController.java`) :
- ✅ POST `/api/jira-connections` - Créer
- ✅ GET `/api/jira-connections` - Lister toutes
- ✅ GET `/api/jira-connections/{id}` - Récupérer une
- ✅ PUT `/api/jira-connections/{id}` - Modifier
- ✅ DELETE `/api/jira-connections/{id}` - Supprimer
- ✅ PATCH `/api/jira-connections/{id}/default` - Définir par défaut
- ✅ PATCH `/api/jira-connections/{id}/status` - Activer/désactiver
- ✅ POST `/api/jira-connections/{id}/test` - Tester connexion

**Conversations** (`ChatSessionController.java`) :
- ✅ POST `/api/conversations` - Créer
- ✅ GET `/api/conversations` - Lister toutes
- ✅ GET `/api/conversations/active` - Lister actives
- ✅ GET `/api/conversations/{id}` - Récupérer une
- ✅ PATCH `/api/conversations/{id}` - Renommer
- ✅ DELETE `/api/conversations/{id}` - Supprimer
- ✅ POST `/api/conversations/{id}/messages` - Ajouter message
- ✅ GET `/api/conversations/{id}/messages` - Lister messages

## Modifications Frontend

### 1. Services Angular

**Fichier** : `frontend/src/app/services/chat.service.ts`

**Modifications** :
- Supprimé `getSessionId()` qui utilisait localStorage
- Supprimé `generateSessionId()`
- Supprimé `localStorage.getItem('chatSessionId')`
- Supprimé `localStorage.setItem('chatSessionId', ...)`
- Les messages sont maintenant gérés via ChatSessionService

### 2. Nouveaux Services

**Fichiers créés** :
- `frontend/src/app/services/chat-session.service.ts`
- `frontend/src/app/models/chat-session.model.ts`

**Fonctionnalités** :
- CRUD complet pour les conversations
- Gestion des messages
- Appels API vers backend Spring Boot

### 3. Nouveau Component

**Fichiers créés** :
- `frontend/src/app/components/conversation-list/conversation-list.component.ts`
- `frontend/src/app/components/conversation-list/conversation-list.component.html`
- `frontend/src/app/components/conversation-list/conversation-list.component.css`

**Fonctionnalités** :
- Liste des conversations
- Suppression de conversations
- Navigation vers une conversation

### 4. Chatbot Component

**Fichier** : `frontend/src/app/components/chatbot/chatbot.component.ts`

**Modifications** :
- Intégration de ChatSessionService
- Chargement des conversations existantes via route parameter
- Sauvegarde automatique des messages dans la base
- Création automatique de conversation au premier message
- Suppression de l'utilisation de localStorage pour les données

### 5. Jira Connections Service

**Fichier** : `frontend/src/app/services/jira-connection.service.ts`

**Modifications** :
- Ajout de `setAsDefault(id)`
- Ajout de `toggleStatus(id)`

### 6. Jira Connections Component

**Fichier** : `frontend/src/app/components/jira-connections/jira-connections.component.ts`

**Modifications** :
- Ajout de `setAsDefault(id)`
- Ajout de `toggleStatus(id)`

### 7. Routing

**Fichier** : `frontend/src/app/app.routes.ts`

**Modifications** :
- Ajout de route `/chatbot/:id` pour ouvrir une conversation spécifique
- Ajout de route `/conversations` pour la liste des conversations

## DTOs Backend Créés

**Fichiers créés** :
- `ChatSessionDTO.java`
- `ChatMessageDTO.java`
- `CreateChatSessionDTO.java`
- `UpdateChatSessionDTO.java`

## Sécurité

### Authentification
- JWT toujours utilisé
- Token stocké dans localStorage (acceptable pour auth)
- User récupéré depuis Spring Security, jamais depuis frontend

### Isolation des données
- Toutes les requêtes backend utilisent `getCurrentUserId()`
- Repositories filtrent par userId
- Impossible d'accéder aux données d'un autre utilisateur

### Protection des données sensibles
- Token Jira masqué dans les réponses API
- Mot de passe utilisateur hashé (BCrypt)
- Connexions chiffrées (SSL)

## Tables PostgreSQL Créées Automatiquement

Hibernate créera ces tables au premier démarrage :

1. **users**
   - id, email, password, firstName, lastName, role, enabled, timestamps
   - Relations avec jira_connections et chat_sessions

2. **jira_connections**
   - id, user_id, connectionName, jiraBaseUrl, jiraEmail, jiraApiToken
   - isDefault, isActive, lastTestedAt, lastTestSuccess, lastTestMessage
   - timestamps

3. **chat_sessions**
   - id, user_id, jira_connection_id, title, isActive
   - timestamps
   - Relation avec chat_messages

4. **chat_messages**
   - id, chat_session_id, messageType, content
   - generatedJql, jiraResults, ticketCount
   - createdAt

## Étapes Suivantes

1. **Configurer Supabase** : Suivre `SUPABASE_SETUP.md`
2. **Démarrer le backend** : Avec les variables d'environnement Supabase
3. **Tester** :
   - Créer une connexion Jira
   - Refresh - vérifier persistance
   - Créer une conversation
   - Envoyer des messages
   - Refresh - vérifier persistance
   - Redémarrer backend - vérifier persistance
4. **Tester isolation** : Créer un deuxième utilisateur et vérifier qu'il ne voit pas les données du premier

## Notes Importantes

- Aucune donnée de connexion ou conversation n'est stockée dans localStorage/sessionStorage
- Toutes les données sont persistées dans PostgreSQL Supabase
- Les tokens Jira ne sont jamais renvoyés au frontend
- L'authentification JWT reste inchangée
- Le design et les fonctionnalités existantes sont préservés
