package br.univali.portugol.nucleo.analise.semantica;

import br.univali.portugol.nucleo.analise.semantica.avisos.AvisoSimboloGlobalOcultado;
import br.univali.portugol.nucleo.analise.semantica.avisos.AvisoValorExpressaoSeraConvertido;
import br.univali.portugol.nucleo.analise.semantica.erros.*;
import br.univali.portugol.nucleo.asa.NoInclusaoBiblioteca;
import br.univali.portugol.nucleo.analise.sintatica.AnalisadorSintatico;
import br.univali.portugol.nucleo.asa.*;
import br.univali.portugol.nucleo.bibliotecas.base.*;
import br.univali.portugol.nucleo.mensagens.AvisoAnalise;
import br.univali.portugol.nucleo.mensagens.ErroSemantico;
import br.univali.portugol.nucleo.simbolos.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta classe percorre a ASA gerada a partir do código fonte para detectar
 * erros de semântica.
 *
 *
 * @version 2.0
 *
 * @see AnalisadorSintatico
 * @see ObservadorAnaliseSemantica
 */
public final class AnalisadorSemantico implements VisitanteASA
{
    private static final Logger LOGGER = Logger.getLogger(AnalisadorSemantico.class.getName());
    
    private static final List<String> FUNCOES_RESERVADAS = getLista();

    private final Memoria memoria;
    private final List<ObservadorAnaliseSemantica> observadores;
    private final Map<String, MetaDadosBiblioteca> metaDadosBibliotecas;
    private final TabelaCompatibilidadeTipos tabelaCompatibilidadeTipos = TabelaCompatibilidadeTiposPortugol.INSTANCE;

    private boolean declarandoSimbolosGlobais;
    private ASA asa;
    private Funcao funcaoAtual;
    private Stack<TipoDado> tipoDadoEscolha = new Stack<>();

    private boolean declarandoVetor;
    private boolean declarandoMatriz;
    private boolean passandoReferencia = false;
    private boolean passandoParametro = false;

    public final static String FUNCAO_LEIA = "leia";
    public final static String FUNCAO_ESCREVA = "escreva";
    public static final String FUNCAO_LIMPA = "limpa";

    private int totalVariaveisDeclaradas = 0; // conta variáveis e parâmetros declarados
    private int totalVetoresDeclarados = 0;
    private int totalMatrizesDeclaradas = 0;
    
    public AnalisadorSemantico()
    {
        memoria = new Memoria();
        metaDadosBibliotecas = new TreeMap<>();
        observadores = new ArrayList<>();
    }

    /**
     * Permite adicionar um observador à análise semântica. Os observadores
     * serão notificados sobre cada erro semântico encontrado no código fonte e
     * deverão tratá-los apropriadamente, exibindo-os em uma IDE, por exemplo.
     *
     * @param observadorAnaliseSemantica o observador da análise semântica a ser
     * registrado.
     * @since 1.0
     */
    public void adicionarObservador(ObservadorAnaliseSemantica observadorAnaliseSemantica)
    {
        if (!observadores.contains(observadorAnaliseSemantica))
        {
            observadores.add(observadorAnaliseSemantica);
        }
    }

    /**
     * Remove um observador da análise previamente registrado utilizando o
     * método 
     * {@link AnalisadorSemantico#adicionarObservador(br.univali.portugol.nucleo.analise.semantica.ObservadorAnaliseSemantica) }.
     * Uma vez removido, o observador não será mais notificado dos erros
     * semânticos encontrados durante a análise.
     *
     * @param observadorAnaliseSemantica um observador de análise semântica
     * previamente registrado.
     * @since 1.0
     */
    public void removerObservador(ObservadorAnaliseSemantica observadorAnaliseSemantica)
    {
        observadores.remove(observadorAnaliseSemantica);
    }

    private void notificarAviso(AvisoAnalise aviso)
    {
        for (ObservadorAnaliseSemantica observadorAnaliseSemantica : observadores)
        {
            observadorAnaliseSemantica.tratarAviso(aviso);
        }
    }

    private void notificarErroSemantico(ErroSemantico erroSemantico)
    {
        for (ObservadorAnaliseSemantica observadorAnaliseSemantica : observadores)
        {
            observadorAnaliseSemantica.tratarErroSemantico(erroSemantico);
        }
    }

    /**
     * Realiza a análise semântica de uma ASA. Este método não retorna valor e
     * não gera exceções. Para capturar os erros semânticos gerados durante a
     * análise, deve-se registrar um ou mais obsrvadores de análise utilizando o
     * método 
     * {@link AnalisadorSemantico#adicionarObservador(br.univali.portugol.nucleo.analise.semantica.ObservadorAnaliseSemantica) }.
     *
     * @param asa a ASA que será percorrida em busca de erros semânticos.
     * @since 1.0
     */
    public void analisar(ASA asa)
    {
        this.asa = asa;
        if (asa != null)
        {
            try
            {
                asa.aceitar(this);
            }
            catch (Exception excecao)
            {
                notificarErroSemantico(new ErroSemanticoNaoTratado(excecao));
            }
        }
    }

    @Override
    public Object visitar(ASAPrograma asap) throws ExcecaoVisitaASA
    {
        for (NoInclusaoBiblioteca inclusao : asap.getListaInclusoesBibliotecas())
        {
            inclusao.aceitar(this);
        }

        // Executa a primeira vez para declarar as funções na tabela de símbolos
        declarandoSimbolosGlobais = true;

        for (NoDeclaracao declaracao : asap.getListaDeclaracoesGlobais())
        {
            declaracao.aceitar(this);
        }

        declarandoSimbolosGlobais = false;

        // Executa a segunda vez para analizar os blocos das funções
        for (NoDeclaracao declaracao : asap.getListaDeclaracoesGlobais())
        {
            declaracao.aceitar(this);
        }

        asap.setTotalVariaveisDeclaradas(totalVariaveisDeclaradas);
        asap.setTotalVetoresDeclarados(totalVetoresDeclarados);
        asap.setTotalMatrizesDeclaradas(totalMatrizesDeclaradas);
        
        return null;
    }

    @Override
    public Object visitar(NoCadeia noCadeia) throws ExcecaoVisitaASA
    {
        return TipoDado.CADEIA;
    }

    @Override
    public Object visitar(NoCaracter noCaracter) throws ExcecaoVisitaASA
    {
        return TipoDado.CARACTER;
    }

    @Override
    public Object visitar(NoCaso noCaso) throws ExcecaoVisitaASA
    {
        if (noCaso.getExpressao() != null)
        {
            TipoDado tipoDado = (TipoDado) noCaso.getExpressao().aceitar(this);

            if ((tipoDadoEscolha.peek() == TipoDado.INTEIRO) || (tipoDadoEscolha.peek() == TipoDado.CARACTER))
            {
                if (tipoDado != tipoDadoEscolha.peek())
                {
                    notificarErroSemantico(new ErroTiposIncompativeis(noCaso, tipoDado, tipoDadoEscolha.peek()));
                }
            }
            else if ((tipoDado != TipoDado.INTEIRO) && (tipoDado != TipoDado.CARACTER))
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noCaso, tipoDado, TipoDado.INTEIRO, TipoDado.CARACTER));
            }
        }

        analisarListaBlocos(noCaso.getBlocos());

        return null;
    }

    @Override
    public Object visitar(NoChamadaFuncao chamadaFuncao) throws ExcecaoVisitaASA
    {
        verificarFuncaoExiste(chamadaFuncao);
        verificarQuantidadeParametros(chamadaFuncao);
        verificarTiposParametros(chamadaFuncao);
        verificarQuantificador(chamadaFuncao);
        verificarModoAcesso(chamadaFuncao);
        verificarParametrosObsoletos(chamadaFuncao);

        return obterTipoRetornoFuncao(chamadaFuncao);
    }

    private void verificarModoAcesso(NoChamadaFuncao chamadaFuncao)
    {
        List<ModoAcesso> modosAcessoEsperados = obterModosAcessoEsperados(chamadaFuncao);
        List<ModoAcesso> modosAcessoPassados = obterModosAcessoPassados(chamadaFuncao);

        int cont = Math.min(modosAcessoEsperados.size(), modosAcessoPassados.size());
        
        if (chamadaFuncao.getNome().equals(FUNCAO_LEIA))
        {
            cont = modosAcessoPassados.size();// a função leia retorna uma lista vazia em modos de acesso esperados
            for (int indice = 0; indice < cont; indice++)
            {
                NoExpressao parametro = chamadaFuncao.getParametros().get(indice);
                boolean parametroValido = parametro instanceof NoReferenciaVariavel || parametro instanceof NoReferenciaVetor || parametro instanceof NoReferenciaMatriz;

                // verifica se o usuário está tentando usar uma constante na função LEIA
                if (parametroValido && parametro instanceof NoReferenciaVariavel) {
                    NoDeclaracao origemDaReferencia = ((NoReferenciaVariavel)parametro).getOrigemDaReferencia();
                    parametroValido = origemDaReferencia != null && !origemDaReferencia.constante();
                        
                }
                if (!parametroValido)
                {
                    notificarErroSemantico(new ErroPassagemParametroInvalida(chamadaFuncao.getParametros().get(indice), obterNomeParametro(chamadaFuncao, indice), chamadaFuncao.getNome(), indice));
                }
            }
        }
        else
        {
            for (int indice = 0; indice < cont; indice++)
            {
                ModoAcesso modoAcessoEsperado = modosAcessoEsperados.get(indice);
                ModoAcesso modoAcessoPassado = modosAcessoPassados.get(indice);

                if (modoAcessoEsperado == ModoAcesso.POR_REFERENCIA && modoAcessoPassado == ModoAcesso.POR_VALOR)
                {


                    notificarErroSemantico(new ErroPassagemParametroInvalida(chamadaFuncao.getParametros().get(indice), obterNomeParametro(chamadaFuncao, indice), chamadaFuncao.getNome(), indice));
                }
            }
        }
    }

        

    private List<ModoAcesso> obterModosAcessoPassados(NoChamadaFuncao chamadaFuncao)
    {
        List<ModoAcesso> modosAcesso = new ArrayList<>();

        if (chamadaFuncao.getParametros() != null)
        {
            for (NoExpressao parametro : chamadaFuncao.getParametros())
            {
                if (parametro instanceof NoReferenciaVariavel)
                {
                    NoReferenciaVariavel noReferenciaVariavel = (NoReferenciaVariavel) parametro;

                    if (noReferenciaVariavel.getEscopo() == null)
                    {
                        try
                        {
                            Simbolo simbolo = memoria.getSimbolo(noReferenciaVariavel.getNome());
                            if (simbolo.constante())
                            {
                                modosAcesso.add(ModoAcesso.POR_VALOR);
                            }
                            else
                            {
                                modosAcesso.add(ModoAcesso.POR_REFERENCIA);
                            }
                        }
                        catch (ExcecaoSimboloNaoDeclarado excecao)
                        {
                            // Não faz nada aqui
                        }
                    }
                    else
                    {
                        modosAcesso.add(ModoAcesso.POR_VALOR);
                    }
                }
                else
                {
                    modosAcesso.add(ModoAcesso.POR_VALOR);
                }
            }
        }

        return modosAcesso;
    }

    private List<ModoAcesso> obterModosAcessoEsperados(NoChamadaFuncao chamadaFuncao)
    {
        List<ModoAcesso> modosAcesso = new ArrayList<>();

        if (chamadaFuncao.getEscopo() == null)
        {
            if (!FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                try
                {
                    Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());

                    for (NoDeclaracaoParametro parametro : funcao.getParametros())
                    {
                        modosAcesso.add(parametro.getModoAcesso());
                    }
                }
                catch (ExcecaoSimboloNaoDeclarado ex)
                {
                    // Não faz nada aqui
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());
            MetaDadosParametros metaDadosParametros = metaDadosFuncao.obterMetaDadosParametros();

            for (MetaDadosParametro metaDadosParametro : metaDadosParametros)
            {
                modosAcesso.add(metaDadosParametro.getModoAcesso());
            }
        }

        return modosAcesso;
    }

    private TipoDado obterTipoRetornoFuncao(NoChamadaFuncao chamadaFuncao)
    {
        if (chamadaFuncao.getEscopo() == null)
        {
            if (FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                return TipoDado.VAZIO;
            }
            else
            {
                try
                {
                    Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());

                    return funcao.getTipoDado();
                }
                catch (ExcecaoSimboloNaoDeclarado excecao)
                {
                    // Não faz nada aqui
                }
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());

            return metaDadosFuncao.getTipoDado();
        }

        return null;
    }

    private void verificarParametrosObsoletos(final NoChamadaFuncao chamadaFuncao)
    {
        int parametrosEsperados = obterNumeroParametrosEsperados(chamadaFuncao);
        int parametrosPassados = (chamadaFuncao.getParametros() != null) ? chamadaFuncao.getParametros().size() : 0;

        int inicio = Math.min(parametrosEsperados, parametrosPassados);

        if (chamadaFuncao.getParametros() != null)
        {
            for (int indice = inicio; indice < parametrosPassados; indice++)
            {
                NoExpressao parametro = chamadaFuncao.getParametros().get(indice);

                notificarErroSemantico(new ErroParametroExcedente(parametro.getTrechoCodigoFonte(), chamadaFuncao));
            }
        }
    }

    private void verificarQuantificador(NoChamadaFuncao chamadaFuncao)
    {
        List<Quantificador> quantificadoresEsperados = obterQuantificadoresEsperados(chamadaFuncao);
        List<Quantificador> quantificadoresPassados = obterQuantificadoresPassados(chamadaFuncao);

        int cont = Math.min(quantificadoresEsperados.size(), quantificadoresPassados.size());

        for (int indice = 0; indice < cont; indice++)
        {
            Quantificador quantificadorPassado = quantificadoresPassados.get(indice);
            Quantificador quantificadorEsperado = quantificadoresEsperados.get(indice);

            if (quantificadorPassado != quantificadorEsperado)
            {
                notificarErroSemantico(new ErroQuantificadorParametroFuncao(chamadaFuncao, indice, obterNomeParametro(chamadaFuncao, indice), quantificadorEsperado, quantificadorPassado));
            }
        }
    }

    private List<Quantificador> obterQuantificadoresEsperados(NoChamadaFuncao chamadaFuncao)
    {
        List<Quantificador> quantificadores = new ArrayList<>();

        if (chamadaFuncao.getEscopo() == null)
        {
            if (!FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                try
                {
                    Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());

                    for (NoDeclaracaoParametro declaracaoParametro : funcao.getParametros())
                    {
                        quantificadores.add(declaracaoParametro.getQuantificador());
                    }
                }
                catch (ExcecaoSimboloNaoDeclarado ex)
                {
                    // Não faz nada aqui
                }
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());
            MetaDadosParametros metaDadosParametros = metaDadosFuncao.obterMetaDadosParametros();

            for (MetaDadosParametro metaDadosParametro : metaDadosParametros)
            {
                quantificadores.add(metaDadosParametro.getQuantificador());
            }
        }

        return quantificadores;
    }

    private List<Quantificador> obterQuantificadoresPassados(NoChamadaFuncao chamadaFuncao)
    {
        List<Quantificador> quantificadores = new ArrayList<>();

        if (chamadaFuncao.getParametros() != null)
        {
            for (NoExpressao parametroPassado : chamadaFuncao.getParametros())
            {
                try
                {
                    if (parametroPassado instanceof NoReferenciaVariavel)
                    {
                        String nome = ((NoReferenciaVariavel) parametroPassado).getNome();
                        Simbolo simbolo = memoria.getSimbolo(nome);

                        if (simbolo instanceof Variavel)
                        {
                            quantificadores.add(Quantificador.VALOR);
                        }
                        else if (simbolo instanceof Vetor)
                        {
                            quantificadores.add(Quantificador.VETOR);
                        }
                        else if (simbolo instanceof Matriz)
                        {
                            quantificadores.add(Quantificador.MATRIZ);
                        }
                    }
                    else if (parametroPassado instanceof NoVetor)
                    {
                        quantificadores.add(Quantificador.VETOR);
                    }
                    else if (parametroPassado instanceof NoMatriz)
                    {
                        quantificadores.add(Quantificador.MATRIZ);
                    }
                    else
                    {
                        quantificadores.add(Quantificador.VALOR);
                    }
                }
                catch (ExcecaoSimboloNaoDeclarado ex)
                {
                    // Não faz nada aqui
                }
            }
        }

        return quantificadores;
    }

    private void verificarTiposParametros(NoChamadaFuncao chamadaFuncao) throws ExcecaoVisitaASA
    {
        List<ModoAcesso> modosAcesso = obterModosAcessoEsperados(chamadaFuncao);
        List<TipoDado> tiposEsperados = obterTiposParametrosEsperados(chamadaFuncao);
        List<TipoDado> tiposPassado = obterTiposParametrosPassados(chamadaFuncao, modosAcesso);

        int cont = Math.min(tiposEsperados.size(), tiposPassado.size());

        if (chamadaFuncao.getNome().equals(FUNCAO_ESCREVA))
        {
            int tamanhoTiposPassado = tiposPassado.size();
            for (int indice = 0; indice < tamanhoTiposPassado; indice++)
            {
                TipoDado tipoPassado = tiposPassado.get(indice);
                
                if (tipoPassado == null)
                {
                    continue;
                }
                
                if (tipoPassado.equals(TipoDado.VAZIO))
                {
                    notificarErroSemantico(new ErroTipoParametroIncompativel(chamadaFuncao.getNome(), obterNomeParametro(chamadaFuncao, indice), chamadaFuncao.getParametros().get(indice), TipoDado.TODOS, tipoPassado));
                }
            }
        }

        for (int indice = 0; indice < cont; indice++)
        {
            TipoDado tipoPassado = tiposPassado.get(indice);
            TipoDado tipoEsperado = tiposEsperados.get(indice);
            ModoAcesso modoAcesso = modosAcesso.get(indice);

            if (tipoPassado != null)
            {
                try
                {
                    tabelaCompatibilidadeTipos.obterTipoRetornoPassagemParametro(tipoEsperado, tipoPassado);
                }
                catch (ExcecaoValorSeraConvertido excecao)
                {
                    if (modoAcesso == ModoAcesso.POR_REFERENCIA)
                    {
                        notificarErroSemantico(new ErroTipoParametroIncompativel(chamadaFuncao.getNome(), obterNomeParametro(chamadaFuncao, indice), chamadaFuncao.getParametros().get(indice), tipoEsperado, tipoPassado));
                    }
                    else
                    {
                        notificarAviso(new AvisoValorExpressaoSeraConvertido(chamadaFuncao.getParametros().get(indice).getTrechoCodigoFonte(), chamadaFuncao, tipoPassado, tipoEsperado, obterNomeParametro(chamadaFuncao, indice)));
                    }
                }
                catch (ExcecaoImpossivelDeterminarTipoDado excecao)
                {
                    notificarErroSemantico(new ErroTipoParametroIncompativel(chamadaFuncao.getNome(), obterNomeParametro(chamadaFuncao, indice), chamadaFuncao.getParametros().get(indice), tipoEsperado, tipoPassado));
                }
            }
        }
    }

    private String obterNomeParametro(NoChamadaFuncao chamadaFuncao, int indice)
    {
        if (chamadaFuncao.getEscopo() == null)
        {
            try
            {
                Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());

                return funcao.getParametros().get(indice).getNome();
            }
            catch (ExcecaoSimboloNaoDeclarado ex)
            {
                // Não deve cair aqui nunca
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());
            MetaDadosParametros metaDadosParametros = metaDadosFuncao.obterMetaDadosParametros();

            return metaDadosParametros.obter(indice).getNome();
        }
        return "";
    }

    private List<TipoDado> obterTiposParametrosEsperados(NoChamadaFuncao chamadaFuncao)
    {
        List<TipoDado> tipos = new ArrayList<>();

        if (chamadaFuncao.getEscopo() == null)
        {
            if (!FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                try
                {
                    Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());
                    List<NoDeclaracaoParametro> parametros = funcao.getParametros();

                    if (parametros != null)
                    {
                        for (NoDeclaracaoParametro parametro : parametros)
                        {
                            tipos.add(parametro.getTipoDado());
                        }
                    }
                }
                catch (ExcecaoSimboloNaoDeclarado ex)
                {
                    // Não deve cair aqui nunca
                }
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());
            MetaDadosParametros metaDadosParametros = metaDadosFuncao.obterMetaDadosParametros();

            for (MetaDadosParametro metaDadosParametro : metaDadosParametros)
            {
                tipos.add(metaDadosParametro.getTipoDado());
            }
        }

        return tipos;
    }

    private List<TipoDado> obterTiposParametrosPassados(NoChamadaFuncao chamadaFuncao, List<ModoAcesso> modosAcesso) throws ExcecaoVisitaASA
    {
        List<TipoDado> tipos = new ArrayList<>();

        if (chamadaFuncao.getParametros() != null)
        {
            for (int indice = 0; indice < chamadaFuncao.getParametros().size(); indice++)
            {
                NoExpressao parametro = chamadaFuncao.getParametros().get(indice);

                if (chamadaFuncao.getEscopo() == null && FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
                {
                    passandoReferencia = false;
                }
                else if (indice < modosAcesso.size())
                {
                    passandoReferencia = modosAcesso.get(indice) == ModoAcesso.POR_REFERENCIA;
                }
                else
                {
                    passandoReferencia = false;
                }

                try
                {
                    if (parametro instanceof NoReferenciaVariavel && chamadaFuncao.getNome().equals(FUNCAO_LEIA))
                    {
                        String nome = ((NoReferenciaVariavel) parametro).getNome();

                        try
                        {
                            Simbolo variavel = memoria.getSimbolo(nome);
                            variavel.setInicializado(true);
                        }
                        catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
                        {
                            // Não faz nada
                        }
                    }
                    passandoParametro = (chamadaFuncao.getEscopo() == null && !FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()));
                    tipos.add((TipoDado) parametro.aceitar(this));
                    passandoParametro = false;
                }
                catch (ExcecaoVisitaASA ex)
                {
                    if (ex.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado)
                    {
                        tipos.add(null);
                    }
                    else
                    {
                        throw ex;
                    }
                }

                passandoReferencia = false;
            }
        }

        return tipos;
    }

    private void verificarQuantidadeParametros(NoChamadaFuncao chamadaFuncao)
    {
        int esperados = obterNumeroParametrosEsperados(chamadaFuncao);
        int passados = (chamadaFuncao.getParametros() != null) ? chamadaFuncao.getParametros().size() : 0;

        //Funções como leia e escreva aceitam numeros infinitos de parametros, mas não nenhum.
        if ((esperados == Integer.MAX_VALUE && passados == 0) || (esperados != Integer.MAX_VALUE && passados != esperados))
        {
            notificarErroSemantico(new ErroNumeroParametrosFuncao(passados, esperados, chamadaFuncao));
        }
    }

    private int obterNumeroParametrosEsperados(NoChamadaFuncao chamadaFuncao)
    {
        if (chamadaFuncao.getEscopo() == null)
        {
            if (FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                if (chamadaFuncao.getNome().equals(FUNCAO_LIMPA))
                {
                    return 0;
                }
                else
                {
                    return Integer.MAX_VALUE;
                }
            }
            try
            {
                Funcao funcao = (Funcao) memoria.getSimbolo(chamadaFuncao.getNome());
                List<NoDeclaracaoParametro> parametros = funcao.getParametros();

                if (parametros != null)
                {
                    return parametros.size();
                }
                else
                {
                    return 0;
                }
            }
            catch (ExcecaoSimboloNaoDeclarado ex)
            {
                return -1;
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());
            MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());

            return metaDadosFuncao.obterMetaDadosParametros().quantidade();
        }
    }

    private void verificarFuncaoExiste(NoChamadaFuncao chamadaFuncao) throws ExcecaoVisitaASA
    {
        if (chamadaFuncao.getEscopo() == null)
        {
            if (!FUNCOES_RESERVADAS.contains(chamadaFuncao.getNome()))
            {
                try
                {
                    Simbolo simbolo = memoria.getSimbolo(chamadaFuncao.getNome());
                    if (!(simbolo instanceof Funcao))
                    {
                        notificarErroSemantico(new ErroReferenciaInvalida(chamadaFuncao, simbolo));
                        throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, chamadaFuncao);
                    }
                    else
                    {
                        simbolo.getOrigemDoSimbolo().adicionarReferencia(chamadaFuncao);
                    }
                }
                catch (ExcecaoSimboloNaoDeclarado ex)
                {
                    notificarErroSemantico(new ErroSimboloNaoDeclarado(chamadaFuncao));
                    throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, chamadaFuncao);
                }
            }
        }
        else
        {
            MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(chamadaFuncao.getEscopo());

            if (metaDadosBiblioteca != null)
            {
                MetaDadosFuncao metaDadosFuncao = metaDadosBiblioteca.obterMetaDadosFuncoes().obter(chamadaFuncao.getNome());
                
                if (metaDadosFuncao == null)
                {
                    notificarErroSemantico(new ErroSimboloNaoDeclarado(chamadaFuncao));
                    throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, chamadaFuncao);
                }
                else
                {
                    chamadaFuncao.setFuncaoDeBiblioteca(true);
                    chamadaFuncao.setTipoRetornoBiblioteca(metaDadosFuncao.getTipoDado());
                }
            }
            else
            {
                notificarErroSemantico(new ErroInclusaoBiblioteca(chamadaFuncao.getTrechoCodigoFonteNome(), new Exception(String.format("A biblioteca '%s' não foi incluída no programa", chamadaFuncao.getEscopo()))));
                throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, chamadaFuncao);
            }
        }
    }

    private void verificarRetornoFuncao(NoDeclaracaoFuncao noDeclaracaoFuncao) throws ExcecaoVisitaASA
    {
        if (noDeclaracaoFuncao.getTipoDado() != TipoDado.VAZIO)
        {
            AnalisadorRetornoDeFuncao analisador = new AnalisadorRetornoDeFuncao();

            if (!analisador.possuiRetornoObrigatorio(noDeclaracaoFuncao))
            {
                notificarErroSemantico(new ErroFuncaoSemRetorne(noDeclaracaoFuncao));
            }

        }
    }

    @Override
    public Object visitar(NoDeclaracaoFuncao declaracaoFuncao) throws ExcecaoVisitaASA
    {
        if (declarandoSimbolosGlobais)
        {
            String nome = declaracaoFuncao.getNome();
            TipoDado tipoDado = declaracaoFuncao.getTipoDado();
            Quantificador quantificador = declaracaoFuncao.getQuantificador();

            Funcao funcao = new Funcao(nome, tipoDado, quantificador, declaracaoFuncao.getParametros(), declaracaoFuncao);
            funcao.setTrechoCodigoFonteNome(declaracaoFuncao.getTrechoCodigoFonteNome());
            funcao.setTrechoCodigoFonteTipoDado(declaracaoFuncao.getTrechoCodigoFonteTipoDado());
            try
            {
                Simbolo simbolo = memoria.getSimbolo(nome);
                notificarErroSemantico(new ErroSimboloRedeclarado(funcao, simbolo));

                funcao.setRedeclarado(true);
            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                memoria.adicionarSimbolo(funcao);
            }
            
            if (funcao.getNome().equals("inicio") && !funcao.getParametros().isEmpty())
            {
                notificarErroSemantico(new ErroFuncaoInicioNaoAceitaParametros(declaracaoFuncao));
            }
        }
        else
        {
            try
            {
                funcaoAtual = (Funcao) memoria.getSimbolo(declaracaoFuncao.getNome());
                memoria.empilharFuncao();
                List<NoDeclaracaoParametro> parametros = declaracaoFuncao.getParametros();
                for (NoDeclaracaoParametro noDeclaracaoParametro : parametros)
                {
                    noDeclaracaoParametro.aceitar(this);
                }
                analisarListaBlocos(declaracaoFuncao.getBlocos());
                verificarRetornoFuncao(declaracaoFuncao);
            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                throw new ExcecaoVisitaASA(excecaoSimboloNaoDeclarado, asa, declaracaoFuncao);
            }
            catch(ClassCastException castException) {
                // essa exceção acontece quando existe uma função declarada com o mesmo nome de uma variável global. Ela está
                // sendo 'engolida' para que a stacktrace do java não seja exibida na console do PS
            }
            finally
            {
                try
                {
                    memoria.desempilharFuncao();
                }
                catch (EmptyStackException e)
                {
                    // esta excessão ocorre quando a ClassCastException acima também acontece (função com mesmo nome de variável global). 
                    // Nesses casos a função não chega a ser empilhada (porque o nome dela coincide com uma variável) e então a pilha
                    // está vazia, gerando uma EmptyStackException quando se tenta 'desempilhar'.
                    // Estamos 'engolindo' a excessão aqui apenas para evitar que a stack trace seja mostrada na console do PS.
                }
            }
        }

        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoMatriz noDeclaracaoMatriz) throws ExcecaoVisitaASA
    {
        noDeclaracaoMatriz.setIdParaInspecao(totalMatrizesDeclaradas);
        totalMatrizesDeclaradas++;
        
        if (declarandoSimbolosGlobais == memoria.isEscopoGlobal())
        {
            String nome = noDeclaracaoMatriz.getNome();
            TipoDado tipoDados = noDeclaracaoMatriz.getTipoDado();
            Integer linhas = obterTamanhoVetorMatriz(noDeclaracaoMatriz.getNumeroLinhas(), noDeclaracaoMatriz);
            Integer colunas = obterTamanhoVetorMatriz(noDeclaracaoMatriz.getNumeroColunas(), noDeclaracaoMatriz);

            if (linhas != null && colunas != null)
            {

                BigInteger bigLinhas = new BigInteger(linhas.toString());
                BigInteger bigColunas = new BigInteger(colunas.toString());
                BigInteger bigMax = new BigInteger(Matriz.TAMANHO_MAXIMO.toString());
                BigInteger bigProduto = bigLinhas.multiply(bigColunas);

                if (bigProduto.compareTo(bigMax) > 0)
                {
                    notificarErroSemantico(new ErroTamanhoMaximoMatriz(linhas, colunas, nome, bigProduto, noDeclaracaoMatriz.getTrechoCodigoFonteNome()));
                }
            }

            Matriz matriz = new Matriz(nome, tipoDados, noDeclaracaoMatriz, 1, 1);
            matriz.setTrechoCodigoFonteNome(noDeclaracaoMatriz.getTrechoCodigoFonteNome());
            matriz.setTrechoCodigoFonteTipoDado(noDeclaracaoMatriz.getTrechoCodigoFonteTipoDado());

            try
            {
                Simbolo simboloExistente = memoria.getSimbolo(nome);
                final boolean global = memoria.isGlobal(simboloExistente);
                final boolean local = memoria.isLocal(simboloExistente);
                memoria.empilharEscopo();
                memoria.adicionarSimbolo(matriz);
                final boolean global1 = memoria.isGlobal(matriz);
                final boolean local1 = memoria.isLocal(matriz);
                if ((global && global1) || (local && local1))
                {
                    matriz.setRedeclarado(true);
                    notificarErroSemantico(new ErroSimboloRedeclarado(matriz, simboloExistente));
                    memoria.desempilharEscopo();
                }
                else
                {
                    memoria.desempilharEscopo();
                    memoria.adicionarSimbolo(matriz);
                    Simbolo simboloGlobal = memoria.isGlobal(simboloExistente) ? simboloExistente : matriz;
                    Simbolo simboloLocal = memoria.isGlobal(simboloExistente) ? matriz : simboloExistente;

                    notificarAviso(new AvisoSimboloGlobalOcultado(simboloGlobal, simboloLocal, noDeclaracaoMatriz));
                }

            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                if (FUNCOES_RESERVADAS.contains(nome))
                {
                    matriz.setRedeclarado(true);
                    Funcao funcaoSistam = new Funcao(nome, TipoDado.VAZIO, Quantificador.VETOR, null, null);
                    notificarErroSemantico(new ErroSimboloRedeclarado(matriz, funcaoSistam));
                }
                else
                {
                    memoria.adicionarSimbolo(matriz);
                }
            }

            if (noDeclaracaoMatriz.constante() && noDeclaracaoMatriz.getInicializacao() == null)
            {
                NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                referencia.setTrechoCodigoFonteNome(noDeclaracaoMatriz.getTrechoCodigoFonteNome());

                notificarErroSemantico(new ErroSimboloNaoInicializado(referencia, matriz));
            }

            if (noDeclaracaoMatriz.getInicializacao() != null)
            {
                if (noDeclaracaoMatriz.getInicializacao() instanceof NoMatriz)
                {
                    NoExpressao inicializacao = noDeclaracaoMatriz.getInicializacao();
                    NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                    referencia.setTrechoCodigoFonteNome(noDeclaracaoMatriz.getTrechoCodigoFonteNome());
                    NoOperacao operacao = new NoOperacaoAtribuicao(referencia, inicializacao);

                    if (linhas != null)
                    {
                        int numeroLinhasDeclaradas = ((NoMatriz) inicializacao).getValores().size();

                        if (linhas != numeroLinhasDeclaradas)
                        {
                            notificarErroSemantico(new ErroQuantidadeLinhasIncializacaoMatriz(noDeclaracaoMatriz.getInicializacao().getTrechoCodigoFonte(), nome, linhas, numeroLinhasDeclaradas));
                        }
                    }

                    if (colunas != null)
                    {
                        for (int linha = 0; linha < ((NoMatriz) inicializacao).getValores().size(); linha++)
                        {
                            List<List<Object>> valores = ((NoMatriz) noDeclaracaoMatriz.getInicializacao()).getValores();

                            if (colunas != valores.get(linha).size())
                            {
                                notificarErroSemantico(new ErroQuantidadeElementosColunaInicializacaoMatriz(noDeclaracaoMatriz.getInicializacao().getTrechoCodigoFonte(), nome, linha, colunas, valores.get(linha).size()));
                            }
                        }
                    }

                    if (noDeclaracaoMatriz.constante() && linhas != null && colunas != null)
                    {
                        List<List<Object>> valores = ((NoMatriz) noDeclaracaoMatriz.getInicializacao()).getValores();

                        for (int linha = 0; linha < valores.size(); linha++)
                        {
                            for (int coluna = 0; coluna < valores.get(linha).size(); coluna++)
                            {
                                if (!(valores.get(linha).get(coluna) instanceof NoExpressaoLiteral))
                                {
                                    notificarErroSemantico(new ErroInicializacaoConstante(noDeclaracaoMatriz, linha, coluna));
                                }
                            }
                        }
                    }

                    try
                    {
                        this.declarandoMatriz = true;
                        operacao.aceitar(this);
                        this.declarandoMatriz = false;
                    }
                    catch (ExcecaoVisitaASA excecao)
                    {
                        if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
                        {
                            throw excecao;
                        }
                    }
                }
                else
                {
                    if (linhas == null)
                    {
                        linhas = 0;
                    }

                    if (colunas == null)
                    {
                        colunas = 0;
                    }

                    notificarErroSemantico(new ErroAoInicializarMatriz(matriz, noDeclaracaoMatriz.getInicializacao().getTrechoCodigoFonte(), linhas, colunas));
                }
            }

            matriz.setConstante(noDeclaracaoMatriz.constante());
        }

        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoVariavel declaracaoVariavel) throws ExcecaoVisitaASA
    {
        declaracaoVariavel.setIdParaInspecao(totalVariaveisDeclaradas);
        //System.out.println(declaracaoVariavel.getNome() + " => " + totalVariaveisDeclaradas);
        totalVariaveisDeclaradas++;
        
        if (declarandoSimbolosGlobais == memoria.isEscopoGlobal())
        {
            String nome = declaracaoVariavel.getNome();
            TipoDado tipoDadoVariavel = declaracaoVariavel.getTipoDado();
            
            Variavel variavel = new Variavel(nome, tipoDadoVariavel, declaracaoVariavel);
            variavel.setTrechoCodigoFonteNome(declaracaoVariavel.getTrechoCodigoFonteNome());
            variavel.setTrechoCodigoFonteTipoDado(declaracaoVariavel.getTrechoCodigoFonteTipoDado());

            try
            {
                Simbolo simboloExistente = memoria.getSimbolo(nome);
                final boolean global = memoria.isGlobal(simboloExistente);
                final boolean local = memoria.isLocal(simboloExistente);
                memoria.empilharEscopo();
                memoria.adicionarSimbolo(variavel);
                final boolean global1 = memoria.isGlobal(variavel);
                final boolean local1 = memoria.isLocal(variavel);
                if ((global && global1) || (local && local1))
                {
                    variavel.setRedeclarado(true);
                    notificarErroSemantico(new ErroSimboloRedeclarado(variavel, simboloExistente));
                    memoria.desempilharEscopo();
                }
                else
                {
                    memoria.desempilharEscopo();
                    memoria.adicionarSimbolo(variavel);
                    Simbolo simboloGlobal = memoria.isGlobal(simboloExistente) ? simboloExistente : variavel;
                    Simbolo simboloLocal = memoria.isGlobal(simboloExistente) ? variavel : simboloExistente;

                    notificarAviso(new AvisoSimboloGlobalOcultado(simboloGlobal, simboloLocal, declaracaoVariavel));
                }
            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                if (FUNCOES_RESERVADAS.contains(nome))
                {
                    variavel.setRedeclarado(true);
                    Funcao funcaoSistam = new Funcao(nome, TipoDado.VAZIO, Quantificador.VETOR, null, null);
                    notificarErroSemantico(new ErroSimboloRedeclarado(variavel, funcaoSistam));
                }
                else
                {
                    memoria.adicionarSimbolo(variavel);
                }
            }

            if (declaracaoVariavel.constante() && declaracaoVariavel.getInicializacao() == null)
            {
                NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                referencia.setTrechoCodigoFonteNome(declaracaoVariavel.getTrechoCodigoFonteNome());

                notificarErroSemantico(new ErroSimboloNaoInicializado(referencia, variavel));
            }

            if (declaracaoVariavel.getInicializacao() != null)
            {
                // Posteriormente restringir na gramática para não permitir atribuir vetor ou matriz a uma variável comum

                if (!(declaracaoVariavel.getInicializacao() instanceof NoVetor) && !(declaracaoVariavel.getInicializacao() instanceof NoMatriz))
                {
                    NoExpressao inicializacao = declaracaoVariavel.getInicializacao();
                    NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                    referencia.setTrechoCodigoFonteNome(declaracaoVariavel.getTrechoCodigoFonteNome());
                    NoOperacao operacao = new NoOperacaoAtribuicao(referencia, inicializacao);

                    memoria.empilharEscopo();
                    memoria.adicionarSimbolo(variavel);

                    if (declaracaoVariavel.constante())
                    {
                        if (inicializacao instanceof NoMenosUnario)
                        {
                            if (!(((NoMenosUnario) inicializacao).getExpressao() instanceof NoExpressaoLiteral))
                            {
                                notificarErroSemantico(new ErroInicializacaoConstante(declaracaoVariavel));
                            }
                        }
                        else if (!(inicializacao instanceof NoExpressaoLiteral))
                        {
                            notificarErroSemantico(new ErroInicializacaoConstante(declaracaoVariavel));
                        }
                    }

                    try
                    {
                        operacao.aceitar(this);
                    }
                    catch (ExcecaoVisitaASA excecao)
                    {
                        if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
                        {
                            throw excecao;
                        }
                    }

                    memoria.desempilharEscopo();
                }
                else
                {
                    notificarErroSemantico(new ErroInicializacaoInvalida(declaracaoVariavel));
                }
            }

            variavel.setConstante(declaracaoVariavel.constante());

        }
        return null;
    }

    @Override
    public Object visitar(NoDeclaracaoVetor noDeclaracaoVetor) throws ExcecaoVisitaASA
    {
        noDeclaracaoVetor.setIdParaInspecao(totalVetoresDeclarados);
        totalVetoresDeclarados++;
        
        if (declarandoSimbolosGlobais == memoria.isEscopoGlobal())
        {
            String nome = noDeclaracaoVetor.getNome();
            TipoDado tipoDados = noDeclaracaoVetor.getTipoDado();
            NoExpressao expTamanho = noDeclaracaoVetor.getTamanho();

            Integer tamanho = obterTamanhoVetorMatriz(expTamanho, noDeclaracaoVetor);

            if (tamanho != null)
            {
                if (tamanho > Vetor.TAMANHO_MAXIMO)
                {
                    notificarErroSemantico(new ErroTamanhoMaximoVetor(tamanho, nome, noDeclaracaoVetor.getTrechoCodigoFonteNome()));
                }
            }
            Vetor vetor = new Vetor(nome, tipoDados, noDeclaracaoVetor, 1);
            vetor.setTrechoCodigoFonteNome(noDeclaracaoVetor.getTrechoCodigoFonteNome());
            vetor.setTrechoCodigoFonteTipoDado(noDeclaracaoVetor.getTrechoCodigoFonteTipoDado());

            try
            {
                Simbolo simboloExistente = memoria.getSimbolo(nome);
                final boolean global = memoria.isGlobal(simboloExistente);
                final boolean local = memoria.isLocal(simboloExistente);
                memoria.empilharEscopo();
                memoria.adicionarSimbolo(vetor);
                final boolean global1 = memoria.isGlobal(vetor);
                final boolean local1 = memoria.isLocal(vetor);
                if ((global && global1) || (local && local1))
                {
                    vetor.setRedeclarado(true);
                    notificarErroSemantico(new ErroSimboloRedeclarado(vetor, simboloExistente));
                    memoria.desempilharEscopo();
                }
                else
                {
                    memoria.desempilharEscopo();
                    memoria.adicionarSimbolo(vetor);
                    Simbolo simboloGlobal = memoria.isGlobal(simboloExistente) ? simboloExistente : vetor;
                    Simbolo simboloLocal = memoria.isGlobal(simboloExistente) ? vetor : simboloExistente;

                    notificarAviso(new AvisoSimboloGlobalOcultado(simboloGlobal, simboloLocal, noDeclaracaoVetor));
                }
            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                if (FUNCOES_RESERVADAS.contains(nome))
                {
                    vetor.setRedeclarado(true);
                    Funcao funcaoSistam = new Funcao(nome, TipoDado.VAZIO, Quantificador.VETOR, null, null);
                    notificarErroSemantico(new ErroSimboloRedeclarado(vetor, funcaoSistam));
                }
                else
                {
                    memoria.adicionarSimbolo(vetor);
                }
            }

            if (noDeclaracaoVetor.constante() && noDeclaracaoVetor.getInicializacao() == null)
            {
                NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                referencia.setTrechoCodigoFonteNome(noDeclaracaoVetor.getTrechoCodigoFonteNome());

                notificarErroSemantico(new ErroSimboloNaoInicializado(referencia, vetor));
            }

            if (noDeclaracaoVetor.getInicializacao() != null)
            {
                if (noDeclaracaoVetor.getInicializacao() instanceof NoVetor)
                {
                    NoExpressao inicializacao = noDeclaracaoVetor.getInicializacao();
                    NoReferenciaVariavel referencia = new NoReferenciaVariavel(null, nome);
                    referencia.setTrechoCodigoFonteNome(noDeclaracaoVetor.getTrechoCodigoFonteNome());
                    NoOperacao operacao = new NoOperacaoAtribuicao(referencia, inicializacao);

                    if (tamanho != null)
                    {
                        int numeroElementosDeclarados = ((NoVetor) inicializacao).getValores().size();

                        if (tamanho != numeroElementosDeclarados)
                        {
                            notificarErroSemantico(new ErroQuantidadeElementosInicializacaoVetor(noDeclaracaoVetor.getInicializacao().getTrechoCodigoFonte(), nome, tamanho, numeroElementosDeclarados));
                        }

                        if (noDeclaracaoVetor.constante())
                        {
                            NoVetor noVetor = (NoVetor) inicializacao;

                            for (int indice = 0; indice < noVetor.getValores().size(); indice++)
                            {
                                if (!(noVetor.getValores().get(indice) instanceof NoExpressaoLiteral))
                                {
                                    notificarErroSemantico(new ErroInicializacaoConstante(noDeclaracaoVetor, indice));
                                }
                            }
                        }
                    }

                    try
                    {
                        this.declarandoVetor = true;
                        operacao.aceitar(this);
                        this.declarandoVetor = false;
                    }
                    catch (ExcecaoVisitaASA excecao)
                    {
                        if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
                        {
                            throw excecao;
                        }
                    }
                }
                else
                {
                    if (tamanho == null)
                    {
                        tamanho = 0;
                    }

                    notificarErroSemantico(new ErroAoInicializarVetor(vetor, noDeclaracaoVetor.getInicializacao().getTrechoCodigoFonte(), tamanho));
                }
            }

            vetor.setConstante(noDeclaracaoVetor.constante());
        }

        return null;

    }

    @Override
    public Object visitar(NoEnquanto noEnquanto) throws ExcecaoVisitaASA
    {
        TipoDado tipoDadoCondicao = (TipoDado) noEnquanto.getCondicao().aceitar(this);

        if (tipoDadoCondicao != TipoDado.LOGICO)
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noEnquanto, tipoDadoCondicao));
        }

        analisarListaBlocos(noEnquanto.getBlocos());

        return null;
    }

    @Override
    public Object visitar(NoEscolha noEscolha) throws ExcecaoVisitaASA
    {
        tipoDadoEscolha.push((TipoDado) noEscolha.getExpressao().aceitar(this));

        if ((tipoDadoEscolha.peek() != TipoDado.INTEIRO) && (tipoDadoEscolha.peek() != TipoDado.CARACTER))
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noEscolha, tipoDadoEscolha.peek(), TipoDado.INTEIRO, TipoDado.CARACTER));
        }
        List<NoCaso> casos = noEscolha.getCasos();
        for (NoCaso noCaso : casos)
        {
            noCaso.aceitar(this);
        }
        
        tipoDadoEscolha.pop();
        
        return null;
    }

    @Override
    public Object visitar(NoFacaEnquanto noFacaEnquanto) throws ExcecaoVisitaASA
    {
        analisarListaBlocos(noFacaEnquanto.getBlocos());

        TipoDado tipoDadoCondicao = (TipoDado) noFacaEnquanto.getCondicao().aceitar(this);

        if (tipoDadoCondicao != TipoDado.LOGICO)
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noFacaEnquanto, tipoDadoCondicao));
        }

        return null;
    }

    @Override
    public Object visitar(NoInteiro noInteiro) throws ExcecaoVisitaASA
    {
        return TipoDado.INTEIRO;
    }

    @Override
    public Object visitar(NoLogico noLogico) throws ExcecaoVisitaASA
    {
        return TipoDado.LOGICO;
    }

    @Override
    public Object visitar(NoMatriz noMatriz) throws ExcecaoVisitaASA
    {
        List<List<Object>> valores = noMatriz.getValores();

        if (valores != null && !valores.isEmpty())
        {

            try
            {
                TipoDado tipoMatriz = (TipoDado) ((NoExpressao) valores.get(0).get(0)).aceitar(this);
                for (List<Object> valList : valores)
                {
                    for (int i = 0; i < valList.size(); i++)
                    {
                        TipoDado tipoDadoElemento = (TipoDado) ((NoExpressao) valList.get(i)).aceitar(this);

                        if (tipoMatriz != tipoDadoElemento)
                        {
                            notificarErroSemantico(new ErroDefinirTipoDadoMatrizLiteral(noMatriz.getTrechoCodigoFonte()));

                            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noMatriz);
                        }
                    }
                }
                return tipoMatriz;
            }
            catch (Exception excecao)
            {
                //excecao.printStackTrace(System.out);
                throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noMatriz);
            }

        }
        else
        {

            notificarErroSemantico(new ErroInicializacaoMatrizEmBranco(noMatriz.getTrechoCodigoFonte()));

            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noMatriz);
        }
    }

    @Override
    public Object visitar(NoMenosUnario noMenosUnario) throws ExcecaoVisitaASA
    {
        TipoDado tipo = (TipoDado) noMenosUnario.getExpressao().aceitar(this);
        if (!tipo.equals(TipoDado.INTEIRO) && !tipo.equals(TipoDado.REAL))
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noMenosUnario, tipo, TipoDado.INTEIRO, TipoDado.REAL));
            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noMenosUnario);
        }

        return tipo;
    }

    @Override
    public Object visitar(NoNao noNao) throws ExcecaoVisitaASA
    {
        TipoDado tipo = (TipoDado) noNao.getExpressao().aceitar(this);
        if (tipo != TipoDado.LOGICO)
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noNao, tipo, TipoDado.LOGICO));
            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noNao);
        }
        return tipo;
    }

    @Override
    public Object visitar(NoOperacaoLogicaIgualdade noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaDiferenca noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(final NoOperacaoAtribuicao noOperacao) throws ExcecaoVisitaASA
    {
        TipoDado tipoDadoRetorno;

        TipoDado operandoEsquerdo = null;
        TipoDado operandoDireito = null;

        Simbolo simbolo = null;
        boolean inicializadoAnterior = false;
        if (!(noOperacao.getOperandoEsquerdo() instanceof NoReferencia))
        {
            notificarErroSemantico(new ErroAtribuirEmExpressao(noOperacao, noOperacao.getOperandoEsquerdo()));
        }
        else
        {
            try
            {
                if (noOperacao.getOperandoEsquerdo() instanceof NoReferenciaVariavel)
                {
                    final NoReferenciaVariavel referencia = (NoReferenciaVariavel) noOperacao.getOperandoEsquerdo();

                    if (referencia.getEscopo() == null)
                    {
                        simbolo = memoria.getSimbolo(referencia.getNome());

                        inicializadoAnterior = simbolo.inicializado();
                        simbolo.setInicializado(true);
                        if (simbolo instanceof Variavel)
                        {

                            if (simbolo.constante())
                            {
                                final Simbolo pSimbolo = simbolo;
                                notificarErroSemantico(new ErroAtribuirEmConstante(noOperacao.getOperandoEsquerdo().getTrechoCodigoFonte(), pSimbolo));
                            }

                            if ((noOperacao.getOperandoDireito() instanceof NoMatriz)
                                    || (noOperacao.getOperandoDireito() instanceof NoVetor))
                            {
                                notificarErroSemantico(new ErroAtribuirMatrizVetorEmVariavel(noOperacao.getOperandoDireito().getTrechoCodigoFonte()));
                            }
                        }
                        else if (simbolo instanceof Vetor)
                        {
                            if (!(noOperacao.getOperandoDireito() instanceof NoVetor))
                            {
                                if (declarandoVetor)
                                {
                                    notificarErroSemantico(new ErroAoInicializarVetor((Vetor) simbolo, noOperacao.getOperandoDireito().getTrechoCodigoFonte(), ((Vetor) simbolo).getTamanho()));
                                }
                            }
                        }
                        else if (simbolo instanceof Matriz)
                        {
                            if (!simbolo.inicializado() && !(noOperacao.getOperandoDireito() instanceof NoMatriz))
                            {
                                notificarErroSemantico(new ErroAoInicializarMatriz((Matriz) simbolo, noOperacao.getOperandoDireito().getTrechoCodigoFonte(), ((Matriz) simbolo).getNumeroLinhas(), ((Matriz) simbolo).getNumeroColunas()));
                            }
                        }
                    }
                    else
                    {
                        /* O escopo pode retornar o nome real da biblioteca ou o alias que o usuário definiu.
                         *                          * 
                         * Como o alias é dinâmico, o gerenciador de bibliotecas não consegue recuperar a
                         * biblioteca a partir dele. Por isso, o método obterMetaDadosBiblioteca() só pode
                         * ser utilizado com o nome real da biblioteca.
                         * 
                         * Para resolver isto, o semântico faz um mapeamento interno das biblitecas. Ao incluir 
                         * a biblioteca ele cria uma chave no mapa, tanto para o nome real da biblioteca, quanto
                         * para o alias do usuário.
                         * 
                         * Por isso, ao trabalhar com as bibliotecas dentro do semântico, deve-se sempre utilizar
                         * o mapa interno, caso contrário vai dar NullPointerException.
                         */
                        final MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(referencia.getEscopo());
                        final MetaDadosConstante metaDadosConstante = metaDadosBiblioteca.getMetaDadosConstantes().obter(referencia.getNome());

                        notificarErroSemantico(new ErroAtribuirConstanteBiblioteca(noOperacao.getOperandoEsquerdo().getTrechoCodigoFonte(), metaDadosConstante, metaDadosBiblioteca));
                    }
                }
                else if (noOperacao.getOperandoEsquerdo() instanceof NoReferenciaMatriz
                        || noOperacao.getOperandoEsquerdo() instanceof NoReferenciaVetor)
                {
                    simbolo = memoria.getSimbolo(((NoReferencia) noOperacao.getOperandoEsquerdo()).getNome());
                    if (simbolo.constante())
                    {
                        final Simbolo pSimbolo = simbolo;
                        notificarErroSemantico(new ErroAtribuirEmConstante(noOperacao.getTrechoCodigoFonte(), pSimbolo));
                    }

                    if (noOperacao.getOperandoDireito() instanceof NoVetor)
                    {
                        notificarErroSemantico(new ErroAoAtribuirEmVetor(noOperacao.getTrechoCodigoFonte()));
                    }
                    else if (noOperacao.getOperandoDireito() instanceof NoMatriz)
                    {
                        notificarErroSemantico(new ErroAoAtribuirEmMatriz(noOperacao.getOperandoDireito().getTrechoCodigoFonte()));
                    }
                }
                else if (noOperacao.getOperandoEsquerdo() instanceof NoChamadaFuncao)
                {
                    notificarErroSemantico(new ErroAtribuirEmChamadaFuncao(noOperacao.getTrechoCodigoFonte()));
                }
            }
            catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
            {
                // Não faz nada
            }
        }

        try
        {
            operandoEsquerdo = (TipoDado) noOperacao.getOperandoEsquerdo().aceitar(this);
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        if (simbolo != null)
        {
            simbolo.setInicializado(inicializadoAnterior);
        }

        try
        {
            operandoDireito = (TipoDado) noOperacao.getOperandoDireito().aceitar(this);
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        if (operandoEsquerdo != null && operandoDireito != null)
        {
            try
            {
                tipoDadoRetorno = tabelaCompatibilidadeTipos.obterTipoRetornoOperacao(noOperacao.getClass(), operandoEsquerdo, operandoDireito);
            }
            catch (ExcecaoValorSeraConvertido excecao)
            {
                notificarAviso(new AvisoValorExpressaoSeraConvertido(noOperacao.getOperandoDireito().getTrechoCodigoFonte(), noOperacao, excecao.getTipoEntrada(), excecao.getTipoSaida()));

                tipoDadoRetorno = excecao.getTipoSaida();
            }
            catch (ExcecaoImpossivelDeterminarTipoDado excecao)
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noOperacao, operandoEsquerdo, operandoDireito));

                throw new ExcecaoVisitaASA(excecao, asa, noOperacao);
            }
        }
        else
        {
            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noOperacao);
        }

        if (simbolo != null)
        {
            simbolo.setInicializado(true);
        }

        return tipoDadoRetorno;
    }

    @Override
    public Object visitar(NoOperacaoLogicaE noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaOU noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaMaior noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaMaiorIgual noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaMenor noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoLogicaMenorIgual noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoSoma noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoSubtracao noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoDivisao noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoMultiplicacao noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoModulo noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoBitwiseLeftShift noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoBitwiseRightShift noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoBitwiseE noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoBitwiseOu noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoOperacaoBitwiseXOR noOperacao) throws ExcecaoVisitaASA
    {
        return recuperaTipoNoOperacao(noOperacao);
    }

    @Override
    public Object visitar(NoBitwiseNao noOperacaoBitwiseNao) throws ExcecaoVisitaASA
    {
        TipoDado tipo = (TipoDado) noOperacaoBitwiseNao.getExpressao().aceitar(this);
        if (tipo != TipoDado.INTEIRO)
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noOperacaoBitwiseNao, tipo, TipoDado.LOGICO));
            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noOperacaoBitwiseNao);
        }
        return tipo;
    }

    @Override
    public Object visitar(NoPara noPara) throws ExcecaoVisitaASA
    {
        memoria.empilharEscopo();

        try
        {
            if (noPara.getInicializacoes() != null)
            {
                for (NoBloco inicializacao : noPara.getInicializacoes())
                {
                    inicializacao.aceitar(this);
                }
            }
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        try
        {
            if (noPara.getCondicao() == null)
            {
                notificarErroSemantico(new ErroParaSemExpressaoComparacao(noPara.getTrechoCodigoFonte()));
            }
            else
            {
                TipoDado tipoDadoCondicao = (TipoDado) noPara.getCondicao().aceitar(this);

                if (tipoDadoCondicao != TipoDado.LOGICO)
                {
                    notificarErroSemantico(new ErroTiposIncompativeis(noPara, tipoDadoCondicao));
                }
            }
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        try
        {
            if (noPara.getIncremento() != null)
            {
                noPara.getIncremento().aceitar(this);
            }
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        analisarListaBlocos(noPara.getBlocos());

        memoria.desempilharEscopo();

        return null;
    }

    @Override
    public Object visitar(NoPare noPare) throws ExcecaoVisitaASA
    {
        return null;
    }

    @Override
    public Object visitar(NoReal noReal) throws ExcecaoVisitaASA
    {
        return TipoDado.REAL;
    }

    @Override
    public Object visitar(NoReferenciaMatriz noReferenciaMatriz) throws ExcecaoVisitaASA
    {
        try
        {
            TipoDado tipoLinha = (TipoDado) noReferenciaMatriz.getLinha().aceitar(this);
            TipoDado tipoColuna = (TipoDado) noReferenciaMatriz.getColuna().aceitar(this);

            if (tipoLinha != TipoDado.INTEIRO || tipoColuna != TipoDado.INTEIRO)
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noReferenciaMatriz, tipoLinha, TipoDado.INTEIRO, tipoColuna, TipoDado.INTEIRO));
            }
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        try
        {
            Simbolo simbolo = memoria.getSimbolo(noReferenciaMatriz.getNome());

            if (!(simbolo instanceof Matriz))
            {
                notificarErroSemantico(new ErroReferenciaInvalida(noReferenciaMatriz, simbolo));
            }
            else
            {
                ((Matriz) simbolo).getOrigemDoSimbolo().adicionarReferencia(noReferenciaMatriz);
            }

            return simbolo.getTipoDado();
        }
        catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
        {
            notificarErroSemantico(new ErroSimboloNaoDeclarado(noReferenciaMatriz));
        }

        throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noReferenciaMatriz);
    }

    @Override
    public Object visitar(NoReferenciaVariavel noReferenciaVariavel) throws ExcecaoVisitaASA
    {
        if (noReferenciaVariavel.getEscopo() == null)
        {
            try
            {
                return analisarReferenciaVariavelPrograma(noReferenciaVariavel);
            }
            catch (ExcecaoImpossivelDeterminarTipoDado ex)
            {
                throw new ExcecaoVisitaASA(ex, asa, noReferenciaVariavel);
            }
        }
        else
        {
            return analisarReferenciaVariavelBiblioteca(noReferenciaVariavel);
        }
    }

    @Override
    public Object visitar(NoReferenciaVetor noReferenciaVetor) throws ExcecaoVisitaASA
    {
        try
        {
            TipoDado tipoIndice = (TipoDado) noReferenciaVetor.getIndice().aceitar(this);

            if (tipoIndice != TipoDado.INTEIRO)
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noReferenciaVetor, tipoIndice, TipoDado.INTEIRO));
            }
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        try
        {
            Simbolo simbolo = memoria.getSimbolo(noReferenciaVetor.getNome());

            if (!(simbolo instanceof Vetor))
            {
                notificarErroSemantico(new ErroReferenciaInvalida(noReferenciaVetor, simbolo));
            }
            else
            {
                ((Vetor) simbolo).getOrigemDoSimbolo().adicionarReferencia(noReferenciaVetor);
            }

            return simbolo.getTipoDado();
        }
        catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
        {
            notificarErroSemantico(new ErroSimboloNaoDeclarado(noReferenciaVetor));
        }

        throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noReferenciaVetor);
    }

    @Override
    public Object visitar(NoRetorne noRetorne) throws ExcecaoVisitaASA
    {
        TipoDado tipoRetornoFuncao = TipoDado.VAZIO;

        if (noRetorne.getExpressao() != null)
        {
            TipoDado tipoExpressaoRetorno = TipoDado.VAZIO;

            try
            {
                noRetorne.setPai(funcaoAtual.getOrigemDoSimbolo());
                tipoExpressaoRetorno = (TipoDado) noRetorne.getExpressao().aceitar(this);
                tipoRetornoFuncao = tabelaCompatibilidadeTipos.obterTipoRetornoFuncao(funcaoAtual.getTipoDado(), tipoExpressaoRetorno);
            }
            catch (ExcecaoValorSeraConvertido e)
            {
                notificarAviso(new AvisoValorExpressaoSeraConvertido(noRetorne, e.getTipoEntrada(), e.getTipoSaida(), funcaoAtual.getNome()));
                tipoRetornoFuncao = e.getTipoSaida();
            }
            catch (ExcecaoImpossivelDeterminarTipoDado ex)
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noRetorne, new String[]
                {
                    funcaoAtual.getNome()
                }, funcaoAtual.getTipoDado(), tipoExpressaoRetorno));
                throw new ExcecaoVisitaASA(ex, asa, noRetorne);
            }
        }else{
            if(tipoRetornoFuncao != funcaoAtual.getOrigemDoSimbolo().getTipoDado()){
                notificarErroSemantico(new ErroTiposIncompativeis(noRetorne, new String[]
                {
                    funcaoAtual.getNome()
                }, funcaoAtual.getTipoDado(), TipoDado.VAZIO));
            }
        }

        return tipoRetornoFuncao;
    }

    @Override
    public Object visitar(NoSe noSe) throws ExcecaoVisitaASA
    {
        TipoDado tipoDadoCondicao = (TipoDado) noSe.getCondicao().aceitar(this);

        if (tipoDadoCondicao != TipoDado.LOGICO)
        {
            notificarErroSemantico(new ErroTiposIncompativeis(noSe, tipoDadoCondicao));
        }

        analisarListaBlocos(noSe.getBlocosVerdadeiros());
        analisarListaBlocos(noSe.getBlocosFalsos());

        return null;
    }

    @Override
    public Object visitar(NoVetor noVetor) throws ExcecaoVisitaASA
    {
        List<NoExpressao> valores = (List) noVetor.getValores();

        if (valores != null && !valores.isEmpty())
        {
            try
            {
                TipoDado tipoDadoVetor = (TipoDado) ((NoExpressao) valores.get(0)).aceitar(this);

                for (int i = 1; i < valores.size(); i++)
                {
                    TipoDado tipoDadoElemento = (TipoDado) ((NoExpressao) valores.get(i)).aceitar(this);

                    if (tipoDadoElemento != tipoDadoVetor)
                    {
                        notificarErroSemantico(new ErroDefinirTipoDadoVetorLiteral(noVetor.getTrechoCodigoFonte()));

                        throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noVetor);
                    }
                }
                return tipoDadoVetor;
            }
            catch (Exception excecao)
            {
                //excecao.printStackTrace(System.out);
                throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noVetor);
            }
        }
        else
        {
            //TODO Fazer essa verificaçao no Sintatico (Portugol.g)
            notificarErroSemantico(new ErroVetorSemElementos(noVetor.getTrechoCodigoFonte()));

            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noVetor);
        }
    }

    @Override
    public Object visitar(NoDeclaracaoParametro noDeclaracaoParametro) throws ExcecaoVisitaASA
    {
        switch (noDeclaracaoParametro.getQuantificador())
        {
            case VALOR:
                noDeclaracaoParametro.setIdParaInspecao(totalVariaveisDeclaradas);
                totalVariaveisDeclaradas++;
                break;
            case VETOR:
                noDeclaracaoParametro.setIdParaInspecao(totalVetoresDeclarados);
                totalVetoresDeclarados++;
                break;
            case MATRIZ:
                noDeclaracaoParametro.setIdParaInspecao(totalMatrizesDeclaradas);
                totalMatrizesDeclaradas++;
                break;
        }
        
        String nome = noDeclaracaoParametro.getNome();
        TipoDado tipoDado = noDeclaracaoParametro.getTipoDado();
        Quantificador quantificador = noDeclaracaoParametro.getQuantificador();
        Simbolo simbolo = null;

        if (quantificador == Quantificador.VALOR)
        {
            simbolo = new Variavel(nome, tipoDado, noDeclaracaoParametro);
        }
        else if (quantificador == Quantificador.VETOR)
        {
            simbolo = new Vetor(nome, tipoDado, noDeclaracaoParametro);
        }
        else if (quantificador == Quantificador.MATRIZ)
        {
            simbolo = new Matriz(nome, tipoDado, noDeclaracaoParametro, 0, 0, new ArrayList<List<Object>>());
        }

        try
        {
            Simbolo simboloExistente = memoria.getSimbolo(nome);

            final boolean global = memoria.isGlobal(simboloExistente);
            final boolean local = memoria.isLocal(simboloExistente);

            memoria.empilharEscopo();
            memoria.adicionarSimbolo(simbolo);

            final boolean global1 = memoria.isGlobal(simbolo);
            final boolean local1 = memoria.isLocal(simbolo);

            if ((global && global1) || (local && local1))
            {
                simbolo.setRedeclarado(true);
                notificarErroSemantico(new ErroParametroRedeclarado(noDeclaracaoParametro, funcaoAtual));
                memoria.desempilharEscopo();
            }
            else
            {
                memoria.desempilharEscopo();
                memoria.adicionarSimbolo(simbolo);
                simbolo.setInicializado(true);

                Simbolo simboloGlobal = memoria.isGlobal(simboloExistente) ? simboloExistente : simbolo;
                Simbolo simboloLocal = memoria.isGlobal(simboloExistente) ? simbolo : simboloExistente;

                notificarAviso(new AvisoSimboloGlobalOcultado(simboloGlobal, simboloLocal, noDeclaracaoParametro));
            }
        }
        catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
        {
            simbolo.setInicializado(true);
            memoria.adicionarSimbolo(simbolo);
        }

        return null;
    }

    private static List<String> getLista()
    {
        List<String> funcoes = new ArrayList<>();

        funcoes.add(FUNCAO_LEIA);
        funcoes.add(FUNCAO_ESCREVA);
        funcoes.add(FUNCAO_LIMPA);

        return funcoes;
    }

    private TipoDado recuperaTipoNoOperacao(NoOperacao noOperacao) throws ExcecaoVisitaASA
    {
        TipoDado operandoEsquerdo = null;
        TipoDado operandoDireito = null;

        try
        {
            operandoEsquerdo = (TipoDado) noOperacao.getOperandoEsquerdo().aceitar(this);
            //noOperacao.getOperandoEsquerdo().setTipoResultante(operandoEsquerdo);
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        try
        {
            operandoDireito = (TipoDado) noOperacao.getOperandoDireito().aceitar(this);
            //noOperacao.getOperandoDireito().setTipoResultante(operandoDireito);
        }
        catch (ExcecaoVisitaASA excecao)
        {
            if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
            {
                throw excecao;
            }
        }

        if (operandoEsquerdo != null && operandoDireito != null)
        {
            try
            {
                return tabelaCompatibilidadeTipos.obterTipoRetornoOperacao(noOperacao.getClass(), operandoEsquerdo, operandoDireito);
            }
            catch (ExcecaoValorSeraConvertido excecao)
            {
                notificarAviso(new AvisoValorExpressaoSeraConvertido(noOperacao, excecao.getTipoEntrada(), excecao.getTipoSaida()));

                return excecao.getTipoSaida();
            }
            catch (ExcecaoImpossivelDeterminarTipoDado excecao)
            {
                notificarErroSemantico(new ErroTiposIncompativeis(noOperacao, operandoEsquerdo, operandoDireito));

                throw new ExcecaoVisitaASA(excecao, asa, noOperacao);
            }
        }
        else
        {
            throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noOperacao);
        }
    }

    private void analisarListaBlocos(List<NoBloco> blocos) throws ExcecaoVisitaASA
    {
        if (blocos == null)
        {
            return;
        }

        memoria.empilharEscopo();

        for (NoBloco noBloco : blocos)
        {
            try
            {
                if (!blocoValido(noBloco))
                {
                    notificarErroSemantico(new ErroBlocoInvalido(noBloco));
                }

                noBloco.aceitar(this);
            }
            catch (ExcecaoVisitaASA excecao)
            {
                if (!(excecao.getCause() instanceof ExcecaoImpossivelDeterminarTipoDado))
                {
                    throw excecao;
                }
            }
        }

        memoria.desempilharEscopo();
    }

    private boolean blocoValido(NoBloco bloco)
    {
        Class classeBloco = bloco.getClass();
        Class<? extends NoBloco>[] classesPermitidas = new Class[]
        {
            NoDeclaracaoVariavel.class, NoDeclaracaoVetor.class, NoDeclaracaoMatriz.class,
            NoCaso.class, NoEnquanto.class, NoEscolha.class, NoFacaEnquanto.class, NoPara.class, NoSe.class,
            NoPare.class, NoRetorne.class, NoTitulo.class, NoVaPara.class,
            NoOperacaoAtribuicao.class, NoChamadaFuncao.class
        };

        for (Class classe : classesPermitidas)
        {
            if (classe.isAssignableFrom(classeBloco))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object visitar(NoInclusaoBiblioteca noInclusaoBiblioteca) throws ExcecaoVisitaASA
    {
        String nome = noInclusaoBiblioteca.getNome();
        String alias = noInclusaoBiblioteca.getAlias();

        try
        {
            try
            {
                memoria.getSimbolo(nome);
                notificarErroSemantico(new ErroInclusaoBiblioteca(noInclusaoBiblioteca.getTrechoCodigoFonteNome(), new Exception(String.format("o identificador \"%s\" já está sendo utilizado", nome))));
            }
            catch (ExcecaoSimboloNaoDeclarado excecao)
            {
                MetaDadosBiblioteca metaDadosBiblioteca = GerenciadorBibliotecas.getInstance().obterMetaDadosBiblioteca(nome);

                if (metaDadosBibliotecas.containsKey(nome))
                {
                    notificarErroSemantico(new ErroInclusaoBiblioteca(noInclusaoBiblioteca.getTrechoCodigoFonteNome(), new Exception(String.format("A biblioteca \"%s\" já foi incluída", nome))));
                }
                else
                {
                    metaDadosBibliotecas.put(nome, metaDadosBiblioteca);
                }

                if (alias != null)
                {
                    try
                    {
                        memoria.getSimbolo(nome);
                        notificarErroSemantico(new ErroInclusaoBiblioteca(noInclusaoBiblioteca.getTrechoCodigoFonteNome(), new Exception(String.format("o identificador \"%s\" já está sendo utilizado", nome))));
                    }
                    catch (ExcecaoSimboloNaoDeclarado excecao2)
                    {
                        if (metaDadosBibliotecas.containsKey(alias))
                        {
                            notificarErroSemantico(new ErroInclusaoBiblioteca(noInclusaoBiblioteca.getTrechoCodigoFonteAlias(), new Exception(String.format("O alias \"%s\" já está sendo utilizado pela biblioteca \"%s\"", alias, metaDadosBibliotecas.get(alias).getNome()))));
                        }
                        else
                        {
                            metaDadosBibliotecas.put(alias, metaDadosBiblioteca);
                        }
                    }
                }
            }
        }
        catch (ErroCarregamentoBiblioteca erro)
        {
            notificarErroSemantico(new ErroInclusaoBiblioteca(noInclusaoBiblioteca.getTrechoCodigoFonteNome(), erro));
        }

        return null;
    }

    private TipoDado analisarReferenciaVariavelPrograma(NoReferenciaVariavel noReferenciaVariavel) throws ExcecaoImpossivelDeterminarTipoDado
    {
        try
        {
            Simbolo simbolo = memoria.getSimbolo(noReferenciaVariavel.getNome());

            if (!simbolo.inicializado())
            {
                notificarErroSemantico(new ErroSimboloNaoInicializado(noReferenciaVariavel, simbolo));
            }

            if (!(simbolo instanceof Variavel) && !declarandoVetor && !declarandoMatriz && !passandoReferencia && !passandoParametro)
            {
                notificarErroSemantico(new ErroReferenciaInvalida(noReferenciaVariavel, simbolo));
            }
            else if (simbolo instanceof Variavel)
            {
                ((Variavel) simbolo).getOrigemDoSimbolo().adicionarReferencia(noReferenciaVariavel);
            }
            else if (simbolo instanceof Vetor)
            {
                simbolo.getOrigemDoSimbolo().adicionarReferencia(noReferenciaVariavel);
            }
            else if (simbolo instanceof Matriz)
            {
                simbolo.getOrigemDoSimbolo().adicionarReferencia(noReferenciaVariavel);
            }
            
            return simbolo.getTipoDado();
        }
        catch (ExcecaoSimboloNaoDeclarado excecaoSimboloNaoDeclarado)
        {
            notificarErroSemantico(new ErroSimboloNaoDeclarado(noReferenciaVariavel));
        }

        throw new ExcecaoImpossivelDeterminarTipoDado();
    }

    private TipoDado analisarReferenciaVariavelBiblioteca(NoReferenciaVariavel noReferenciaVariavel) throws ExcecaoVisitaASA
    {
        final String escopo = noReferenciaVariavel.getEscopo();
        final String nome = noReferenciaVariavel.getNome();
        final MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(escopo);

        if (metaDadosBiblioteca != null)
        {
            MetaDadosConstante metaDadosConstante = metaDadosBiblioteca.getMetaDadosConstantes().obter(nome);

            if (metaDadosConstante != null)
            {
                noReferenciaVariavel.setVariavelDeBiblioteca(true);
                noReferenciaVariavel.setTipoBiblioteca(metaDadosConstante.getTipoDado());
                
                return metaDadosConstante.getTipoDado();
            }

            notificarErroSemantico(new ErroConstanteNaoEncontradaNaBiblioteca(noReferenciaVariavel.getTrechoCodigoFonteNome(), nome, metaDadosBiblioteca));
        }
        else
        {
            notificarErroSemantico(new ErroBibliotecaNaoInserida(noReferenciaVariavel.getTrechoCodigoFonteNome(), escopo));
        }

        throw new ExcecaoVisitaASA(new ExcecaoImpossivelDeterminarTipoDado(), asa, noReferenciaVariavel);
    }

    private Integer obterTamanhoVetorMatriz(final NoExpressao expTamanho, NoDeclaracao noDeclaracao) throws ExcecaoVisitaASA
    {
        if (expTamanho != null)
        {
            TipoDado tipoTamanho = (TipoDado) expTamanho.aceitar(this);

            if (tipoTamanho == TipoDado.INTEIRO)
            {
                if (!(expTamanho instanceof NoInteiro) && !(expTamanho instanceof NoReferenciaVariavel))
                {
                    notificarErroSemantico(new ErroTamanhoVetorMatriz(noDeclaracao, expTamanho));
                }
                else if (expTamanho instanceof NoReferenciaVariavel)
                {
                    NoReferenciaVariavel ref = (NoReferenciaVariavel) expTamanho;

                    if (ref.getEscopo() == null)
                    {
                        try
                        {
                            Variavel variavel = (Variavel) memoria.getSimbolo(ref.getNome());
                            NoDeclaracaoVariavel decl = (NoDeclaracaoVariavel) variavel.getOrigemDoSimbolo();

                            if (variavel.constante())
                            {

                                return ((NoInteiro) decl.getInicializacao()).getValor();
                            }
                            else
                            {
                                notificarErroSemantico(new ErroTamanhoVetorMatriz(noDeclaracao, expTamanho));
                            }
                        }
                        catch (ExcecaoSimboloNaoDeclarado | ClassCastException ex)
                        {
                            // Não faz nada. Já notificou simbolo inexistente
                            // Não faz nada. Já notificou uso indevido
                        }
                    }
                    else
                    {
                        MetaDadosBiblioteca metaDadosBiblioteca = metaDadosBibliotecas.get(ref.getEscopo());
                        MetaDadosConstantes metaDadosConstantes = metaDadosBiblioteca.getMetaDadosConstantes();
                        MetaDadosConstante metaDadosConstante = metaDadosConstantes.obter(ref.getNome());

                        if (metaDadosConstante.getTipoDado() == TipoDado.INTEIRO)
                        {
                            return (Integer) metaDadosConstante.getValor();
                        }
                        else
                        {
                            notificarErroSemantico(new ErroTamanhoVetorMatriz(noDeclaracao, expTamanho));
                        }
                    }
                }
                else
                {
                    return ((NoInteiro) expTamanho).getValor();
                }
            }
            else
            {
                notificarErroSemantico(new ErroTamanhoVetorMatriz(noDeclaracao, expTamanho));
            }
        }

        return null;
    }

    @Override
    public Object visitar(NoContinue noContinue) throws ExcecaoVisitaASA
    {
        throw new ExcecaoVisitaASA("Erro", new ErroComandoNaoSuportado(noContinue.getTrechoCodigoFonte()), asa, noContinue);
    }

    @Override
    public Object visitar(NoTitulo noTitulo) throws ExcecaoVisitaASA
    {
        throw new ExcecaoVisitaASA("Erro", new ErroComandoNaoSuportado(noTitulo.getTrechoCodigoFonte()), asa, noTitulo);
    }

    @Override
    public Object visitar(NoVaPara noVaPara) throws ExcecaoVisitaASA
    {
        throw new ExcecaoVisitaASA("Erro", new ErroComandoNaoSuportado(noVaPara.getTrechoCodigoFonte()), asa, noVaPara);
    }
}
