package br.univali.portugol.nucleo.analise.semantica.erros;

import br.univali.portugol.nucleo.asa.TrechoCodigoFonte;
import br.univali.portugol.nucleo.mensagens.ErroSemantico;

/**
 *
 * @author fillipi
 */
public class ErroInicializacaoMatrizEmBranco extends ErroSemantico {

    public ErroInicializacaoMatrizEmBranco(TrechoCodigoFonte trechoCodigoFonte)
    {
        super(trechoCodigoFonte);
    }

    @Override
    protected String construirMensagem()
    {
        return "A inicialização da matriz não possui elementos";
    }
    
}
