package com.example.filemanagement;

// ===== IMPORTS =====
// Flux d'octets (fichiers binaires) : lecture et écriture avec tampon
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
// Flux de caractères (fichiers texte) : lecture et écriture avec tampon
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;   // lit des octets bruts depuis un fichier
import java.io.FileOutputStream;  // écrit des octets bruts dans un fichier
import java.io.FileReader;        // lit des caractères depuis un fichier
import java.io.FileWriter;        // écrit des caractères dans un fichier
import java.io.IOException;       // exception levée en cas d'erreur d'entrée/sortie
import java.io.PrintWriter;       // écriture formatée (println, printf)
// API moderne (Java 7+) : java.nio.file
import java.nio.file.Files;                 // méthodes statiques pour manipuler les fichiers
import java.nio.file.NoSuchFileException;   // exception : le fichier n'existe pas
import java.nio.file.Path;                  // représente un chemin de fichier ou de dossier
import java.nio.file.Paths;                 // permet de créer un Path à partir d'une chaîne
import java.nio.file.StandardCopyOption;    // options de copie (ex. REPLACE_EXISTING)
import java.nio.file.attribute.BasicFileAttributes; // métadonnées : taille, date, type
import java.util.List;

public class Main {

    // Point d'entrée du programme : il appelle chaque méthode pour la tester
    public static void main(String[] args) {
        System.out.println("TP 12 : Gestion des fichiers en Java");
        System.out.println("Environnement prêt pour les tests.");

        // Crée le dossier data et le fichier employees.txt s'ils n'existent pas
        prepareTestData();

        System.out.println("\n--- Lecture avec BufferedReader ---");
        // Lit employees.txt ligne par ligne et l'affiche
        readFileWithBufferedReader("data/employees.txt");

        System.out.println("\n--- Écriture avec BufferedWriter ---");
        // Écrit dans output.txt, puis le relit pour vérifier le contenu
        writeFileWithBufferedWriter("data/output.txt", "Contenu de test");
        readFileWithBufferedReader("data/output.txt");

        System.out.println("\n--- Écriture avec PrintWriter ---");
        // Écriture formatée dans report.txt, puis relecture
        writeWithPrintWriter("data/report.txt");
        readFileWithBufferedReader("data/report.txt");

        System.out.println("\n--- API Files (lecture / écriture) ---");
        // Même opérations qu'avant, mais avec l'API moderne Files
        readWithFilesApi("data/employees.txt");
        writeWithFilesApi("data/nio_output.txt", "Contenu écrit avec l'API Files");
        readWithFilesApi("data/nio_output.txt");

        System.out.println("\n--- Copie et déplacement ---");
        // Copie : l'original est conservé
        copyFile("data/employees.txt", "data/employees_backup.txt");
        // Déplacement : l'original disparaît, le fichier existe sous le nouveau nom
        moveFile("data/nio_output.txt", "data/moved_output.txt");

        System.out.println("\n--- Suppression ---");
        // Supprime un fichier existant
        deleteFile("data/moved_output.txt");
        // Teste le cas d'un fichier absent : un message d'information s'affiche
        deleteFile("data/fichier_inexistant.txt");

        System.out.println("\n--- Fichiers binaires ---");
        Path image = Paths.get("data/test.jpg");
        // On teste d'abord que l'image existe pour éviter une erreur
        if (Files.exists(image)) {
            // Affiche la taille de l'image en octets
            readBinaryFile("data/test.jpg");
            try {
                // Lit tous les octets de l'image en mémoire (tableau de bytes)
                byte[] data = Files.readAllBytes(image);
                // Écrit ces octets dans un nouveau fichier : c'est une copie binaire
                writeBinaryFile("data/copy_binary.bin", data);
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture des octets : " + e.getMessage());
            }
        } else {
            System.out.println("Placez une image data/test.jpg pour tester les fichiers binaires.");
        }

        System.out.println();
        // Affiche taille, date de modification et type du fichier
        getFileMetadata("data/employees.txt");
    }

    // ===== PRÉPARATION DES DONNÉES DE TEST =====
    // Crée le dossier data/ et le fichier employees.txt si nécessaire
    public static void prepareTestData() {
        try {
            Path dir = Paths.get("data");
            // createDirectories ne fait rien si le dossier existe déjà
            Files.createDirectories(dir);
            // resolve() ajoute un nom de fichier au chemin : data/employees.txt
            Path employees = dir.resolve("employees.txt");
            // On ne crée le fichier que s'il n'existe pas déjà
            if (!Files.exists(employees)) {
                // Files.write écrit chaque élément de la liste sur une ligne
                Files.write(employees, List.of(
                        "Alice,25,Engineer",
                        "Bob,30,Manager",
                        "Charlie,28,Designer"));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la préparation des données : " + e.getMessage());
        }
    }

    // ===== LECTURE : FileReader + BufferedReader =====
    // FileReader lit les caractères du fichier.
    // BufferedReader ajoute un tampon et permet de lire ligne par ligne (readLine).
    public static void readFileWithBufferedReader(String filePath) {
        // try-with-resources : le flux est fermé automatiquement à la fin du bloc,
        // même en cas d'erreur. On n'a donc pas besoin d'appeler close().
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // readLine() renvoie null quand il n'y a plus de ligne à lire
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            // Si le fichier n'existe pas ou n'est pas lisible, on affiche un message clair
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }

    // ===== ÉCRITURE : FileWriter + BufferedWriter =====
    // Attention : sans option "append", le fichier est ÉCRASÉ s'il existe déjà.
    public static void writeFileWithBufferedWriter(String filePath, String content) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(content);   // écrit le texte reçu en paramètre
            bw.newLine();        // saute une ligne (adapté au système d'exploitation)
            bw.write("Ligne ajoutée avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }

    // ===== ÉCRITURE FORMATÉE : PrintWriter =====
    // PrintWriter fonctionne comme System.out : println, printf, print.
    public static void writeWithPrintWriter(String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(filePath))) {
            pw.println("=== Rapport des employés ===");
            // printf : %s = texte, %d = nombre entier, %n = saut de ligne
            pw.printf("Nom: %s, Âge: %d, Poste: %s%n", "Alice", 25, "Engineer");
            pw.printf("Nom: %s, Âge: %d, Poste: %s%n", "Bob", 30, "Manager");
            pw.println("Fin du rapport.");
        } catch (IOException e) {
            // Ce catch ne capte que les erreurs d'ouverture du fichier.
            // PrintWriter lui-même n'est pas bloquant : pw.checkError() permet de détecter
            // une erreur d'écriture.
            System.err.println("Erreur lors de l'écriture : " + e.getMessage());
        }
    }

    // ===== API java.nio.file.Files : LECTURE =====
    public static void readWithFilesApi(String filePath) {
        try {
            // Path = représentation moderne d'un chemin de fichier
            Path path = Paths.get(filePath);
            // readAllLines lit tout le fichier d'un coup et renvoie une liste de lignes.
            // Pratique pour les petits fichiers, à éviter pour les très gros.
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture : " + e.getMessage());
        }
    }

    // ===== API java.nio.file.Files : ÉCRITURE =====
    public static void writeWithFilesApi(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            // Files.write écrit des octets : on convertit donc le texte avec getBytes().
            // Le fichier est créé s'il n'existe pas, écrasé sinon.
            Files.write(path, content.getBytes());
            System.out.println("Fichier écrit avec succès : " + filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture : " + e.getMessage());
        }
    }

    // ===== COPIE =====
    public static void copyFile(String source, String destination) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);
            // REPLACE_EXISTING : écrase la destination si elle existe déjà.
            // Sans cette option, une FileAlreadyExistsException serait levée.
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier copié de " + source + " vers " + destination);
        } catch (IOException e) {
            System.err.println("Erreur lors de la copie : " + e.getMessage());
        }
    }

    // ===== DÉPLACEMENT =====
    // move() déplace le fichier ou le renomme : l'original n'existe plus ensuite.
    public static void moveFile(String source, String destination) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier déplacé de " + source + " vers " + destination);
        } catch (IOException e) {
            System.err.println("Erreur lors du déplacement : " + e.getMessage());
        }
    }

    // ===== SUPPRESSION =====
    public static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            // On vérifie d'abord que le fichier existe
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("Fichier supprimé : " + filePath);
            } else {
                System.out.println("Le fichier n'existe pas : " + filePath);
            }
        } catch (NoSuchFileException e) {
            // Cas rare : le fichier a disparu entre le test exists() et delete()
            System.err.println("Le fichier a été supprimé entre-temps : " + e.getMessage());
        } catch (IOException e) {
            // Autres erreurs : droits insuffisants, fichier utilisé par un autre programme...
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ===== LECTURE BINAIRE =====
    // Un fichier binaire (image, PDF...) se lit octet par octet, pas comme du texte.
    public static void readBinaryFile(String filePath) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            // Tampon de 1024 octets : on lit le fichier par blocs
            byte[] buffer = new byte[1024];
            int bytesRead;           // nombre d'octets réellement lus à chaque tour
            long totalBytes = 0;     // compteur de la taille totale
            // read() renvoie -1 quand la fin du fichier est atteinte
            while ((bytesRead = bis.read(buffer)) != -1) {
                totalBytes += bytesRead;
            }
            System.out.println("Fichier binaire lu : " + filePath);
            System.out.println("Taille totale : " + totalBytes + " octets");
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture binaire : " + e.getMessage());
        }
    }

    // ===== ÉCRITURE BINAIRE =====
    public static void writeBinaryFile(String filePath, byte[] data) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            bos.write(data);  // écrit tous les octets du tableau
            bos.flush();      // force l'écriture du tampon sur le disque
            System.out.println("Fichier binaire écrit : " + filePath);
            System.out.println("Taille écrite : " + data.length + " octets");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture binaire : " + e.getMessage());
        }
    }

    // ===== MÉTADONNÉES =====
    // Les métadonnées décrivent le fichier : taille, dates, type...
    public static void getFileMetadata(String filePath) {
        try {
            Path path = Paths.get(filePath);
            // readAttributes lit les attributs de base du fichier en une seule opération
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("=== Métadonnées du fichier : " + filePath + " ===");
            System.out.println("Taille : " + attrs.size() + " octets");
            System.out.println("Dernière modification : " + attrs.lastModifiedTime().toInstant());
            System.out.println("Est un répertoire : " + attrs.isDirectory());
            System.out.println("Est un fichier : " + attrs.isRegularFile());
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des métadonnées : " + e.getMessage());
        }
    }
}