package br.univali.portugol.nucleo.asa;

import java.util.List;

/**
 * Representa uma matriz no código fonte.
 * <p>
 * Este nó da ASA representa uma matriz literal no código fonte. Uma matriz
 * literal é declarada da seguinte forma:
 *
 * <code><pre>
 *
 *    funcao exemploMatrizLiteral()
 *    {
 *        inteiro matriz[][] =
 *        {
 *          { 1, 2, 3 },
 *          { 4, 5, 6 },
 *          { 7, 8, 9 }
 *        }
 *    }
 *
 * </pre></code>
 *
 * @author Luiz Fernando Noschang
 * @version 1.0
 */
public final class NoMatriz extends NoExpressao
{
    private List<List<Object>> valores;

    /**
     *
     * @param valores uma lista de listas contendo os valores da matriz.
     * @since 1.0
     */
    public NoMatriz(List<List<Object>> valores)
    {
        this.valores = valores;
    }

    /**
     * Obtém os valores armazenados nesta matriz. O valor retornado é uma lista
     * contendo outras listas. Cada lista representa uma linha da matriz e cada
     * valor da lista corresponde a uma coluna.
     *
     * @return os valores armazenados na matriz.
     * @since 1.0
     */
    public List<List<Object>> getValores()
    {
        return valores;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object aceitar(VisitanteASA visitante) throws ExcecaoVisitaASA
    {
        return visitante.visitar(this);
    }

    @Override
    public TipoDado getTipoResultante()
    {
        
        if (!valores.isEmpty() && !valores.get(0).isEmpty())
        {
            Object valor = valores.get(0).get(0);
            if(valor instanceof NoReferencia){
                return ((NoReferencia)valor).getOrigemDaReferencia().getTipoDado();
            }
            else if (valor instanceof NoExpressao)
            {
                return ((NoExpressao)valor).getTipoResultante();
            }
        }
        
        return TipoDado.VAZIO;
    }

    public NoMatriz getInicializacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
