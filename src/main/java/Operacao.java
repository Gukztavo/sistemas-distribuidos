import model.Pessoa;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;

import java.util.Scanner;

public class Operacao {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");
    Scanner scanner = new Scanner(System.in);

    static Pessoa currentUser = null;

    private static void login() {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String senha = scanner.nextLine();

        EntityManager manager = emf.createEntityManager();
        try {
            Pessoa pessoa = manager.createQuery("SELECT p FROM Pessoa p WHERE p.email = :email AND p.senha = :senha", Pessoa.class)
                    .setParameter("email", email)
                    .setParameter("senha", senha)
                    .getSingleResult();
            if (pessoa != null) {
                currentUser = pessoa;
                System.out.println("Login realizado com sucesso, " + pessoa.getNome() + "!");
            }
        } catch (NoResultException e) {
            System.out.println("Email ou senha incorretos ");
        } finally {
            manager.close();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ObjectMapper objectMapper = new ObjectMapper();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");

        login();
//        while (true) {
//            System.out.println("Menu:");
//            System.out.println("1. Register Person");
//            System.out.println("2. List People");
//            System.out.println("3. Remove Person");
//            System.out.println("4. Login");
//            System.out.println("5. Logout");
//            System.out.println("6. Exit");
//            System.out.print("Enter your choice: ");
//
//            int choice = scanner.nextInt();
//            scanner.nextLine(); // Consumir a nova linha deixada pelo nextInt
//
//            switch (choice) {
//                case 1:
//                    registerPerson();
//                    break;
//                case 2:
//                    listarPessoas();
//                    break;
//                case 3:
//                    removePessoa(scanner, emf);
//                    return;
//                case 4:
//                    login();
//                    break;
//                case 5:
//                    logout();
//                    System.out.println("teste");
//                    break;
//                default:
//                    System.out.println("Opção invalida, tente novamente ");
//            }
//        }
//    }
//
//
//
//     public static void registerPerson(String nome, String email, String senha) {
//        EntityManager manager = emf.createEntityManager();
//        try {
//            manager.getTransaction().begin();
//            Pessoa pessoa = new Pessoa(nome, senha, email);  // Supondo que Pessoa tenha um construtor correspondente
//            manager.persist(pessoa);
//            manager.getTransaction().commit();
//            System.out.println("Person inserted successfully into the database.");
//        } finally {
//            manager.close();
//        }
//    }
//
//    private static Pessoa safelyDeserialize(String jsonInput, ObjectMapper objectMapper) {
//        try {
//            return objectMapper.readValue(jsonInput, Pessoa.class);
//        } catch (JsonProcessingException e) {
//            System.err.println("Error processing JSON input: " + e.getMessage());
//            return null;  // ou poderia lançar uma RuntimeException se prefero tratar como erro fatal
//        }
//
//    }
//
//    public static void listarPessoas() {
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");
//        EntityManager manager = emf.createEntityManager();
//
//        try {
//            // Cria uma consulta para buscar todas as pessoas
//            List<Pessoa> pessoas = manager.createQuery("SELECT p FROM Pessoa p", Pessoa.class).getResultList();
//
//            // Verifica se a lista está vazia e imprime as pessoas
//            if (((List<?>) pessoas).isEmpty()) {
//                System.out.println("No people found in the database.");
//            } else {
//                for (Pessoa pessoa : pessoas) {
//                    System.out.println(" Name: " + pessoa.getNome() + ", Email: " + pessoa.getEmail() + ", Senha: " + pessoa.getSenha());
//                }
//            }
//        } finally {
//            manager.close();
//            emf.close();
//        }
//    }
//
//    private static void removePessoa(Scanner scanner, EntityManagerFactory emf) {
//        System.out.print("Entre com o id da pessoa ser removida: ");
//        Long id = scanner.nextLong();  // Lê o ID do usuário
//        scanner.nextLine();  // Limpa o buffer após ler o número
//
//        EntityManager manager = emf.createEntityManager();
//        try {
//            manager.getTransaction().begin();
//
//            // Encontrar a pessoa pelo ID
//            Pessoa pessoa = manager.find(Pessoa.class, id);
//
//            if (pessoa == null) {
//                System.out.println("Id: Não foi localizado " + id);
//            } else {
//                manager.remove(pessoa); // Remove a pessoa encontrada
//                manager.getTransaction().commit(); // Confirma a transação
//                System.out.println("Pessoa removida com sucesso");
//            }
//        } catch (Exception e) {
//            if (manager.getTransaction().isActive()) {
//                manager.getTransaction().rollback(); // Reverte a transação em caso de falha
//            }
//            System.err.println("Failed to remove the person: " + e.getMessage());
//        } finally {
//            manager.close();
//        }
//
//    }
//
//    private static Pessoa currentUser = null;
//
//    private static void login() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Enter email: ");
//        String email = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String senha = scanner.nextLine();
//
//        EntityManager manager = emf.createEntityManager();
//        try {
//            Pessoa pessoa = manager.createQuery("SELECT p FROM Pessoa p WHERE p.email = :email AND p.senha = :senha", Pessoa.class)
//                    .setParameter("email", email)
//                    .setParameter("senha", senha)
//                    .getSingleResult();
//            if (pessoa != null) {
//                currentUser = pessoa;
//                System.out.println("Login realizado com sucesso, " + pessoa.getNome() + "!");
//            }
//        } catch (NoResultException e) {
//            System.out.println("Email ou senha incorretos ");
//        } finally {
//            manager.close();
//        }
//    }
//
//    private static void logout() {
//        if (currentUser != null) {
//            System.out.println("Logout realziado com sucesso , " + currentUser.getNome() + "!");
//            currentUser = null;
//        } else {
//            System.out.println("usuario não localizado");
//        }
    }
}




