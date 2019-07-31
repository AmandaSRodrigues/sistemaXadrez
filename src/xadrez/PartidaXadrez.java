package xadrez;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jogoDeTabuleiro.Peca;
import jogoDeTabuleiro.Posicao;
import jogoDeTabuleiro.Tabuleiro;
import xadrez.pecas.Bispo;
import xadrez.pecas.Cavalo;
import xadrez.pecas.Peao;
import xadrez.pecas.Rainha;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {
	
	private int rodada;
	private Color jogadorAtual;
	private Tabuleiro tabuleiro;
	private boolean check;
	private boolean checkMate;
	private PecaXadrez enPassantVulnerable;
	private PecaXadrez promocao;
	
	private List<Peca> pecasTabuleiro = new ArrayList<>();
	private List<Peca> pecasCapturadas = new ArrayList<>();
	
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
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	
	public PecaXadrez getEnPassantVulnereble() {
		return enPassantVulnerable;
	}
	
	public PecaXadrez getPromocao( ) {
		return promocao;
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
		
		if (testeCheck(jogadorAtual)) {
			desfazerMovimento(origem, destino, capturarPeca);
			throw new XadrezException("Voce nao pode se colocar em check");
		}

		PecaXadrez pecaMovida = (PecaXadrez)tabuleiro.peca(destino);
		
		// #movimento especial promocao
		promocao = null;
		if (pecaMovida instanceof Peao) {
			if (pecaMovida.getColor() == Color.WHITE && destino.getLinha() == 0 || pecaMovida.getColor() == Color.BLACK && destino.getLinha() == 7) {
				promocao = (PecaXadrez)tabuleiro.peca(destino);
				promocao = substituirPecaPromovida("Q");
				
			}
		}
		
		check = (testeCheck(oponente(jogadorAtual))) ? true : false;
		
		if (testeCheckMate(oponente(jogadorAtual))) {
			checkMate = true;
		}
		else {
			proximaRodada();
		}
		
		// #movimento especial en passant
		if(pecaMovida instanceof Peao && (destino.getLinha() == origem.getLinha() - 2 || destino.getLinha() == origem.getLinha() + 2)) {
			enPassantVulnerable = pecaMovida;
		}
		else
			enPassantVulnerable = null;
		
		return (PecaXadrez)capturarPeca;
	}
	
	public PecaXadrez substituirPecaPromovida(String tipo) {
		if (promocao == null) {
			throw new IllegalStateException("Nao existe peca para ser promovida");
		}
		if (!tipo.equals("B") && !tipo.equals("C") && !tipo.equals("T") && !tipo.equals("Q")) {
			throw new InvalidParameterException("Tipo de promocao invalida");
		}
		
		Posicao pos = promocao.getPosicaoXadrez().toPosicao();
		Peca p = tabuleiro.removerPeca(pos);
		pecasTabuleiro.remove(p);
		
		PecaXadrez novaPeca = novaPeca(tipo, promocao.getColor());
		tabuleiro.lugarPeca(novaPeca, pos);
		pecasTabuleiro.add(novaPeca);
		
		return novaPeca;
	}
	
	private PecaXadrez novaPeca(String tipo, Color color) {
		if (tipo.equals("B")) return new Bispo(tabuleiro, color);
		if (tipo.equals("C")) return new Cavalo(tabuleiro, color);
		if (tipo.equals("T")) return new Torre(tabuleiro, color);
		return new Rainha(tabuleiro, color);
	}
	
	private Peca mover(Posicao origem, Posicao destino) {
		PecaXadrez p = (PecaXadrez)tabuleiro.removerPeca(origem);
		p.aumentarContagemMovimentos();
		Peca pecaCapturada = tabuleiro.removerPeca(destino);
		tabuleiro.lugarPeca(p, destino);
		
		if (pecaCapturada != null) {
			pecasTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}
		
		// #movimento especial roque pequeno
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez)tabuleiro.removerPeca(origemT);
			tabuleiro.lugarPeca(torre, destinoT);
			torre.aumentarContagemMovimentos();
		}
		
		// #movimento especial roque grande
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez)tabuleiro.removerPeca(origemT);
			tabuleiro.lugarPeca(torre, destinoT);
			torre.aumentarContagemMovimentos();
		}
		
		// #movimento especial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == null) {
				Posicao posicaoPeao;
				if (p.getColor() == Color.WHITE) {
					posicaoPeao = new Posicao(destino.getLinha() + 1, destino.getColuna());
				}
				else {
					posicaoPeao = new Posicao(destino.getLinha() - 1, destino.getColuna());
				}
				pecaCapturada = tabuleiro.removerPeca(posicaoPeao);
				pecasCapturadas.add(pecaCapturada);
				pecasTabuleiro.remove(pecaCapturada);
			}
		}
		
		return pecaCapturada;	
	}
	
	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		PecaXadrez p = (PecaXadrez)tabuleiro.removerPeca(destino);
		p.diminuirContagemMovimentos();
		tabuleiro.lugarPeca(p, origem);

		if (pecaCapturada != null) {
			tabuleiro.lugarPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasTabuleiro.add(pecaCapturada);
		}
		
		// #movimento especial roque pequeno
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao posOriTorre = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao posDesTorre = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez)tabuleiro.removerPeca(posDesTorre);
			tabuleiro.lugarPeca(torre, posOriTorre);
			torre.diminuirContagemMovimentos();
		}
		
		// #movimento especial roque grande
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez)tabuleiro.removerPeca(destinoT);
			tabuleiro.lugarPeca(torre, origemT);
			torre.diminuirContagemMovimentos();
		}
		
		// #movimento especial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == enPassantVulnerable) {
				PecaXadrez peao = (PecaXadrez)tabuleiro.removerPeca(destino);
				Posicao posicaoPeao;
				if (p.getColor() == Color.WHITE) {
					posicaoPeao = new Posicao(3, destino.getColuna());
				}
				else {
					posicaoPeao = new Posicao(4, destino.getColuna());
				}
				tabuleiro.lugarPeca(peao, posicaoPeao);
			}
		}
	}	
	
	private void validarPosicaoOrigem(Posicao posicao) {
		if (!tabuleiro.existePeca(posicao)) {
			throw new XadrezException("Nao existe peca na posicao de origem.");
		}
		if (jogadorAtual != ((PecaXadrez)tabuleiro.peca(posicao)).getColor()) {
			throw new XadrezException("A peca que escolhida pertence ao adversario.");
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
	
	private Color oponente(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private PecaXadrez Rei(Color color) {
		List<Peca> list = pecasTabuleiro.stream().filter(x -> ((PecaXadrez)x).getColor() == color).collect(Collectors.toList());
		for (Peca p : list) {
			if (p instanceof Rei) {
				return (PecaXadrez)p;
			}
		}
		throw new IllegalStateException("Nao existe um rei " + color + " no tabuleiro.");
	}

	private boolean testeCheck(Color color) {
		Posicao posicaoRei = Rei(color).getPosicaoXadrez().toPosicao();
		List<Peca> pecasOponente = pecasTabuleiro.stream().filter(x -> ((PecaXadrez)x).getColor() == oponente(color)).collect(Collectors.toList());
		for (Peca p : pecasOponente) {
			boolean[][] mat = p.possiveisMovimentos();
			if (mat[posicaoRei.getLinha()][posicaoRei.getColuna()]) {
				return true;
			}
		}
		return false;
	}
  
	private boolean testeCheckMate(Color color) {
		if(!testeCheck(color)) {
			return false;
		}
		List<Peca> list = pecasTabuleiro.stream().filter(x -> ((PecaXadrez)x).getColor() == color).collect(Collectors.toList());
		for (Peca p: list) {
			boolean [][] mat = p.possiveisMovimentos();
			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Posicao origem = ((PecaXadrez)p).getPosicaoXadrez().toPosicao();
						Posicao destino = new Posicao(i, j);
						Peca pecaCapturada = mover(origem, destino);
						boolean testeCheck = testeCheck(color);
						desfazerMovimento(origem, destino, pecaCapturada);
						if (!testeCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private void novoLugarPeca(char coluna, int linha, PecaXadrez peca) {
		tabuleiro.lugarPeca(peca, new PosicaoXadrez(coluna,linha).toPosicao());
		pecasTabuleiro.add(peca);
	}
	
	private void iniciarPartida() {
		novoLugarPeca('a', 1, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('b', 1, new Cavalo(tabuleiro, Color.WHITE));
		novoLugarPeca('c', 1, new Bispo(tabuleiro, Color.WHITE));
		novoLugarPeca('d', 1, new Rainha(tabuleiro, Color.WHITE));
		novoLugarPeca('e', 1, new Rei(tabuleiro, Color.WHITE, this));
		novoLugarPeca('f', 1, new Bispo(tabuleiro, Color.WHITE));
		novoLugarPeca('g', 1, new Cavalo(tabuleiro, Color.WHITE));
		novoLugarPeca('h', 1, new Torre(tabuleiro, Color.WHITE));
		novoLugarPeca('a', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('b', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('c', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('d', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('e', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('f', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('g', 2, new Peao(tabuleiro, Color.WHITE, this));
		novoLugarPeca('h', 2, new Peao(tabuleiro, Color.WHITE, this));
		
		novoLugarPeca('a', 8, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('b', 8, new Cavalo(tabuleiro, Color.BLACK));
		novoLugarPeca('c', 8, new Bispo(tabuleiro, Color.BLACK));
		novoLugarPeca('d', 8, new Rainha(tabuleiro, Color.BLACK));
		novoLugarPeca('e', 8, new Rei(tabuleiro, Color.BLACK, this));
		novoLugarPeca('f', 8, new Bispo(tabuleiro, Color.BLACK));
		novoLugarPeca('g', 8, new Cavalo(tabuleiro, Color.BLACK));
		novoLugarPeca('h', 8, new Torre(tabuleiro, Color.BLACK));
		novoLugarPeca('a', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('b', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('c', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('d', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('e', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('f', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('g', 7, new Peao(tabuleiro, Color.BLACK, this));
		novoLugarPeca('h', 7, new Peao(tabuleiro, Color.BLACK, this));
	}
}
