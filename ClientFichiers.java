import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientFichiers extends Thread {
    public static ConfigManager conf = new ConfigManager();
    private static final String HOST =conf.getConfigValue("ip_server") ;
    private static final int PORT = Integer.parseInt(conf.getConfigValue("port_server"));
    public static void main(String[] args) throws Exception {
        String[] commande = {"MENU", "UPLOAD", "LIST", "REMOVE", "DOWNLOAD"};
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Bienvenu sur FL transfert");
            System.out.println("Voici les commandes que vous pouvez exécuter :");
            System.out.println("1 pour uploader");
            System.out.println("2 pour lister");
            System.out.println("-1 pour quitter");
            System.out.print("Saisissez votre commande : ");
            int num_com = scanner.nextInt();
            scanner.nextLine();  
            if (num_com == -1) {
                System.out.println("Au revoir!");
                break;  
            }
            if (num_com == 1) {
                uploader(scanner, commande[1]);
            }
            else if (num_com == 2) {
                lister(scanner, commande[2]);
            } 
            else {
                System.out.println("Commande inconnue, veuillez réessayer.");
            }
        }
        scanner.close();
    }
    public static void uploader(Scanner scanner, String commande) {
        while (true) {
            System.out.println("q pour quitter l'upload");
            System.out.print("Entrez le chemin du fichier à uploader : ");
            String filename = scanner.nextLine();

            // Si l'utilisateur veut quitter l'upload
            if (filename.equalsIgnoreCase("q")) {
                break;
            }

            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("Le fichier n'existe pas. Essayez à nouveau.");
                continue;
            }

            // Effectuer la commande d'upload (assurez-vous que ClientFichiers.client est bien implémenté)
            System.out.println("Votre commande : " + commande);
            System.out.println("Fichier : " + filename);

            try {
                // Appel à la méthode d'upload
                ClientFichiers.client(commande, file);
                break;  // Sortir de la boucle après un upload réussi
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erreur lors de l'upload, réessayez.");
            }
        }
    }

    public static void lister(Scanner scanner, String commande) throws Exception {
        File[] file = null;
        file = ClientFichiers.list();       
        boolean quitterListe = true; 
        while (quitterListe) {
            String[] fichiers = new String[file.length];
            int k = 0;
            for (File file2 : file) {
                fichiers[k] = file2.getName();
                k++;
            }
            System.out.println("Commande : " + commande);

            System.out.println("Liste des fichiers disponibles :");
            for (int i = 0; i < fichiers.length; i++) {
                System.out.println((i + 1) + ". " + fichiers[i]);
            }

  
            System.out.print("Sélectionnez un fichier (entrez le numéro) ou tapez 'MENU' pour revenir au menu principal : ");
            String choix = scanner.nextLine();

            if (choix.equalsIgnoreCase("MENU")) {
                quitterListe = false;
                System.out.println("Retour au menu principal...");
                continue;
            }
            int choixFichier;
            try {
                choixFichier = Integer.parseInt(choix);
                if (choixFichier < 1 || choixFichier > fichiers.length) {
                    System.out.println("Choix invalide, veuillez réessayer.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide, veuillez entrer un numéro ou 'MENU'.");
                continue;
            }

            String fichierSelectionne = fichiers[choixFichier - 1];
            System.out.println("Vous avez sélectionné : " + fichierSelectionne);

            System.out.println("Que souhaitez-vous faire ?");
            System.out.println("Tapé :");
            System.out.println("1 pour Télécharger (download)");
            System.out.println("2 pour Supprimer (remove)");
            System.out.print("Entrez votre choix (1 ou 2) : ");
            String actionChoisie = scanner.nextLine();

            if ("1".equals(actionChoisie)) {
                File f = new File(fichierSelectionne);
                try {
                    ClientFichiers.client("DOWNLOAD",f);
                } catch (Exception e) {
                   System.out.println("Une erreur est survenue  lors du telechargement !!! ... veuillez patienter un petit moment ");
                }
            } else if ("2".equals(actionChoisie)) {
                try {
                    File f2 = new File(fichierSelectionne);
                     ClientFichiers.client("REMOVE",f2); 
                    System.out.println("Suppression du fichier " + fichierSelectionne);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Erreur lors de la suppression.");
                }
            } else {
                System.out.println("Action inconnue. Veuillez entrer '1' pour télécharger ou '2' pour supprimer.");
                continue;
            }

            System.out.print("Souhaitez-vous revenir à la liste des fichiers ? (oui/non) : ");
            String reponse = scanner.nextLine();
            if ("oui".equalsIgnoreCase(reponse)) {
                file = ClientFichiers.list();
            }
            if (reponse.equalsIgnoreCase("non")) {
                quitterListe = false;
                System.out.println("Retour au menu principal...");
            }
        }
    }
    public static void client(String commande,File fichier) throws Exception {
        File dossierClient = new File("client_files");
        if (!dossierClient.exists()) {
            dossierClient.mkdir();
            System.out.println("Dossier 'client_files' créé pour stocker les fichiers téléchargés.");
        }
        try (
             Socket socket = new Socket(HOST, PORT);             
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            System.out.println("Essaie de connection  au serveur principal...tourant au port "+PORT+"en cours ");
            for (int i = 0; i < 5; i++) {
                if (i == 4) {
                    System.out.println("Liaisons etablie avec le serveur.");
                }
             }
            while (true) {

                if (commande.equalsIgnoreCase("EXIT")) {
                    System.out.println("Déconnexion en cours ...");
                    output.writeUTF("EXIT");
                    break;
                }
                if (commande.equalsIgnoreCase("REMOVE")) {
                    System.out.println("Suppression..."+fichier.getName());
                    output.writeUTF("REMOVE "+fichier.getName());
                    break;
                }

                if (commande.equals("UPLOAD")) {
                    if (!fichier.exists() || !fichier.isFile()) {
                        System.out.println("Erreur : Fichier introuvable!!!");
                        continue;
                    }
                    output.writeUTF("UPLOAD " + fichier.getName());
                    try (FileInputStream fis = new FileInputStream(fichier)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                        output.flush();
                        System.out.println("Fichier envoyer avec succes");
                        break;
                    }
                } else if (commande.equals("DOWNLOAD")) {
                    output.writeUTF("DOWNLOAD " + fichier.getName());
                    File fichierRecu = new File(dossierClient,fichier.getName()); // Sauvegarde dans client_files
                    try (FileOutputStream fos = new FileOutputStream(fichierRecu)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Fichier " + fichier.getName() + " téléchargé avec succès et sauvegardé dans 'client_files'.");
                        break;
                    }                   
                } else {
                    System.out.println("Commande inconnue. Veuillez réessayer a nouveau.");
                }
            }

        } catch (IOException e) {
            System.out.println("Le serveur n'est pas active impossible d etablir une connection!!!");
            System.out.println("Serveur not found.");
        }
    }
    public static File[] list() throws Exception {
        try (
             Socket socket = new Socket(HOST, PORT);             
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                output.writeUTF("LIST");
                File[] F = receivelist(socket); 
                 return F;  

        } catch (IOException e) {
            System.out.println("Le serveur n'est pas active impossible d etablir une connection!!!");
            System.out.println("Serveur not found.");
        }
                return null;
    }
    
    public static File[] receivelist(Socket client) throws Exception{
        ObjectInputStream obs = null;
        try {
            obs = new ObjectInputStream(client.getInputStream());
        File[] list = (File[])obs.readObject();
        return list;
        } catch (Exception e) {
            System.out.println("Corruption du signal ou le serveur est hors ligne");
        }
        return null;
    }
}
