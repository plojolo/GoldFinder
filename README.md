# Goldfinder

Goldfinder est un jeu multi-joueur avec une architecture client-serveur. Les joueurs incarnent des chercheurs d'or explorant une mine abandonnée pour collecter le plus d'or possible. Le jeu propose divers modes, dont un mode solo, un mode multi-joueur, et une variante cops vs. robbers.

## Table des matières

1. [Fonctionnalités](#fonctionnalités)
2. [Architecture](#architecture)
3. [Prérequis](#prérequis)
4. [Installation](#installation)
5. [Utilisation](#utilisation)
6. [Protocole de Communication](#protocole-de-communication)
7. [Organisation du Code](#organisation-du-code)
8. [Difficultés Rencontrées et Solutions](#difficultés-rencontrées-et-solutions)
9. [Conclusion](#conclusion)

## Fonctionnalités

-   **Parties mono-joueur et multi-joueur**
-   **Gestion des connexions UDP et TCP**
-   **Classement des meilleurs scores**
-   **Gestion de plusieurs parties simultanées**
-   **Mode "cops vs. robbers"**
-   **Architecture multi-serveurs**
-   **Implémentation de bots pour tests de performance**
-   **Tests de montée en charge**
-   **Bonus en jeu : ralentissement, téléportation, dragon, destruction de murs**

## Architecture

L'architecture du projet est basée sur une communication client-serveur utilisant des protocoles UDP et TCP. Les serveurs sont capables de gérer plusieurs parties simultanément, et un serveur de dispatch est utilisé pour équilibrer la charge entre plusieurs serveurs de jeu.

## Prérequis

-   Java 8 ou supérieur
-   Maven
-   Git

## Installation

1. Clonez le dépôt :

    ```sh
    git clone https://etulab.univ-amu.fr/travers.c/goldfinder-template.git
    cd goldfinder-template
    ```

2. Compilez le projet avec Maven :
    ```sh
    mvn clean install
    ```

## Utilisation

1. Lancez le serveur de dispatch :

    ```sh
    java -jar target/dispatch-server.jar
    ```

2. Lancez un ou plusieurs serveurs de jeu :

    ```sh
    java -jar target/game-server.jar
    ```

3. Lancez un client :
    ```sh
    java -jar target/client.jar
    ```

## Protocole de Communication

### Début/fin d'une partie

-   **Requête pour rejoindre une partie :**

    ```
    GAME_JOIN:player_name END
    ```

-   **Réponse du serveur lors du démarrage de la partie :**

    ```
    GAME_START player1:0 player2:1 ... END
    ```

-   **Annonce de fin de partie :**
    ```
    GAME_END player1:score player2:score ... END
    ```

### Déplacement et voisinage

-   **Requête de voisinage :**

    ```
    SURROUNDING
    ```

-   **Réponse du serveur :**

    ```
    UP:WALL RIGHT:EMPTY DOWN:GOLD LEFT:PLAYER2 END
    ```

-   **Requête de déplacement :**

    ```
    UP (ou DOWN, LEFT, RIGHT)
    ```

-   **Réponse du serveur :**
    ```
    INVALIDMOVE
    ```
    ou
    ```
    VALIDMOVE:item
    ```

## Organisation du Code

Le code est organisé en plusieurs classes pour assurer une structure modulaire :

-   **`UIClient` et `BotClient` :** Implémentent une interface client pour les joueurs humains et les bots.
-   **`UDPController` et `TCPController` :** Implémentent les contrôleurs pour les communications UDP et TCP.
-   **`Goldfinder` et `CopsVsRobbers` :** Représentent les différents modes de jeu.
-   **`AppClient` :** Gère les clients connectés et leurs contrôleurs associés.
-   **`AppServer` :** Gère les parties de jeu côté serveur.
-   **`DispatchServer` :** Redirige les clients vers les instances appropriées de `AppServer`.
-   **`UDPRequest` et `TCPRequest` :** Gèrent les requêtes entrantes sur le serveur.

## Difficultés Rencontrées et Solutions

1. **Gestion de plusieurs contrôleurs pour le client :**

    - Solution : Création d'une classe `Controller` pour faciliter l'interchangeabilité dans le fichier FXML.

2. **Gestion des connexions UDP et TCP :**

    - Solution : Centralisation des requêtes via une interface `Request`.

3. **Implémentation de parties solo avec des bots :**

    - Solution : Instanciation de bots depuis le serveur.

4. **Méthode de dispatch efficace pour le serveur dispatch :**
    - Solution : Développement d'une stratégie de dispatch basée sur la charge des serveurs.

## Conclusion

Le projet Goldfinder a été un défi passionnant et enrichissant, permettant de mettre en œuvre de nombreuses fonctionnalités et de surmonter plusieurs défis techniques. Grâce à une approche méthodique et à une collaboration efficace, nous avons pu développer un jeu multi-joueur et multi-serveur complet, offrant une expérience de jeu immersive et divertissante.
