package xadrez;

import jogoDeTabuleiro.Peca;
import jogoDeTabuleiro.Posicao;
import jogoDeTabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {
	
	private int rodada;
	private Color jogadorAtual;
	private Tabuleiro tabuleiro;
	
	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		rodada = 1;
		jogadorAtual = Color.WHITE;
		iniciarPartida();
	}
	
	public int getRodada() {
		return rodada;
	}
	
	public Color getJogadorAtual() {
		return jogadorAtual;
	}
	
	public PecaXadrez[][] getPecas() {
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i=0; i<tabuleiro.getLinhas(); i++) {
			for (int j=0; j<tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}
		return mat;
	}
	
	public boolean[][] possiveisMovimentos(PosicaoXadrez origemPosicao) {
		Posicao posicao = origemPosicao.toPosicao();
		validarPosicaoOrigem(posicao);
		return tabuleiro.peca(posicao).possiveisMovimentos();
	}
	
	public PecaXadrez movimentarXadrez(PosicaoXadrez posicaoOrigem, PosicaoXadrez posicaoDestino) {
		Posicao origem = posicaoOrigem.toPosicao();
		Posicao destino = posicaoDestino.toPosicao();
		validarPosicaoOrigem(origem);
		validarPosicaoDestino(origem, destino);
		Peca capturarPeca = mover(origem, destino);
		proximaRodada();
		return (PecaXadrez)capturarPeca;
	}
	
	private Peca mover(Posicao origem, Posicao destino) {
		Peca p = tabuleiro.removerPeca(origem);
		Peca pecaCapturada = tabuleiro.removerPeca(destino);
		tabuleiro.lugarPeca(p, destino);
		return pecaCapturada;	
	}
	
	private void validarPosicaoOrigem(Posicao posicao) {
		if (!tabuleiro.existePeca(posicao)) {
			throw new XadrezException("Nao existe peca na posicao de origem.");
		}
		if (jogadorAtual != ((PecaXadrez)tabuleiro.peca(posicao)).getColor()) {
			throw new XadrezException("A peca que escolhida pertence ao adversário.");
		}
		if(!tabuleiro.peca(posicao).existeMovimento()) {
			throw new XadrezException("Nao existe movimentos possiveis para essa peca.");
		}
	}
	
	private void validarPosicaoDestino(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).possivelMovimento(destino)) {
			throw new XadrezException("A peca escolhida nao pode se mover para a posicao de destino");
		}
	}
	
	private void proximaRodada() {
		rodada++;
		jogadorAtual = (jogadorAtual == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private void novoLugarPeca(char coluna, int linha, PecaXadrez peca) {
		tabuleiro.lugarPeca(peca, new PosicaoXadrez(coluna,linha).toPosicao());
	}
	
	private void iniciarPartida() {
		novoLugarPeca('c', 1, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('c', 2, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('d', 2, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('e', 2, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('e', 1, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('d', 1, new Rei(tabuleiro, Color.WHITE));

		novoLugarPeca('c', 7, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('c', 8, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('d', 7, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('e', 7, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('e', 8, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('d', 8, new Rei(tabuleiro, Color.BLACK));
	}
}
