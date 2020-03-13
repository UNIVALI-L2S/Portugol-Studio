package br.univali.portugol.nucleo.analise.sintatica.erros;

import br.univali.portugol.nucleo.mensagens.ErroSemiSintatico;

/**
 *
 * @author Luiz Fernando Noschang
 */
public final class ErroEscapeUnico extends ErroSemiSintatico
{
    
    
    public ErroEscapeUnico(int linha, int coluna, String codigofonte)
    {
        super(linha, coluna, codigofonte);
    }   

    @Override
    protected String construirMensagem()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Variáveis do tipo cadeias e caracter com o símbolo '\\' devem utiliza-lo como: '\\\\'.\nIsso se deve ao símbolo '\\' ser utilizado em casos como '\\t' e '\\n' onde ele passa por uma reinterpretação do seu significado, se tornando uma tabulação e um pular linha respectivamente");
        
        return sb.toString();
    }
}
