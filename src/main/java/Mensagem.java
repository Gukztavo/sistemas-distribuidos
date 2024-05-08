public class Mensagem {
    private String conteudo;

    public Mensagem() {} // Construtor padrão necessário para deserialização

    public Mensagem(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
