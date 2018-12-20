package br.univali.portugol.nucleo.execucao.erros;

import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.portugol.nucleo.mensagens.ErroExecucao;

/**
 *
 * @author Luiz Fernando Noschang
 */
public class ErroImpossivelConverterTipos extends ErroExecucao
{
    private TipoDado tipoEntrada;
    private TipoDado tipoSaida;
    private String codigo = "ErroExecucao.ErroImpossivelConverterTipos";

    public ErroImpossivelConverterTipos(TipoDado tipoEntrada, TipoDado tipoSaida)
    {
        this.tipoEntrada = tipoEntrada;
        this.tipoSaida = tipoSaida;
        super.setCodigo(codigo);
    }

    @Override
    protected String construirMensagem()
    {
        return String.format("Não foi possível converter o valor de \"%s\" para \"%s\"", tipoEntrada, tipoSaida);
    }    
}
