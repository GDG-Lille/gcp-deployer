# simple-appengine-jersey

Simple Appengine Jersey est un projet pour créer et surtout déployer son API Rest en quelques minutes.

Quelles sont les étapes ?

### Création d'un projet Google Appengine
Aller sur la [console Google Cloud Platform](https://console.developers.google.com/project) et créer un nouveau projet.
Attention à l'identifiant du projet.

### Edition du projet en local
Editer le fichier [appengine-web.xml](https://github.com/GDG-Lille/simple-appengine-jersey/blob/master/src/main/webapp/WEB-INF/appengine-web.xml) et remplir <application>simple-appengine-jersey</application> avec son identifiant de projet Appengine.
Le projet est basé sur [Jersey](https://jersey.java.net/), implémentation du standard Jax-RS pour créer des APIs.

### Lancemenent en local
Grâcce au [plugin maven](https://cloud.google.com/appengine/docs/java/tools/maven), à la racine, 
` mvn clean package ` puis ` mvn appengine:devserver `

### Déploiement
` mvn appengine:update `
Votre application est ensuite disponible sur http://<IDENTIFIANT>.appspot.com
