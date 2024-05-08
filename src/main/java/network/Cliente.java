package network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {
    protected static int defaultPort = 22222;
    private static String currentUserEmail; // Armazena o email do usuário atual

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String serverHostname = getInput(scanner, "Digite o nome do servidor (o padrão é 127.0.0.1): ", "127.0.0.1");
        int serverPort = getPort(scanner, "Digite o número da porta do servidor (o padrão é 22222): ", defaultPort);

        System.out.println("Tentando conectar ao servidor " + serverHostname + " na porta " + serverPort + ".");
        try (
                Socket echoSocket = new Socket(serverHostname, serverPort);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Conectado ao servidor.");
            handleUserOperations(stdIn, out, in);
        } catch (UnknownHostException e) {
            System.err.println("Não foi possível encontrar o host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Não foi possível obter E/S para a conexão com: " + serverHostname);
            System.exit(1);
        }
    }

    private static String getInput(Scanner scanner, String message, String defaultValue) {
        System.out.print(message);
        String input = scanner.nextLine();
        return input.isEmpty() ? defaultValue : input;
    }

    private static int getPort(Scanner scanner, String message, int defaultPort) {
        System.out.print(message);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Número de porta inválido. Usando a porta padrão " + defaultPort + ".");
            return defaultPort;
        }
    }

    private static void handleUserOperations(BufferedReader stdIn, PrintWriter out, BufferedReader in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            System.out.println("Escolha a operação:");
            System.out.println("1. Cadastrar Candidato");
            System.out.println("2. Login");
            System.out.println("3. Visualizar Perfil");
            System.out.println("4. Logout");
            System.out.print("Opção: ");
            String operationChoice = stdIn.readLine();

            ObjectNode json = mapper.createObjectNode();
            String operationResponse;

            switch (operationChoice) {
                case "1":
                    collectAndSendCandidateDetails(json, "cadastrarCandidato", stdIn, out);
                    break;
                case "2":
                    collectAndSendLoginDetails(json, "loginCandidato", stdIn, out);
                    break;
                case "3":
                    sendProfileRequest(json, out);
                    break;
                case "4":
                    sendLogoutRequest(json, out);
                    break;
                default:
                    System.out.println("Opção inválida. Encerrando o cliente.");
                    return;
            }

            operationResponse = in.readLine();
            System.out.println("Resposta do servidor: " + operationResponse);

            if (operationChoice.equals("2") && operationResponse.contains("200")) {
                currentUserEmail = json.get("email").asText();
            } else if (operationChoice.equals("4")) {
                currentUserEmail = null;
            }
        }
    }

    private static void collectAndSendCandidateDetails(ObjectNode json, String operation, BufferedReader stdIn, PrintWriter out) throws IOException {
        json.put("operacao", operation);
        System.out.println("Digite o nome do candidato:");
        json.put("nome", stdIn.readLine());
        System.out.println("Digite o email do candidato:");
        json.put("email", stdIn.readLine());
        System.out.println("Digite a senha do candidato:");
        json.put("senha", stdIn.readLine());
        out.println(json.toString());
    }

    private static void collectAndSendLoginDetails(ObjectNode json, String operation, BufferedReader stdIn, PrintWriter out) throws IOException {
        json.put("operacao", operation);
        System.out.println("Digite o email:");
        json.put("email", stdIn.readLine());
        System.out.println("Digite a senha:");
        json.put("senha", stdIn.readLine());
        out.println(json.toString());
    }

    private static void sendProfileRequest(ObjectNode json, PrintWriter out) {
        if (currentUserEmail == null) {
            System.out.println("Você precisa fazer login primeiro.");
            return;
        }
        json.put("operacao", "visualizarCandidato");
        json.put("email", currentUserEmail);
        out.println(json.toString());
    }

    private static void sendLogoutRequest(ObjectNode json, PrintWriter out) {
        if (currentUserEmail == null) {
            System.out.println("Você não está logado.");
            return;
        }
        json.put("operacao", "logout");
        json.put("token", currentUserEmail);
        out.println(json.toString());
    }
}
