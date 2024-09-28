
# 📄 MBAT-API

## Description
MBAT-API est une application de gestion de comptes bancaires développée en utilisant Spring Boot et Angular. Elle permet de gérer les opérations bancaires, l'authentification et l'autorisation des utilisateurs, ainsi que la gestion des comptes par année et mois.

## Table des matières
- [Description](#description)
- [Fonctionnalités](#fonctionnalités)
- [Installation](#installation)
- [Configuration](#configuration)
- [Endpoints de l'API](#endpoints-de-lapi)
- [Tests](#tests)
- [Contribution](#contribution)
- [Licence](#licence)

## Fonctionnalités
- 🔐 Authentification et autorisation avec JWT.
- 📅 Gestion des comptes bancaires par année et mois.
- 📊 Suivi des opérations bancaires.
- 🛠️ Interface utilisateur développée en Angular.
- 📑 Documentation Swagger intégrée.

## Installation

### Prérequis
- Java 17 ou plus récent
- Maven
- MySQL
- Node.js et Angular CLI (pour le frontend)

### Backend - Spring Boot

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/KevinRadlowski/MBAT-api.git
   cd MBAT-api
   ```

2. **Configurer la base de données :**
    - Créez une base de données MySQL nommée `mbat_database`.
    - Modifiez le fichier `src/main/resources/application.properties` pour configurer les informations de connexion à la base de données :
      ```properties
      spring.datasource.url=jdbc:mysql://LOGIN:PASSWORD@localhost:3306/mbat_database?createDatabaseIfNotExist=true
      ```

3. **Installer les dépendances et compiler le projet :**
   ```bash
   mvn clean install
   ```

4. **Lancer l'application :**
   ```bash
   mvn spring-boot:run
   ```

5. **Accéder à la documentation Swagger :**
    - Ouvrez votre navigateur et allez à l'adresse : `http://localhost:8080/swagger-ui.html`

### Frontend - Angular

1. **Naviguer vers le dossier frontend :**
   ```bash
   cd frontend
   ```

2. **Installer les dépendances :**
   ```bash
   npm install
   ```

3. **Lancer l'application Angular :**
   ```bash
   ng serve
   ```

4. **Accéder à l'application :**
    - Ouvrez votre navigateur et allez à l'adresse : `http://localhost:4200`

## Configuration

### Variables d'environnement

Assurez-vous de configurer correctement les variables d'environnement nécessaires dans le fichier `.env`. Par exemple :

```java
export const environment = {
production: false,
// apiUrl: 'http://localhost:8080',
apiUrl: 'http://192.168.56.101:8080'
};
```

## Endpoints de l'API

| Méthode | Endpoint                  | Description                                  | Authentification |
|---------|---------------------------|----------------------------------------------|------------------|
| POST    | `/api/auth/register`      | Enregistre un nouvel utilisateur             | ❌               |
| POST    | `/api/auth/login`         | Connecte un utilisateur                      | ❌               |
| GET     | `/api/accounts`           | Récupère tous les comptes bancaires          | ✔️               |
| POST    | `/api/accounts`           | Crée un nouveau compte bancaire              | ✔️               |
| GET     | `/api/accounts/{id}`      | Récupère les détails d'un compte bancaire    | ✔️               |
| PUT     | `/api/accounts/{id}`      | Met à jour un compte bancaire                | ✔️               |
| DELETE  | `/api/accounts/{id}`      | Supprime un compte bancaire                  | ✔️               |
| GET     | `/api/operations`         | Récupère toutes les opérations bancaires     | ✔️               |
| POST    | `/api/operations`         | Crée une nouvelle opération bancaire         | ✔️               |

## Tests

### Tests d'Intégration

Pour lancer les tests d'intégration, utilisez la commande :

```bash
mvn test
```

## Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Forkez le projet.
2. Créez votre branche de fonctionnalité (`git checkout -b feature/AmazingFeature`).
3. Committez vos modifications (`git commit -m 'Add some AmazingFeature'`).
4. Pushez vers la branche (`git push origin feature/AmazingFeature`).
5. Ouvrez une Pull Request.
