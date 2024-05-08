package network;

import model.Pessoa;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Server {

    private static final int PORT = 22222;
    private static ExecutorService clientTaskPool = Executors.newCachedThreadPool();
    private static SessionFactory sessionFactory;
    private static Map<String, String> emailToSessionMap = new HashMap<>();
    private static Map<String, Pessoa> sessionToUserMap = new HashMap<>();

    public static void main(String[] args) {
        sessionFactory = HibernateUtil.getSessionFactory();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                clientTaskPool.execute(new ClientRequestHandler(serverSocket.accept()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static class ClientRequestHandler implements Runnable {

        private Socket clientSocket;

        public ClientRequestHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String clientRequest;
                while ((clientRequest = inputReader.readLine()) != null) {
                    System.out.println("Received operation from client: " + clientRequest);
                    handleClientRequest(clientRequest, outputWriter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleClientRequest(String requestData, PrintWriter responseWriter) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseNode = mapper.createObjectNode();
                JsonNode requestJson = mapper.readTree(requestData);

                String operationType = requestJson.get("operacao").asText();
                switch (operationType) {
                    case "cadastrarCandidato":
                        registerCandidate(requestJson, responseNode, responseWriter);
                        break;
                    case "loginCandidato":
                        loginUser(requestJson, responseNode, responseWriter);
                        break;
                    case "visualizarCandidato":
                        viewCandidateProfile(requestJson, responseNode, responseWriter);
                        break;
                    case "logout":
                        logoutUser(requestJson, responseNode, responseWriter);
                        break;
                    default:
                        responseNode.put("status", 400);
                        responseNode.put("mensagem", "Operação inválida");
                        responseWriter.println(responseNode.toString());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                responseWriter.println("{\"status\": 500, \"mensagem\": \"Erro ao processar a operação\"}");
            }
        }

        private void registerCandidate(JsonNode requestData, ObjectNode responseNode, PrintWriter responseWriter) {
            String nome = requestData.get("nome").asText();
            String email = requestData.get("email").asText();
            String senha = requestData.get("senha").asText();

            if (isEmailAlreadyExists(email)) {
                responseNode.put("status", 400);
                responseNode.put("mensagem", "E-mail já cadastrado");
            } else if (!isValidEmail(email)) {
                responseNode.put("status", 400);
                responseNode.put("mensagem", "Formato de e-mail inválido");
            } else if (!isValidPassword(senha)) {
                responseNode.put("status", 400);
                responseNode.put("mensagem", "Senha inválida. Deve conter apenas caracteres numéricos e ter entre 3 e 8 caracteres");
            } else if (!isValidName(nome)) {
                responseNode.put("status", 400);
                responseNode.put("mensagem", "Nome inválido. Deve ter entre 6 e 30 caracteres");
            } else {
                int status = createUser(nome, email, senha);
                responseNode.put("status", status);
                if (status == 201) {
                    String token = UUID.randomUUID().toString();
                    emailToSessionMap.put(email, token);
                    sessionToUserMap.put(token, getUserByEmail(email));
                    responseNode.put("token", token);
                }
            }
            responseWriter.println(responseNode.toString());
        }

        private void loginUser(JsonNode requestData, ObjectNode responseNode, PrintWriter responseWriter) {
            String email = requestData.get("email").asText();
            String senha = requestData.get("senha").asText();

            Pessoa user = getUserByEmailAndPassword(email, senha);
            if (user != null) {
                String token = UUID.randomUUID().toString();
                emailToSessionMap.put(email, token);
                sessionToUserMap.put(email, user);
                responseNode.put("status", 200);
                responseNode.put("token", token);
            } else {
                responseNode.put("status", 401);
                responseNode.put("mensagem", "E-mail ou senha incorretos");
            }
            responseWriter.println(responseNode.toString());
        }

        private void viewCandidateProfile(JsonNode requestData, ObjectNode responseNode, PrintWriter responseWriter) {
            String email = requestData.get("email").asText();
            Pessoa user = sessionToUserMap.get(email);
            if (user != null) {
                responseNode.put("status", 200);
                responseNode.put("nome", user.getNome());
                responseNode.put("email", user.getEmail());
            } else {
                responseNode.put("status", 401);
                responseNode.put("mensagem", "Token de autenticação inválido");
            }
            responseWriter.println(responseNode.toString());
        }

        private void logoutUser(JsonNode requestData, ObjectNode responseNode, PrintWriter responseWriter) {
            String token = requestData.get("token").asText();

            if (token == null || token.isEmpty()) {
                responseNode.put("status", 401);
                responseNode.put("mensagem", "Token de autenticação é nulo ou vazio");
                responseWriter.println(responseNode.toString());
                return;
            }
            // Encontra o email do usuário associado ao token
            String userEmail = emailToSessionMap.entrySet().stream()
                    .filter(entry -> token.equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            System.out.println("cheguei antes do if");
            if (userEmail == null) {
                // Remoção do usuário e do token
                emailToSessionMap.remove(userEmail);
                sessionToUserMap.remove(userEmail);

                responseNode.put("status", 200);
                responseNode.put("mensagem", "Logout realizado com sucesso.");
                System.out.println("Logout realizado com sucesso para o usuário: " + userEmail);
                System.out.println("cheguei no if ");
            } else {
                responseNode.put("status", 401);
                responseNode.put("mensagem", "Token de autenticação inválido ou sessão já encerrada.");
                System.out.println("Falha no logout: Token inválido ou sessão já encerrada.");
            }

            responseWriter.println(responseNode.toString());
        }



//        private void logoutUser(JsonNode requestData, ObjectNode responseNode, PrintWriter responseWriter) {
//            String token = requestData.get("token").asText();
//            // aCHA O email do usaurio com base na sessão
//            String userEmail = emailToSessionMap.entrySet().stream()
//                    .filter(entry -> token.equals(entry.getValue()))
//                    .map(Map.Entry::getKey)
//                    .findFirst()
//                    .orElse(null);
//
//            if (userEmail != null) {
//                emailToSessionMap.remove(userEmail);
//                sessionToUserMap.remove(token);
//                responseNode.put("status", 200);
//                responseNode.put("mensagem", "Logout realizado com sucesso");
//            } else {
//                responseNode.put("status", 401);
//                responseNode.put("mensagem", "Token de autenticação inválido");
//            }
//            responseWriter.println(responseNode.toString());
//        }

        // Utility methods to validate and manage user data
        private boolean isEmailAlreadyExists(String email) {
            try (Session session = sessionFactory.openSession()) {
                return session.createQuery("FROM pessoa WHERE email = :email", Pessoa.class)
                        .setParameter("email", email)
                        .uniqueResult() != null;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean isValidEmail(String email) {
            String emailRegex = "^(.+)@(.+)$";
            Pattern pattern = Pattern.compile(emailRegex);
            return pattern.matcher(email).matches() && email.length() >= 7 && email.length() <= 50;
        }

        private boolean isValidPassword(String password) {
            return password.matches("\\d+") && password.length() >= 3 && password.length() <= 8;
        }

        private boolean isValidName(String name) {
            return name.length() >= 6 && name.length() <= 30;
        }

        private int createUser(String nome, String email, String senha) {
            Pessoa user = new Pessoa(nome, email, senha);
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                session.save(user);
                transaction.commit();
                return 201;
            } catch (Exception e) {
                e.printStackTrace();
                return 500; // HTTP Internal Server Error status code
            }
        }

        private Pessoa getUserByEmailAndPassword(String email, String senha) {
            try (Session session = sessionFactory.openSession()) {
                return session.createQuery("FROM pessoa WHERE email = :email AND senha = :senha", Pessoa.class)
                        .setParameter("email", email)
                        .setParameter("senha", senha)
                        .uniqueResult();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private Pessoa getUserByEmail(String email) {
            try (Session session = sessionFactory.openSession()) {
                return session.createQuery("FROM pessoa WHERE email = :email", Pessoa.class)
                        .setParameter("email", email)
                        .uniqueResult();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

