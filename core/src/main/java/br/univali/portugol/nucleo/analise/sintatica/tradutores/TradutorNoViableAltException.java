package br.univali.portugol.nucleo.analise.sintatica.tradutores;

import br.univali.portugol.nucleo.analise.sintatica.AnalisadorSintatico;
import static br.univali.portugol.nucleo.analise.sintatica.AnalisadorSintatico.eCaracterEspecial;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroCadeiaIncompleta;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroCaracterInvalidoReferencia;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroComandoEsperado;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroExpressaoEsperada;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroExpressaoIncompleta;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroParsingNaoTratado;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroTipoDeDadoEstaFaltando;
import br.univali.portugol.nucleo.mensagens.ErroSintatico;
import java.util.Stack;
import org.antlr.runtime.NoViableAltException;
import static br.univali.portugol.nucleo.analise.sintatica.AnalisadorSintatico.estaNoContexto;
import static br.univali.portugol.nucleo.analise.sintatica.AnalisadorSintatico.estaEmUmComando;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroExpressaoInesperada;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroInteiroForaDoIntervalo;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroParentesis;

/**
 * Tradutor para erros de parsing do tipo {@link NoViableAltException}.
 * 
 * TODO: adicionar nesta documentação a descrição e exemplos de quando este tipo 
 * de erro é gerado pelo ANTLR.
 * 
 * 
 * @author Luiz Fernando Noschang
 * @version 1.0
 * 
 * @see AnalisadorSintatico
 */
public final class TradutorNoViableAltException
{
    /**
     * 
     * @param erro               o erro de parsing gerado pelo ANTLR, sem nenhum tratamento.
     * @param tokens             a lista de tokens envolvidos no erro.
     * @param pilhaContexto      a pilha de contexto do analisador sintático.
     * @param mensagemPadrao     a mensagem de erro padrão para este tipo de erro.
     * @return                   o erro sintático traduzido.
     * @since 1.0
     */
    public ErroSintatico traduzirErroParsing(NoViableAltException erro, String[] tokens, Stack<String> pilhaContexto, String mensagemPadrao, String codigoFonte)
    {
        int linha = erro.line;
        int coluna = erro.charPositionInLine;
        String contextoAtual = pilhaContexto.peek();
        
//        System.out.println("TESTE NUCLEO: " + erro.grammarDecisionDescription);
        if(erro.grammarDecisionDescription.contains("INT-OVERFLOW")){
            return new ErroInteiroForaDoIntervalo(linha, coluna, erro.grammarDecisionDescription.substring(13));
        }
        switch (contextoAtual)
        {
            case "declaracaoTipoDado": return new ErroTipoDeDadoEstaFaltando(linha, coluna);
            case "listaBlocos": return new ErroComandoEsperado(linha, coluna);
            case "expressao7": return traduzirErrosExpressao(linha, coluna, erro, tokens, pilhaContexto, mensagemPadrao, codigoFonte);
            case "expressao5": return traduzirErrosExpressao(linha, coluna, erro, tokens, pilhaContexto, mensagemPadrao, codigoFonte);
            case "referencia": return new ErroCaracterInvalidoReferencia(linha, coluna, erro.token.getText());
        }
        
        return new ErroParsingNaoTratado(erro, mensagemPadrao, contextoAtual);
    }

    private ErroSintatico traduzirErrosExpressao(int linha, int coluna, NoViableAltException erro, String[] tokens, Stack<String> pilhaContexto, String mensagemPadrao, String codigoFonte)
    {
        String alternativa = erro.token.getText();
        
         if (estaNoContexto("vetor", pilhaContexto) || estaNoContexto("matriz", pilhaContexto))
         {
            switch (alternativa)
            {
                case "}":
                case ",": return new ErroExpressaoEsperada(linha, coluna, pilhaContexto);
            }
         }
         else if (estaEmUmComando(pilhaContexto) && !alternativa.equals(")"))
         {
            return new ErroParentesis(linha, coluna, ErroParentesis.Tipo.FECHAMENTO);
         }
         else if(eCaracterEspecial(alternativa) && !alternativa.equals("GAMBIARRA"))
         {
             return new ErroExpressaoInesperada(linha, coluna, alternativa);
         }
        
        return criarErroExpressaoIncompleta(erro, pilhaContexto, linha, coluna, mensagemPadrao);
    }    
    
    private ErroSintatico criarErroExpressaoIncompleta(NoViableAltException erro, Stack<String> pilhaContexto, int linha, int coluna, String mensagemPadrao)
    {
        if (erro.token.getText().equals("<EOF>"))
        {
            return new ErroCadeiaIncompleta(linha, coluna, mensagemPadrao);
        }
        else if (estaEmUmComando(pilhaContexto))
        {
            return new ErroExpressaoEsperada(linha, coluna, pilhaContexto);
        }
        
        return new ErroExpressaoIncompleta(linha, coluna);
    }

    
}