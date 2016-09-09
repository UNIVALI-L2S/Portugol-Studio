package br.univali.ps.ui.inspetor;

import br.univali.portugol.nucleo.asa.NoDeclaracao;
import br.univali.portugol.nucleo.asa.NoDeclaracaoMatriz;
import br.univali.portugol.nucleo.asa.NoDeclaracaoParametro;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVariavel;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVetor;
import br.univali.portugol.nucleo.asa.Quantificador;
import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.ps.ui.utils.IconFactory;
import javax.swing.Icon;

/**
 *
 * @author elieser
 */
public abstract class ItemDaLista {

    protected final NoDeclaracao noDeclaracao;
    private long ultimaPintura = 0; //timestamp da ultima atualização
    protected static final long TEMPO_ENTRE_PINTURAS = 200; //no máximo 4 pinturas por segundo
    protected boolean desenhaDestaques = true; //quando o programa está em execução o desenho dos destaques é desativado.
    //Não adiatanda destacar a última variável atualizada com o programa em execução já que o destaque fica "pulando" freneticamente
    //de uma variável para outra

    public ItemDaLista(NoDeclaracao no) {
        this.noDeclaracao = no;
    }

    public boolean podeRepintar() {
        return System.currentTimeMillis() - ultimaPintura >= TEMPO_ENTRE_PINTURAS;
    }

    public void setDesenhaDestaques(boolean statusDosDestaques) {
        desenhaDestaques = statusDosDestaques;
    }

    void resetaTempoDaUltimaAtualizacao() {
        //resta o momento da atualização de maneira que a próxima chamada para o método podeRepintar retorna true
        ultimaPintura = System.currentTimeMillis() - TEMPO_ENTRE_PINTURAS;
    }

    void atualizaMomentoDaUltimaPintura() {
        ultimaPintura = System.currentTimeMillis();
    }

    abstract RenderizadorBase getRendererComponent();

    Icon getIcone() {
        String icone = "unknown";
        TipoDado tipo = getTipo();
        if (tipo != null) {
            if (ehVariavel()) {
                icone = tipo.getNome();
            }
            else if(ehVetor()) { // vetores e matrizes usam o mesmo ícone
                icone = "vetor_" + tipo.getNome();
            }
            else {
                icone = "matriz_" + tipo.getNome();
            }
        }
        return IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, icone + ".png");
    }

    boolean ehVetor() {
        return noDeclaracao instanceof NoDeclaracaoVetor || (noDeclaracao instanceof NoDeclaracaoParametro && ((NoDeclaracaoParametro) noDeclaracao).getQuantificador() == Quantificador.VETOR);
    }

    boolean ehMatriz() {
        return noDeclaracao instanceof NoDeclaracaoMatriz || (noDeclaracao instanceof NoDeclaracaoParametro && ((NoDeclaracaoParametro) noDeclaracao).getQuantificador() == Quantificador.MATRIZ);
    }

    boolean ehVariavel() {
        return noDeclaracao instanceof NoDeclaracaoVariavel || (noDeclaracao instanceof NoDeclaracaoParametro && ((NoDeclaracaoParametro) noDeclaracao).getQuantificador() == Quantificador.VALOR);
    }

    public boolean podeDesenharDestaque() {
        return this == InspetorDeSimbolos.ultimoItemModificado && desenhaDestaques;
    }

    public String getNome() {
        return noDeclaracao.getNome();
    }

    public TipoDado getTipo() {
        return noDeclaracao.getTipoDado();
    }

    public NoDeclaracao getNoDeclaracao() {
        return noDeclaracao;
    }

    public abstract void limpa();

}
