# Groq API Configuration Guide

## Étape 1 : Obtenir une clé API Groq

1. Allez sur https://console.groq.com
2. Créez un compte ou connectez-vous
3. Allez dans **API Keys**
4. Cliquez sur **Create API Key**
5. Copiez la clé générée (format : `gsk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)

## Étape 2 : Configurer la variable d'environnement (Windows PowerShell)

### Option A : Pour la session actuelle uniquement

```powershell
$env:GROQ_API_KEY="votre_clé_groq_ici"
$env:GROQ_MODEL="llama-3.3-70b-versatile"
```

### Option B : Pour la session actuelle + démarrer le backend

```powershell
$env:GROQ_API_KEY="votre_clé_groq_ici"
$env:GROQ_MODEL="llama-3.3-70b-versatile"
cd backend
mvnw.cmd spring-boot:run
```

### Option C : Variable d'environnement permanente (Windows)

```powershell
setx GROQ_API_KEY "votre_clé_groq_ici"
setx GROQ_MODEL "llama-3.3-70b-versatile"
```

*Note : Après setx, fermez et rouvrez votre terminal PowerShell.*

## Étape 3 : Vérifier la configuration

Au démarrage du backend, vous devriez voir dans les logs :

```
✓ Groq API key configured: true
✓ Groq API model: llama-3.3-70b-versatile
✓ Groq API URL: https://api.groq.com/openai/v1/chat/completions
```

Si la clé n'est pas configurée, vous verrez :

```
⚠ GROQ_API_KEY is missing or not configured
⚠ To enable Groq AI features, set the GROQ_API_KEY environment variable
⚠ Example: $env:GROQ_API_KEY='your-key-here'
```

## Étape 4 : Tester

Une fois le backend démarré avec la clé configurée :

1. Ouvrez le chatbot dans Angular
2. Envoyez un message général : "bonjour"
3. Vous devriez recevoir une réponse de l'IA Groq

## Modèles Groq disponibles

- `llama-3.3-70b-versatile` (par défaut, recommandé)
- `llama-3.1-70b-versatile`
- `mixtral-8x7b-32768`
- `gemma-7b-it`

Pour changer de modèle :

```powershell
$env:GROQ_MODEL="llama-3.1-70b-versatile"
```

## Sécurité

- **NE JAMAIS** commiter votre clé API dans Git
- **NE JAMAIS** partager votre clé API
- La clé n'est jamais affichée dans les logs
- La clé est stockée uniquement en variable d'environnement

## Dépannage

### Erreur "Groq API authentication failed (401)"

- Vérifiez que la clé API est correcte
- Vérifiez que la clé n'a pas expiré
- Assurez-vous que la variable d'environnement est bien définie

### Erreur "Groq API rate limit exceeded (429)"

- Vous avez atteint la limite de taux de Groq
- Attendez quelques minutes avant de réessayer
- Considérez une mise à niveau de votre compte Groq

### Le chatbot répond toujours avec le message fallback

- Vérifiez les logs au démarrage pour confirmer que Groq est configuré
- Assurez-vous que le backend a été redémarré après avoir défini la variable
- Vérifiez que la clé API n'est pas vide

## Configuration application.properties

La configuration dans `backend/src/main/resources/application.properties` :

```properties
# AI Configuration (Groq)
groq.api.key=${GROQ_API_KEY:}
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.api.model=${GROQ_MODEL:llama-3.3-70b-versatile}
```

Cette configuration utilise les variables d'environnement avec des valeurs par défaut.
