package br.univali.portugol.nucleo.analise.sintatica;

import br.univali.portugol.nucleo.StringUtils;
import br.univali.portugol.nucleo.analise.sintatica.antlr4.PortugolBaseVisitor;
import br.univali.portugol.nucleo.analise.sintatica.antlr4.PortugolParser;
import br.univali.portugol.nucleo.asa.*;
import br.univali.portugol.nucleo.analise.sintatica.antlr4.PortugolParser.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GeradorASA {

    private final PortugolParser parser;

    public GeradorASA(PortugolParser parser) {
        this.parser = parser;
    }

    public ASA geraASA() {

        Antlr4ParserVisitor visitor = new Antlr4ParserVisitor();

        visitor.visitArquivo(parser.arquivo()); // invoca a primeira regra da gramática

        return visitor.getAsa();
    }

    private class Antlr4ParserVisitor extends PortugolBaseVisitor<No> {

        private final ASAPrograma asa = new ASAPrograma();

        public ASAPrograma getAsa() {
            return asa;
        }

        @Override
        public No visitArquivo(ArquivoContext ctx) {
            
            List<NoInclusaoBiblioteca> inclusoes = new ArrayList<>();
            for (InclusaoBibliotecaContext inclusaoBibliotecaContext : ctx.inclusaoBiblioteca()) {
                inclusoes.add((NoInclusaoBiblioteca)inclusaoBibliotecaContext.accept(this));
            }
            asa.setListaInclusoesBibliotecas(inclusoes);   
           
            List<ParserRuleContext> declaracoes = ctx.getRuleContexts(ParserRuleContext.class);
            for (ParserRuleContext contextoDeclaracao : declaracoes) {
                NoDeclaracao noDeclaracao = (NoDeclaracao)contextoDeclaracao.accept(this);
                if (!(noDeclaracao instanceof NoListaDeclaracoes)) {
                    asa.adicionaDeclaracaoGlobal(noDeclaracao);
                }
                else { // trata o caso de listas de declaração de variáveis ou arrays globais
                    for (Object declaracao : ((NoListaDeclaracoes)noDeclaracao).getDeclaracoes()) {
                        asa.adicionaDeclaracaoGlobal((NoDeclaracao)declaracao);
                    }
                }
            }
            
            return null;
        }

        @Override
        public No visitListaDeclaracoes(ListaDeclaracoesContext ctx) {
            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(ctx.TIPO().getText());
            boolean constante = ctx.CONSTANTE() != null;
            
            NoListaDeclaracoes lista = new NoListaDeclaracoes(tipo, constante);
            
            for (DeclaracaoContext declaracaoContext : ctx.declaracao()) {
                NoDeclaracao noDeclaracao = (NoDeclaracao)declaracaoContext.accept(this);
               lista.adicionaDeclaracao(noDeclaracao);
            }
            
            return lista;
        }
        
//        @Override 
//        public No visitDeclaracaoListaMatriz(DeclaracaoListaMatrizContext ctx) {
//            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(ctx.declaracaoMatriz().TIPO().getText());
//            boolean constante = ctx.declaracaoMatriz().CONSTANTE() != null;
//            
//            NoListaDeclaracaoMatriz no = new NoListaDeclaracaoMatriz(tipo, constante);
//            
//            // trata a primeira matriz da lista
//            no.adicionaDeclaracao((NoDeclaracaoMatriz)ctx.declaracaoMatriz().accept(this));
//            
//            int totalMatrizes = ctx.definicaoMatriz().size(); // trata as outras matrizes da lista
//            for (int i = 0; i < totalMatrizes; i++) {
//                DefinicaoMatrizContext definicao = ctx.definicaoMatriz(i);
//                String nomeMatriz = definicao.ID().getText();
//                
//                NoExpressao linhas = (definicao.linhaMatriz() != null) ? (NoExpressao)definicao.linhaMatriz().accept(this) : null;
//                NoExpressao colunas = (definicao.colunaMatriz() != null) ? (NoExpressao)definicao.colunaMatriz().accept(this) : null;
//                
//                NoDeclaracaoMatriz noMatriz = new NoDeclaracaoMatriz(nomeMatriz, tipo, linhas, colunas, constante);
//                
//                InicializacaoMatrizContext inicializacao = definicao.inicializacaoMatriz();
//                if (inicializacao != null) { // a matriz tem inicialização?
//                    noMatriz.setInicializacao((NoExpressao)inicializacao.accept(this));
//                }
//                
//                no.adicionaDeclaracao(noMatriz);
//            }
//
//            return no;
//        }
        

//        @Override
//        public No visitDeclaracaoListaArray(DeclaracaoListaArrayContext ctx) {
//            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(ctx.declaracaoArray().TIPO().getText());
//            boolean constante = ctx.declaracaoArray().CONSTANTE() != null;
//            
//            NoListaDeclaracaoVetores no = new NoListaDeclaracaoVetores(tipo, constante);
//            
//            // trata o primeiro vetor da lista
//            no.adicionaDeclaracao((NoDeclaracaoVetor)ctx.declaracaoArray().accept(this));
//            
//            int totalVetores = ctx.definicaoArray().size(); // trata os outros arrays da lista
//            for (int i = 0; i < totalVetores; i++) {
//                
//                DefinicaoArrayContext definicao = ctx.definicaoArray(i);
//                String nomeVetor = definicao.ID().getText();
//                
//                NoExpressao tamanho = (definicao.tamanhoArray() != null) ? (NoExpressao)definicao.tamanhoArray().accept(this) : null;
//                
//                NoDeclaracaoVetor noVetor = new NoDeclaracaoVetor(nomeVetor, tipo, tamanho, constante);
//                
//                InicializacaoArrayContext inicializacao = definicao.inicializacaoArray();
//                if (inicializacao != null) { // o vetor tem inicialização?
//                    noVetor.setInicializacao((NoExpressao)inicializacao.accept(this));
//                }
//                
//                no.adicionaDeclaracao(noVetor);
//            }
//
//            return no;
//        }
        

//        @Override
//        public No visitDeclaracaoListaVariaveis(DeclaracaoListaVariaveisContext ctx) {
//            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(ctx.declaracaoVariavel().TIPO().getText());
//            boolean constante = ctx.declaracaoVariavel().CONSTANTE() != null;
//            
//            NoListaDeclaracaoVariaveis no = new NoListaDeclaracaoVariaveis(tipo, constante);
//            
//            // trata a primeira variável da lista
//            no.adicionaDeclaracao((NoDeclaracaoVariavel)ctx.declaracaoVariavel().accept(this));
//            
//            int totalVariaveis = ctx.itemListaVariaveis().size(); // trata as outras variáveis da lista
//            for (int i = 0; i < totalVariaveis; i++) {
//                String nomeVariavel = ctx.itemListaVariaveis(i).ID().getText();
//                
//                NoDeclaracaoVariavel noVariavel = new NoDeclaracaoVariavel(nomeVariavel, tipo, constante);
//                
//                ExpressaoContext inicializacao = ctx.itemListaVariaveis(i).expressao();
//                if (inicializacao != null) { // a variável tem inicialização?
//                    noVariavel.setInicializacao((NoExpressao)inicializacao.accept(this));
//                }
//                
//                no.adicionaDeclaracao(noVariavel);
//            }
//
//            return no;
//        }
        
        @Override
        public No visitDeclaracaoFuncao(PortugolParser.DeclaracaoFuncaoContext ctx) {

            String nomeFuncao = ctx.ID().getText();
            String nomeTipoRetorno = ctx.TIPO() != null ? ctx.TIPO().getText() : "vazio";
            TipoDado tipoRetorno = TipoDado.obterTipoDadoPeloNome(nomeTipoRetorno);

            NoDeclaracaoFuncao declaracaoFuncao = new NoDeclaracaoFuncao(nomeFuncao, tipoRetorno, Quantificador.VALOR);

            if (ctx.listaParametros() != null) { // se a função tem parâmetros
                List<NoDeclaracaoParametro> parametros = new ArrayList<>();
                for (ParametroContext parametroContext : ctx.listaParametros().parametro()) {
                    parametros.add((NoDeclaracaoParametro)parametroContext.accept(this));
                }
                declaracaoFuncao.setParametros(parametros);
            }
                        
            declaracaoFuncao.setBlocos(getBlocos(ctx.comando()));

            declaracaoFuncao.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            declaracaoFuncao.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.FUNCAO(), ctx.getText().length()));
            
            if (ctx.TIPO() != null) {
                declaracaoFuncao.setTrechoCodigoFonteTipoDado(getTrechoCodigoFonte(ctx.TIPO()));
            }

            return declaracaoFuncao;
        }

        @Override
        public No visitRetorne(RetorneContext ctx) {
            TrechoCodigoFonte trechoCodigoFonte = getTrechoCodigoFonte(ctx.RETORNE());
            
            NoExpressao expressao = null;
            if (ctx.expressao() != null) {
                expressao = (NoExpressao)ctx.expressao().accept(this);
            }
            
            return new NoRetorne(trechoCodigoFonte, expressao);
        }

        @Override
        public No visitNegacaoBitwise(NegacaoBitwiseContext ctx) 
        {
            NoBitwiseNao no = new NoBitwiseNao((NoExpressao)ctx.expressao().accept(this));
            no.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.OP_NOT_BITWISE(), ctx.getText().length()));
            
            return no;
        }

        @Override
        public No visitNegacao(NegacaoContext ctx) {
            NoNao noNao = new NoNao((NoExpressao)ctx.expressao().accept(this));
            
            noNao.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.OP_NAO(), ctx.getText().length()));
            
            return noNao;
        }
        
        
        private NoReferenciaVariavel criaNoReferenciaVariavel(TerminalNode id, String escopo, int trechoCodigoFonteLenght) {
            String nome = id.getText();
            NoReferenciaVariavel no = new NoReferenciaVariavel(escopo, nome);
            
            no.setTrechoCodigoFonteNome(getTrechoCodigoFonte(id));
            no.setTrechoCodigoFonte(getTrechoCodigoFonte(id, trechoCodigoFonteLenght));
            
            return no;
        }
        
        @Override
        public No visitReferenciaParaVariavel(PortugolParser.ReferenciaParaVariavelContext ctx) {
   
            String escopo = null;
            PortugolParser.EscopoBibliotecaContext escopoBiblioteca = ctx.escopoBiblioteca();
            if (escopoBiblioteca != null) {
                escopo = escopoBiblioteca.ID().getText();
            }

            return criaNoReferenciaVariavel(ctx.ID(), escopo, ctx.getText().length());
        }

        @Override
        public No visitPare(PareContext ctx) {
            NoPare noPare = new NoPare();
            noPare.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.PARE()));
            return noPare;
        }

        @Override
        public No visitCaso(CasoContext casoContext) {
            NoExpressao expressao = casoContext.CONTRARIO() == null ? (NoExpressao) casoContext.expressao().accept(this) : null;
            NoCaso caso = new NoCaso(expressao);
            caso.setBlocos(getBlocos(casoContext.comando()));
            caso.setTrechoCodigoFonte(getTrechoCodigoFonte(casoContext.CASO(), casoContext.getText().length()));
            return caso;
        }
        
        @Override
        public No visitEscolha(EscolhaContext ctx) {
            NoEscolha noEscolha = new NoEscolha((NoExpressao)ctx.expressao().accept(this));
            
            List<NoCaso> casos = new ArrayList<>();
            for (CasoContext casoContext : ctx.caso()) {
                casos.add((NoCaso)casoContext.accept(this));
            }
             
            noEscolha.setCasos(casos);
            
            noEscolha.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ESCOLHA(), ctx.getText().length()));
            
            return noEscolha;
        }

        
         /***
          *     ATENçÃO: No momento as atribuições compostas estão sendo tratadas
          *     como atribuições simples para evitar mudanças maiores no código
          */
        @Override
        public No visitAtribuicaoCompostaSoma(AtribuicaoCompostaSomaContext ctx) {
            
            NoExpressao opEsquerdo = (NoExpressao)ctx.expressao(0).accept(this);
            NoExpressao opDireito = (NoExpressao)ctx.expressao(1).accept(this);

            return new NoOperacaoAtribuicao(opEsquerdo, new NoOperacaoSoma(opEsquerdo, opDireito));
        }

        @Override
        public No visitAtribuicaoCompostaSubtracao(AtribuicaoCompostaSubtracaoContext ctx) {
            NoExpressao opEsquerdo = (NoExpressao)ctx.expressao(0).accept(this);
            NoExpressao opDireito = (NoExpressao)ctx.expressao(1).accept(this);

            return new NoOperacaoAtribuicao(opEsquerdo, new NoOperacaoSubtracao(opEsquerdo, opDireito));
        }

        @Override
        public No visitAtribuicaoCompostaDivisao(AtribuicaoCompostaDivisaoContext ctx) {
            NoExpressao opEsquerdo = (NoExpressao)ctx.expressao(0).accept(this);
            NoExpressao opDireito = (NoExpressao)ctx.expressao(1).accept(this);

            return new NoOperacaoAtribuicao(opEsquerdo, new NoOperacaoDivisao(opEsquerdo, opDireito));
        }

        @Override
        public No visitAtribuicaoCompostaMultiplicacao(AtribuicaoCompostaMultiplicacaoContext ctx) {
            NoExpressao opEsquerdo = (NoExpressao)ctx.expressao(0).accept(this);
            NoExpressao opDireito = (NoExpressao)ctx.expressao(1).accept(this);

            return new NoOperacaoAtribuicao(opEsquerdo, new NoOperacaoMultiplicacao(opEsquerdo, opDireito));
        }

        private List<NoBloco> getBlocos(List<ComandoContext> ctx) {
            List<NoBloco> blocos = new ArrayList<>();
            for (ComandoContext comando : ctx) {
                NoBloco bloco = (NoBloco)comando.accept(this);
                
                if (!(bloco instanceof NoListaDeclaracoes)) {
                    blocos.add(bloco);
                }
                else { // trata a lista de declarações (variáveis ou arrays) como um 'amontoado' de declarações
                    List<NoDeclaracao> declaracoes = ((NoListaDeclaracoes)bloco).getDeclaracoes();
                    for (NoDeclaracao declaracao : declaracoes) {
                        blocos.add((NoBloco)declaracao);
                    }
                }
            }
            return blocos;
        }
        
        private List<NoBloco> getBlocos(ListaComandosContext ctx) {
            return getBlocos(ctx.comando());
        }
        
        private NoOperacaoAtribuicao criaIncrementoUnario(TerminalNode ID, IndiceArrayContext contextoExpressao) {
            String nomeVetor = ID.getText();
            NoExpressao expressao = (NoExpressao)contextoExpressao.expressao().accept(this);
            NoReferenciaVetor referenciaVetor = new NoReferenciaVetor(null, nomeVetor, expressao);
            
            referenciaVetor.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ID));
            
            return new NoOperacaoAtribuicao(referenciaVetor, new NoOperacaoSoma(referenciaVetor, new NoInteiro(1)));
        }
        
        private NoOperacaoAtribuicao criaIncrementoUnario(TerminalNode id, List<IndiceArrayContext> indiceArrayCtx, int ctxLenght) {
             if (indiceArrayCtx.size() == 1) { // é um vetor?
                return criaIncrementoUnario(id, indiceArrayCtx.get(0));
            }
            else if (indiceArrayCtx.size() == 2) { // é uma matriz?
                return criaIncrementoUnario(id, indiceArrayCtx.get(0), indiceArrayCtx.get(1));
            }
            
            // é uma variável
            NoReferenciaVariavel referenciaVariavel = criaNoReferenciaVariavel(id, null, ctxLenght);
            NoOperacaoAtribuicao atribuicao = new NoOperacaoAtribuicao(referenciaVariavel, new NoOperacaoSoma(referenciaVariavel, new NoInteiro(1)));
            atribuicao.setTrechoCodigoFonte(new TrechoCodigoFonte(referenciaVariavel.getTrechoCodigoFonte(), ctxLenght));
            
            return atribuicao;
        }
        
        private NoOperacaoAtribuicao criaIncrementoUnario(TerminalNode ID, IndiceArrayContext contextoLinhas, IndiceArrayContext contextoColunas) {
            String nomeMatriz = ID.getText();
            NoExpressao linhas = (NoExpressao)contextoLinhas.expressao().accept(this);
            NoExpressao colunas = (NoExpressao)contextoColunas.expressao().accept(this);
            NoReferenciaMatriz referenciaMatriz = new NoReferenciaMatriz(null, nomeMatriz, linhas, colunas);
            referenciaMatriz.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ID));
            return new NoOperacaoAtribuicao(referenciaMatriz, new NoOperacaoSoma(referenciaMatriz, new NoInteiro(1)));
        }
        
        private NoOperacaoAtribuicao criaDecrementoUnario(TerminalNode ID, IndiceArrayContext contextoExpressao) {
            NoExpressao expressao = (NoExpressao)contextoExpressao.expressao().accept(this);
            String nomeVetor = ID.getText();
            NoReferenciaVetor referenciaVetor = new NoReferenciaVetor(null, nomeVetor, expressao);
            referenciaVetor.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ID));
            return new NoOperacaoAtribuicao(referenciaVetor, new NoOperacaoSubtracao(referenciaVetor, new NoInteiro(1)));
        }
        
        private NoOperacaoAtribuicao criaDecrementoUnario(TerminalNode ID, IndiceArrayContext contextoLinhas, IndiceArrayContext contextoColunas) {
            String nomeMatriz = ID.getText();
            NoExpressao linhas = (NoExpressao)contextoLinhas.expressao().accept(this);
            NoExpressao colunas = (NoExpressao)contextoColunas.expressao().accept(this);
            NoReferenciaMatriz referenciaMatriz = new NoReferenciaMatriz(null, nomeMatriz, linhas, colunas);
            referenciaMatriz.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ID));
            return new NoOperacaoAtribuicao(referenciaMatriz, new NoOperacaoSubtracao(referenciaMatriz, new NoInteiro(1)));
        }
        
        private NoOperacaoAtribuicao criaDecrementoUnario(TerminalNode id, List<IndiceArrayContext> indiceArrayCtx, int ctxLenght) {
             if (indiceArrayCtx.size() == 1) { // é um vetor?
                return criaIncrementoUnario(id, indiceArrayCtx.get(0));
            }
            else if (indiceArrayCtx.size() == 2) { // é uma matriz?
                return criaIncrementoUnario(id, indiceArrayCtx.get(0), indiceArrayCtx.get(1));
            }
            
            // é uma variável
            NoReferenciaVariavel referenciaVariavel = criaNoReferenciaVariavel(id, null, ctxLenght);
            NoOperacaoAtribuicao atribuicao = new NoOperacaoAtribuicao(referenciaVariavel, new NoOperacaoSubtracao(referenciaVariavel, new NoInteiro(1)));
            atribuicao.setTrechoCodigoFonte(new TrechoCodigoFonte(referenciaVariavel.getTrechoCodigoFonte(), ctxLenght));
            
            return atribuicao;
        }
        
        
        @Override
        public No visitIncrementoUnarioPosfixado(IncrementoUnarioPosfixadoContext ctx) {
            return criaIncrementoUnario(ctx.ID(), ctx.indiceArray(), ctx.getText().length());
        }

        @Override
        public No visitIncrementoUnarioPrefixado(IncrementoUnarioPrefixadoContext ctx) {
            return criaIncrementoUnario(ctx.ID(), ctx.indiceArray(), ctx.getText().length());
        }

        @Override
        public No visitDecrementoUnarioPosfixado(DecrementoUnarioPosfixadoContext ctx) {
            if (ctx.indiceArray().size() == 1) { // é um vetor?
                return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(0));
            }
            else if (ctx.indiceArray().size() == 2) { // é uma matriz?
                return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(0), ctx.indiceArray(1));
            }
            
            return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(), ctx.getText().length());
        }

        @Override
        public No visitDecrementoUnarioPrefixado(DecrementoUnarioPrefixadoContext ctx) {
            if (ctx.indiceArray().size() == 1) { // é um vetor?
                return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(0));
            }
            else if (ctx.indiceArray().size() == 2) { // é uma matriz?
                return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(0), ctx.indiceArray(1));
            }
            
            return criaDecrementoUnario(ctx.ID(), ctx.indiceArray(), ctx.getText().length());
        }
        
        @Override
        public No visitAtribuicao(PortugolParser.AtribuicaoContext ctx) {
            
            NoOperacaoAtribuicao atribuicao = GeradorNoOperacao.gera(ctx, this, NoOperacaoAtribuicao.class);
            
            NoExpressao esquerda = (NoExpressao)ctx.expressao(0).accept(this);
            atribuicao.setTrechoCodigoFonte(new TrechoCodigoFonte(esquerda.getTrechoCodigoFonte(), ctx.getText().length()));
            
            return atribuicao;
        }

        @Override
        public No visitDeclaracaoVariavel(PortugolParser.DeclaracaoVariavelContext ctx) {
            ListaDeclaracoesContext listaDeclaracoes = (ListaDeclaracoesContext)ctx.getParent().getParent();
            TipoDado tipoVariavel = TipoDado.obterTipoDadoPeloNome(listaDeclaracoes.TIPO().getText());
            String nomeVariavel = ctx.ID().getText();
            boolean constante = listaDeclaracoes.CONSTANTE() != null;
            NoDeclaracaoVariavel noDeclaracaoVariavel = new NoDeclaracaoVariavel(nomeVariavel, tipoVariavel, constante);

            // a declaração da variável tem uma inicialização?
            if (ctx.expressao() != null) {
                NoExpressao inicializacao = (NoExpressao) ctx.expressao().accept(this);
                noDeclaracaoVariavel.setInicializacao(inicializacao);
            }
            
            noDeclaracaoVariavel.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            noDeclaracaoVariavel.setTrechoCodigoFonteTipoDado(getTrechoCodigoFonte(listaDeclaracoes.TIPO()));
            
            noDeclaracaoVariavel.setTrechoCodigoFonte(getTrechoCodigoFonte(constante ? listaDeclaracoes.CONSTANTE() : listaDeclaracoes.TIPO(), ctx.getText().length()));
            
            return noDeclaracaoVariavel;
        }

        @Override
        public No visitInclusaoBiblioteca(InclusaoBibliotecaContext ctx) {
            String nome = ctx.ID(0).getText();
            
            String alias = null;
            if (ctx.ID(1) != null) {
                alias = ctx.ID(1).getText();
            }
            
            NoInclusaoBiblioteca no = new NoInclusaoBiblioteca(nome, alias);
            
            no.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID(0)));
            no.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ID(0), ctx.getText().length()));
            
            if (no.getAlias() != null) {
                no.setTrechoCodigoFonteAlias(getTrechoCodigoFonte(ctx.ID(1)));
            }
            
            return no;
        }
        
        @Override
        public No visitTamanhoArray(TamanhoArrayContext ctx) {            
            return (NoExpressao) ctx.expressao().accept(this);
        }

        @Override
        public No visitDeclaracaoMatriz(DeclaracaoMatrizContext ctx) {
            ListaDeclaracoesContext listaDeclaracoes = (ListaDeclaracoesContext)ctx.getParent().getParent();
            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(listaDeclaracoes.TIPO().getText());
            String nome = ctx.ID().getText();
            
            NoExpressao expressaoLinhas = null;
            if (ctx.linhaMatriz() != null) {
                 expressaoLinhas = (NoExpressao) ctx.linhaMatriz().accept(this);
            }
            
            NoExpressao expressaoColunas = null;
            if (ctx.colunaMatriz() != null) {
                 expressaoColunas = (NoExpressao) ctx.colunaMatriz().accept(this);
            }
            
            boolean constante = listaDeclaracoes.CONSTANTE() != null;
            
            NoDeclaracaoMatriz matriz = new NoDeclaracaoMatriz(nome, tipo, expressaoLinhas, expressaoColunas, constante);
            
            InicializacaoMatrizContext inicializacao = ctx.inicializacaoMatriz();
            if (inicializacao != null) {
                matriz.setInicializacao((NoMatriz)inicializacao.accept(this));
            }
            
            matriz.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            matriz.setTrechoCodigoFonteTipoDado(getTrechoCodigoFonte(listaDeclaracoes.TIPO()));
            
            // TODO TRATAR as constates
            matriz.setTrechoCodigoFonte(getTrechoCodigoFonte(listaDeclaracoes.TIPO(), ctx.getText().length()));
            
            return matriz; 
        }

        @Override
        public No visitInicializacaoMatriz(InicializacaoMatrizContext ctx) {
            List<List<Object>> linhas = new ArrayList<>();
            
            for (InicializacaoArrayContext inicializacaoArrayContext : ctx.inicializacaoArray()) {
                List<Object> linha = new ArrayList<>();

                if (inicializacaoArrayContext.listaExpressoes() != null) { // se as linhas da matriz não foram inicializadas com listas vazias: inteiro m[][] = {{}, {}}
                    for (ExpressaoContext expressao : inicializacaoArrayContext.listaExpressoes().expressao()) {
                        linha.add(expressao.accept(this));
                    }
                }

                linhas.add(linha);
            }
            
            NoMatriz matriz = new NoMatriz(linhas);
            
            matriz.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ABRE_CHAVES(), ctx.getText().length()));
            
            return matriz;
        }
        
        @Override
        public No visitInicializacaoArray(InicializacaoArrayContext ctx) {
            
            List<Object> valores = new ArrayList<>();
            if (ctx.listaExpressoes() != null) { // quando o vetor é inicializado com uma lista vazia: inteiro v[] = {}
                for (ExpressaoContext expressao : ctx.listaExpressoes().expressao()) {
                    valores.add(expressao.accept(this));
                }
            }
            NoVetor noVetor = new NoVetor(valores);

            noVetor.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ABRE_CHAVES(), ctx.getText().length()));
            
            return noVetor;
        }
        
        @Override
        public No visitDeclaracaoArray(DeclaracaoArrayContext ctx) {
            ListaDeclaracoesContext listaDeclaracoes = (ListaDeclaracoesContext)ctx.getParent().getParent();
            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(listaDeclaracoes.TIPO().getText());
            String nome = ctx.ID().getText();
            
            NoExpressao tamanho = null;
            if (ctx.tamanhoArray() != null) {
                tamanho = (NoExpressao) visitTamanhoArray(ctx.tamanhoArray());
            }
            
            
            boolean constante = listaDeclaracoes.CONSTANTE() != null;
            
            NoDeclaracaoVetor vetor = new NoDeclaracaoVetor(nome, tipo, tamanho, constante);
            
            InicializacaoArrayContext inicializacao = ctx.inicializacaoArray();
            if (inicializacao != null) {
                vetor.setInicializacao((NoVetor)inicializacao.accept(this));
            }
            
            vetor.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            vetor.setTrechoCodigoFonteTipoDado(getTrechoCodigoFonte(listaDeclaracoes.TIPO()));
            
            // TODO TRATAR as constates
            vetor.setTrechoCodigoFonte(getTrechoCodigoFonte(listaDeclaracoes.TIPO(), ctx.getText().length()));
            
            return vetor; 
        }
        
        // Loop para (for)
        @Override
        public No visitPara(PortugolParser.ParaContext contexto) {
            PortugolParser.InicializacaoParaContext inicializacaoPara = contexto.inicializacaoPara();
            PortugolParser.CondicaoContext condicao = contexto.condicao();
            PortugolParser.IncrementoParaContext incrementoPara = contexto.incrementoPara();
            
            NoPara noPara = new NoPara();

            if (inicializacaoPara != null) {
                List<NoBloco> inicializacoes = new ArrayList<>();
                if (inicializacaoPara.ID() == null) { // se NÃO foi usada uma referência para variável na inicialização do para
                    NoBloco blocoInicializacao = (NoBloco)inicializacaoPara.accept(this);
                    if (blocoInicializacao instanceof NoListaDeclaracoes) {
                        List<NoDeclaracao> declaracoes = ((NoListaDeclaracoes)blocoInicializacao).getDeclaracoes();
                        for (NoDeclaracao declaracao : declaracoes) {
                            inicializacoes.add((NoBloco)declaracao);
                        }
                    }
                    else {
                        inicializacoes.add(blocoInicializacao); // declaração única de variável ou atribuição
                    }
                }
                else { // o para foi inicializado apenas com uma referência para variável, como 'para(j; j< 10; j++) ... "
                    inicializacoes.add(new NoReferenciaVariavel(null, inicializacaoPara.ID().getText()));
                }
                noPara.setInicializacoes(inicializacoes);
            }
            
            if (condicao != null) {
                noPara.setCondicao((NoExpressao) condicao.accept(this));
            }

            if (incrementoPara != null) {
                noPara.setIncremento((NoExpressao) incrementoPara.accept(this));
            }

            // percorre os comandos filhos do loop
            noPara.setBlocos(getBlocos(contexto.listaComandos()));

            noPara.setTrechoCodigoFonte(getTrechoCodigoFonte(contexto.PARA(), contexto.getText().length()));
            
            return noPara;
        }

        @Override
        public No visitFacaEnquanto(FacaEnquantoContext ctx) {
            NoFacaEnquanto facaEnquanto = new NoFacaEnquanto((NoExpressao)ctx.expressao().accept(this));
            facaEnquanto.setBlocos(getBlocos(ctx.listaComandos()));
            facaEnquanto.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.FACA(), ctx.getText().length()));
            return facaEnquanto;
        }

        @Override
        public No visitEnquanto(EnquantoContext ctx) {
            NoEnquanto enquanto = new NoEnquanto((NoExpressao)ctx.expressao().accept(this));
            enquanto.setBlocos(getBlocos(ctx.listaComandos()));
            enquanto.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ENQUANTO(), ctx.getText().length()));
            return enquanto;
        }
        
        @Override
        public No visitSe(SeContext ctx) {
            NoSe se = new NoSe((NoExpressao)ctx.expressao().accept(this));
            
            se.setBlocosVerdadeiros(getBlocos(ctx.listaComandos(0)));
            
            if (ctx.SENAO() != null) {
                se.setBlocosFalsos(getBlocos(ctx.listaComandos(1)));
            }
            
            se.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.SE(), ctx.getText().length()));
            
            return se;
        }
        
        @Override
        public No visitChamadaFuncao(PortugolParser.ChamadaFuncaoContext ctx) {

            PortugolParser.EscopoBibliotecaContext escopoBiblioteca = ctx.escopoBiblioteca();

            String escopo = (escopoBiblioteca != null) ? escopoBiblioteca.ID().getText() : null;
            String nomeFuncao = ctx.ID().getText();

            NoChamadaFuncao noChamadaFuncao = new NoChamadaFuncao(escopo, nomeFuncao);
            
            if (ctx.listaExpressoes() != null) { // se existem parâmetros sendo passados para a função
                List<NoExpressao> parametros = new ArrayList<>();
                for (ParserRuleContext expressaoContext : ctx.listaExpressoes().getRuleContexts(ParserRuleContext.class)) {
                    parametros.add((NoExpressao)expressaoContext.accept(this));
                }
                noChamadaFuncao.setParametros(parametros);
            }
            
            noChamadaFuncao.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            noChamadaFuncao.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ID(), ctx.getText().length()));
            
            return noChamadaFuncao;
        }

        @Override
        public No visitParametro(ParametroContext ctx) {
            
            String nome = ctx.ID().getText();
            TipoDado tipo = TipoDado.obterTipoDadoPeloNome(ctx.TIPO().getText());
            
            Quantificador quantificador = Quantificador.VALOR;
            if (ctx.parametroArray() != null) {
                quantificador = Quantificador.VETOR;
            }
            else if (ctx.parametroMatriz() != null) {
                quantificador = Quantificador.MATRIZ;
            }
            
            ModoAcesso modoAcesso = ctx.E_COMERCIAL()!= null ? ModoAcesso.POR_REFERENCIA : ModoAcesso.POR_VALOR;
            
            NoDeclaracaoParametro parametro = new NoDeclaracaoParametro(nome, tipo, quantificador, modoAcesso);
            
            parametro.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            parametro.setTrechoCodigoFonteTipoDado(getTrechoCodigoFonte(ctx.TIPO()));
            parametro.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.TIPO(), ctx.getText().length()));
            
            return parametro; 
        }
        
        @Override
        public No visitReferenciaArray(ReferenciaArrayContext ctx) {
            String escopo = null;
            if (ctx.escopoBiblioteca() != null) {
                escopo = ctx.escopoBiblioteca().getText();
            }
            
            String nomeArray = ctx.ID().getText();

            NoReferenciaVetor noReferenciaVetor = new NoReferenciaVetor(escopo, nomeArray, (NoExpressao)ctx.indiceArray().expressao().accept(this));
            
            noReferenciaVetor.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            noReferenciaVetor.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ID(), ctx.getText().length()));
            
            return noReferenciaVetor;
        }
        
        private TrechoCodigoFonte getTrechoCodigoFonte(TerminalNode node, int tamanho) {
            Token symbol = node.getSymbol();
            int linha = symbol.getLine();
            int coluna = symbol.getCharPositionInLine();
            return new TrechoCodigoFonte(linha, coluna, tamanho);
        }
        
        private TrechoCodigoFonte getTrechoCodigoFonte(TerminalNode node) {
            int tamanho = node.getSymbol().getText().length();
            return getTrechoCodigoFonte(node, tamanho);
        }
        
         @Override
        public No visitReferenciaMatriz(ReferenciaMatrizContext ctx) {
            String escopo = null;
            if (ctx.escopoBiblioteca() != null) {
                escopo = ctx.escopoBiblioteca().getText();
            }
            
            String nomeMatriz = ctx.ID().getText();

            NoExpressao expressaoLinha = (NoExpressao)ctx.indiceArray(0).expressao().accept(this);
            NoExpressao expressaoColuna = (NoExpressao)ctx.indiceArray(1).expressao().accept(this);
            
            NoReferenciaMatriz matriz = new NoReferenciaMatriz(escopo, nomeMatriz, expressaoLinha, expressaoColuna);
            
            matriz.setTrechoCodigoFonteNome(getTrechoCodigoFonte(ctx.ID()));
            matriz.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ID(), ctx.getText().length()));
            
            return matriz;
        }

        private int parseInt(String texto, int base) {
            
            if (base != 10 && base != 16) {
                throw new IllegalArgumentException("não é possível converter inteiros na base " + base);
            }
            
            if (base == 16) {
                return Integer.parseInt(texto.replaceFirst("0[xX]", ""), base); // remove o 0x da frente do número hexa
            }
            
            return Integer.parseInt(texto, base);
        }
        
        @Override
        public No visitNumeroInteiro(PortugolParser.NumeroInteiroContext ctx) {
            
            TerminalNode terminal = ctx.INT() != null ? ctx.INT() : ctx.HEXADECIMAL();
            int base = terminal == ctx.INT() ? 10 : 16; // decimal ou hexa
                    
            int valorInteiro = parseInt(terminal.getText(), base);

            NoInteiro noInteiro = new NoInteiro(valorInteiro);
            noInteiro.setTrechoCodigoFonte(getTrechoCodigoFonte(terminal));
            return noInteiro;
        }

        @Override
        public No visitNumeroReal(PortugolParser.NumeroRealContext ctx) {
            double valorDouble = Double.parseDouble(ctx.REAL().getText());
            NoReal noReal = new NoReal(valorDouble);
            noReal.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.REAL()));
            return noReal;
        }

        @Override
        public No visitExpressaoEntreParenteses(ExpressaoEntreParentesesContext ctx) {
            NoExpressao expressao = (NoExpressao)ctx.expressao().accept(this);
            expressao.setEstaEntreParenteses(true);
            expressao.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.ABRE_PARENTESES(), ctx.getText().length()));
            return expressao;
        }

        @Override
        public No visitMaisUnario(MaisUnarioContext ctx) {
            // ignora o operador de '+' e usa apenas a expressão
            return ctx.expressao().accept(this);
        }

        @Override
        public No visitMenosUnario(MenosUnarioContext ctx) {
            NoMenosUnario noMenosUnario = new NoMenosUnario((NoExpressao)ctx.expressao().accept(this));
            noMenosUnario.setTrechoCodigoFonteMenos(getTrechoCodigoFonte(ctx.OP_SUBTRACAO()));
            noMenosUnario.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.OP_SUBTRACAO(), ctx.getText().length()));
            return noMenosUnario;
        }
        
        @Override
        public No visitString(StringContext ctx) {
            String texto = ctx.STRING().getText();
            String substring = StringUtils.removerAspasCircundantes(texto);
            NoCadeia noCadeia = new NoCadeia(StringUtils.traduzirSequenciasEscape(substring)); //ignora as aspas que circundam a string
            noCadeia.setValorOriginal(texto);
            noCadeia.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.STRING()));
            return noCadeia;
        }

        @Override
        public No visitValorLogico(ValorLogicoContext ctx) {
            boolean valor = ctx.LOGICO().getText().equals("verdadeiro");
            NoLogico noLogico = new NoLogico(valor);
            noLogico.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.LOGICO()));
            return noLogico;
        }

        @Override
        public No visitCaracter(CaracterContext ctx) {
            NoCaracter noCaracter = new NoCaracter(ctx.CARACTER().getText().charAt(1));
            noCaracter.setTrechoCodigoFonte(getTrechoCodigoFonte(ctx.CARACTER()));
            return noCaracter;
        }
        
        private <TipoContexto extends ParserRuleContext> 
        NoOperacao criaNoOperacao(TipoContexto contexto, PortugolBaseVisitor visitor, Class<? extends NoOperacao> clazz, TerminalNode operador) {
            
            NoOperacao no = GeradorNoOperacao.gera(contexto, visitor, clazz);
            no.setTrechoCodigoFonteOperador(getTrechoCodigoFonte(operador));
            no.setTrechoCodigoFonte(new TrechoCodigoFonte(no.getOperandoEsquerdo().getTrechoCodigoFonte(), contexto.getText().length()));
            return no;
        }
        
        @Override
        public No visitOperacaoMenor(PortugolParser.OperacaoMenorContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaMenor.class, ctx.OP_MENOR());
        }

        @Override
        public No visitOperacaoMenorIgual(PortugolParser.OperacaoMenorIgualContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaMenorIgual.class, ctx.OP_MENOR_IGUAL());
        }

        @Override
        public No visitOperacaoMaior(PortugolParser.OperacaoMaiorContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaMaior.class, ctx.OP_MAIOR());
        }

        @Override
        public No visitOperacaoMaiorIgual(PortugolParser.OperacaoMaiorIgualContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaMaiorIgual.class, ctx.OP_MAIOR_IGUAL());
        }

        @Override
        public No visitOperacaoIgualdade(PortugolParser.OperacaoIgualdadeContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaIgualdade.class, ctx.OP_IGUALDADE());
        }

        @Override
        public No visitOperacaoDiferenca(PortugolParser.OperacaoDiferencaContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaDiferenca.class, ctx.OP_DIFERENCA());
         }

        @Override
        public No visitAdicao(PortugolParser.AdicaoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoSoma.class, ctx.OP_ADICAO());
        }

        @Override
        public No visitSubtracao(SubtracaoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoSubtracao.class, ctx.OP_SUBTRACAO());
        }
        
        @Override
        public No visitDivisao(DivisaoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoDivisao.class, ctx.OP_DIVISAO());
        }
        
        @Override
        public No visitMultiplicacao(MultiplicacaoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoMultiplicacao.class, ctx.OP_MULTIPLICACAO());
        }

        @Override
        public No visitModulo(ModuloContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoModulo.class, ctx.OP_MOD());
        }

        @Override
        public No visitOperacaoShiftLeft(OperacaoShiftLeftContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoBitwiseLeftShift.class, ctx.OP_SHIFT_LEFT());
        }

        @Override
        public No visitOperacaoShiftRight(OperacaoShiftRightContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoBitwiseRightShift.class, ctx.OP_SHIFT_RIGHT());
        }
        
        @Override
        public No visitOperacaoXor(OperacaoXorContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoBitwiseXOR.class, ctx.OP_XOR());
        }

        @Override
        public No visitOperacaoAndBitwise(OperacaoAndBitwiseContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoBitwiseE.class, ctx.E_COMERCIAL());
        }

        @Override
        public No visitOperacaoOrBitwise(OperacaoOrBitwiseContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoBitwiseOu.class, ctx.OP_OU_BITWISE());
        }
        
        @Override
        public No visitOperacaoELogico(OperacaoELogicoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaE.class, ctx.OP_E_LOGICO());
        }

        @Override
        public No visitOperacaoOuLogico(OperacaoOuLogicoContext ctx) {
            return criaNoOperacao(ctx, this, NoOperacaoLogicaOU.class, ctx.OP_OU_LOGICO());
        }

    }

}
