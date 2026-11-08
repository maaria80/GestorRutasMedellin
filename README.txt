GESTOR DE RUTAS - Java Swing + Jackson + JXMapViewer2

IDE (NetBeans / IntelliJ / Eclipse):
  1. Cree un proyecto Java y copie los 7 archivos de src/ .
  2. Agregue al proyecto TODOS los .jar de la carpeta lib/ (Libraries / Build Path).
  3. Ejecute Main.java. Para probar: "Cargar ruta" -> Medellin.json

Consola (JDK 11+):
  Windows:  javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
            java -cp "out;lib/*" Main
  Linux/Mac: javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
             java -cp "out:lib/*" Main

El mapa necesita internet (descarga los mosaicos de OpenStreetMap).
