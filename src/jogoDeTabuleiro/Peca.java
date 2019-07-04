package jogoDeTabuleiro;

public class Peca {
	
	protected Posicao posicao;
	private Tabuleiro tabuleiro;

	public Peca(Tabuleiro tabuleiro) {
		this.tabuleiro = tabuleiro;
		posicao = null;		//opcional, o java considera nulo o valor da variavel nao iniciada
	}

	protected Tabuleiro getTabuleiro() {
		return tabuleiro;
	}
	
}
