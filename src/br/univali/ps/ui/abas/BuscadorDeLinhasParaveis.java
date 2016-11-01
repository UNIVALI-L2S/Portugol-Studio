package br.univali.ps.ui.abas;

import br.univali.portugol.nucleo.Programa;
import br.univali.portugol.nucleo.asa.ASAPrograma;

/**
 *
 * @author elieser
 */
import br.univali.portugol.nucleo.asa.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Luiz Fernando
 * @author Elieser
 */
public final class BuscadorDeLinhasParaveis extends VisitanteNulo {

    private static final Logger LOGGER = Logger.getLogger(BuscadorDeLinhasParaveis.class.getName());

    private final Set<Integer> linhasParaveis = new HashSet<>();

    private static final Set<Class> classesParaveis = new HashSet<>();

    static {
        classesParaveis.add(NoCaso.class);
        classesParaveis.add(NoChamadaFuncao.class);
        classesParaveis.add(NoDeclaracaoMatriz.class);
        classesParaveis.add(NoDeclaracaoVetor.class);
        classesParaveis.add(NoDeclaracaoParametro.class);
        classesParaveis.add(NoDeclaracaoVariavel.class);
        classesParaveis.add(NoEnquanto.class);
        classesParaveis.add(NoPara.class);
        classesParaveis.add(NoEscolha.class);
        classesParaveis.add(NoOperacaoAtribuicao.class);
        classesParaveis.add(NoOperacao.class);
        classesParaveis.add(NoSe.class);
    }

    private boolean verificaSePodeParar(NoBloco noBloco) {
        if (classesParaveis.contains(noBloco.getClass())) {
            int linha = noBloco.getTrechoCodigoFonte().getLinha();
            if (linha >= 0) 
            {
                linhasParaveis.add(linha);
                return true;
            }
        }
        return false;
    }

    public Set<Integer> getLinhasParaveis(Programa programa) {
        linhasParaveis.clear();
        try {
            programa.getArvoreSintaticaAbstrata().aceitar(this);
        } catch (ExcecaoVisitaASA ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return linhasParaveis;
    }

    @Override
    public Object visitar(ASAPrograma asap) throws ExcecaoVisitaASA {
        for (NoDeclaracao declaracao : asap.getListaDeclaracoesGlobais()) {
            declaracao.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoCadeia noCadeia) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoCaracter noCaracter) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoCaso noCaso) throws ExcecaoVisitaASA {
        NoExpressao expressao = noCaso.getExpressao();
        if (expressao != null) {
            verificaSePodeParar(expressao);
        } else {
            verificaSePodeParar(noCaso);
        }

        if (noCaso.getBlocos() != null) {
            for (NoBloco filho : noCaso.getBlocos()) {
                filho.aceitar(this);
            }
        }
        return null;
    }

    @Override
    public Object visitar(NoChamadaFuncao chamadaFuncao) throws ExcecaoVisitaASA {
        verificaSePodeParar(chamadaFuncao);
        return null;
    }

    @Override
    public Object visitar(NoContinue noContinue) throws ExcecaoVisitaASA {
        verificaSePodeParar(noContinue);
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoFuncao declaracaoFuncao) throws ExcecaoVisitaASA {
        verificaSePodeParar(declaracaoFuncao);

        for (NoBloco filho : declaracaoFuncao.getBlocos()) {
            filho.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoMatriz noDeclaracaoMatriz) throws ExcecaoVisitaASA {
        verificaSePodeParar(noDeclaracaoMatriz);
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoVariavel noDeclaracaoVariavel) throws ExcecaoVisitaASA {
        verificaSePodeParar(noDeclaracaoVariavel);
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoVetor noDeclaracaoVetor) throws ExcecaoVisitaASA {
        noDeclaracaoVetor.definirPontoParada(verificaSePodeParar(noDeclaracaoVetor));
        return null;
    }

    @Override
    public Object visitar(NoEnquanto noEnquanto) throws ExcecaoVisitaASA {
        verificaSePodeParar(noEnquanto.getCondicao());
        for (NoBloco bloco : noEnquanto.getBlocos()) {
            bloco.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoEscolha noEscolha) throws ExcecaoVisitaASA {
        verificaSePodeParar(noEscolha);
        for (NoCaso caso : noEscolha.getCasos()) {
            caso.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoFacaEnquanto noFacaEnquanto) throws ExcecaoVisitaASA {
        verificaSePodeParar(noFacaEnquanto);
        for (NoBloco no : noFacaEnquanto.getBlocos()) {
            no.aceitar(this);
        }

        verificaSePodeParar(noFacaEnquanto.getCondicao());
        return null;
    }

    @Override
    public Object visitar(NoInteiro noInteiro) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoLogico noLogico) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoMatriz noMatriz) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoMenosUnario noMenosUnario) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoNao noNao) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaIgualdade noOperacaoLogicaIgualdade) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaDiferenca noOperacaoLogicaDiferenca) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoAtribuicao noOperacaoAtribuicao) throws ExcecaoVisitaASA {
        verificaSePodeParar(noOperacaoAtribuicao);
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaE noOperacaoLogicaE) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaOU noOperacaoLogicaOU) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaMaior noOperacaoLogicaMaior) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaMaiorIgual noOperacaoLogicaMaiorIgual) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaMenor noOperacaoLogicaMenor) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoLogicaMenorIgual noOperacaoLogicaMenorIgual) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoSoma noOperacaoSoma) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoSubtracao noOperacaoSubtracao) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoDivisao noOperacaoDivisao) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoMultiplicacao noOperacaoMultiplicacao) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoModulo noOperacaoModulo) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoBitwiseLeftShift noOperacaoBitwiseLeftShift) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoBitwiseRightShift noOperacaoBitwiseRightShift) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoBitwiseE noOperacaoBitwiseE) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoBitwiseOu noOperacaoBitwiseOu) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoOperacaoBitwiseXOR noOperacaoBitwiseXOR) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoBitwiseNao noOperacaoBitwiseNao) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoPara noPara) throws ExcecaoVisitaASA {
        NoExpressao condicao = noPara.getCondicao();
        verificaSePodeParar(condicao);
        for (NoBloco no : noPara.getBlocos()) {
            no.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoPare noPare) throws ExcecaoVisitaASA {
        verificaSePodeParar(noPare);
        return null;
    }

    @Override
    public Object visitar(NoReal noReal) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoReferenciaMatriz noReferenciaMatriz) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoReferenciaVariavel noReferenciaVariavel) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoReferenciaVetor noReferenciaVetor) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoRetorne noRetorne) throws ExcecaoVisitaASA {
        NoExpressao noExpressao = noRetorne.getExpressao();
        if (noExpressao != null) {
            noExpressao.aceitar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NoSe noSe) throws ExcecaoVisitaASA {
        verificaSePodeParar(noSe.getCondicao());
        for (NoBloco no : noSe.getBlocosVerdadeiros()) {
            no.aceitar(this);
        }

        if (noSe.getBlocosFalsos() != null) {
            for (NoBloco no : noSe.getBlocosFalsos()) {
                no.aceitar(this);
            }
        }

        return null;
    }

    @Override
    public Object visitar(NoTitulo noTitulo) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoVaPara noVaPara) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoVetor noVetor) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoParametro noDeclaracaoParametro) throws ExcecaoVisitaASA {
        return null;
    }

    @Override
    public Object visitar(NoInclusaoBiblioteca noInclusaoBiblioteca) throws ExcecaoVisitaASA {
        return null;
    }
}
