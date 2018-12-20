/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.univali.portugol.nucleo.analise.semantica.avisos;

import br.univali.portugol.nucleo.asa.NoDeclaracao;
import br.univali.portugol.nucleo.asa.NoDeclaracaoParametro;
import br.univali.portugol.nucleo.mensagens.AvisoAnalise;
import br.univali.portugol.nucleo.simbolos.Simbolo;

/**
 *
 * @author Adson Esteves
 */
public class AvisoVetorPodeSerVariavel extends AvisoAnalise{

    private NoDeclaracao declaracao;
    private NoDeclaracaoParametro noDeclaracaoParametro;
    private int tamanho;
    private String codigo = "AvisoSemantico.AvisoVetorPodeSerVariavel";
    
    public AvisoVetorPodeSerVariavel(NoDeclaracao declaracao, int tamanho)
    {
        super(declaracao.getTrechoCodigoFonteNome());
        
        this.declaracao = declaracao;
        this.tamanho = tamanho;
        this.getMensagem();
        super.setCodigo(codigo);
    }

    @Override
    protected String construirMensagem() {
        return "O Vetor "+declaracao.getNome()+" tem tamanho ["+tamanho+"] e pode ser substituido por uma variável";
    }
    
}
