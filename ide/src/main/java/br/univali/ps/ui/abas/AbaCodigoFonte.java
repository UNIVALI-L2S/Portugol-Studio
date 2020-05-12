package br.univali.ps.ui.abas;

import br.univali.portugol.nucleo.programa.Estado;
import br.univali.ps.ui.swing.ColorController;
import br.univali.ps.ui.swing.Themeable;
import br.univali.ps.ui.utils.FabricaDeFileChooser;
import br.univali.ps.ui.utils.FabricaDicasInterface;
import br.univali.ps.ui.paineis.PainelSaida;
import br.univali.ps.ui.telas.TelaRenomearSimbolo;
import br.univali.portugol.nucleo.ErroAoRenomearSimbolo;
import br.univali.ps.ui.rstautil.ProcuradorDeDeclaracao;
import br.univali.portugol.nucleo.ErroCompilacao;
import br.univali.portugol.nucleo.Portugol;
import br.univali.portugol.nucleo.programa.Programa;
import br.univali.portugol.nucleo.analise.ResultadoAnalise;
import br.univali.portugol.nucleo.analise.sintatica.erros.ErroExpressoesForaEscopoPrograma;
import br.univali.portugol.nucleo.asa.ASAPrograma;
import br.univali.portugol.nucleo.asa.ExcecaoVisitaASA;
import br.univali.portugol.nucleo.asa.NoDeclaracaoBase;
import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.portugol.nucleo.asa.TrechoCodigoFonte;
import br.univali.portugol.nucleo.bibliotecas.base.Biblioteca;
import br.univali.portugol.nucleo.bibliotecas.base.ErroCarregamentoBiblioteca;
import br.univali.portugol.nucleo.execucao.ModoEncerramento;
import br.univali.portugol.nucleo.execucao.ObservadorExecucaoBasico;
import br.univali.portugol.nucleo.execucao.ResultadoExecucao;
import br.univali.portugol.nucleo.mensagens.ErroSintatico;
import br.univali.ps.dominio.PortugolDocumento;
import br.univali.ps.dominio.PortugolDocumentoListener;
import br.univali.ps.nucleo.Caminhos;
import br.univali.ps.nucleo.PortugolStudio;
import br.univali.ps.plugins.base.Plugin;
import br.univali.ps.plugins.base.UtilizadorPlugins;
import br.univali.ps.nucleo.Configuracoes;
import br.univali.ps.nucleo.ExcecaoAplicacao;
import br.univali.ps.plugins.base.ErroInstalacaoPlugin;
import br.univali.ps.plugins.base.GerenciadorPlugins;
import br.univali.ps.ui.Lancador;
import br.univali.ps.ui.editor.Editor;
import br.univali.ps.ui.editor.Utils;
import br.univali.ps.ui.inspetor.InspetorDeSimbolos;
import br.univali.ps.ui.paineis.PSPainelTabulado;
import br.univali.ps.ui.paineis.PainelConfigPlugins;
import br.univali.ps.ui.paineis.PainelTabuladoPrincipal;
import br.univali.ps.ui.abas.utilitarios.AbaGerenciadorBibliotecas;
import br.univali.ps.ui.abas.utilitarios.AbaGerenciadorTemplates;
import br.univali.ps.ui.editor.MiniBarraDeFerramentas;
import br.univali.ps.ui.rstautil.PortugolParser;
import br.univali.ps.ui.rstautil.tree.filters.DataTypeFilter;
import br.univali.ps.ui.rstautil.tree.filters.SymbolTypeFilter;
import br.univali.ps.ui.rstautil.tree.filters.view.DataTypeFilterView;
import br.univali.ps.ui.rstautil.tree.filters.view.SymbolTypeFilterView;
import br.univali.ps.ui.swing.filtros.FiltroArquivo;
import br.univali.ps.ui.utils.FileHandle;
import br.univali.ps.ui.utils.IconFactory;
import br.univali.ps.ui.swing.weblaf.BarraDeBotoesExpansivel;
import br.univali.ps.ui.swing.weblaf.WeblafUtils;
import br.univali.ps.ui.swing.weblaf.jOptionPane.QuestionDialog;
import br.univali.ps.ui.telas.TelaPrincipal;
import com.alee.laf.button.WebButton;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import com.alee.laf.scroll.WebScrollPaneUI;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public final class AbaCodigoFonte extends Aba implements PortugolDocumentoListener, AbaListener, PropertyChangeListener, ChangeListener, UtilizadorPlugins, Themeable {

    private static final Logger LOGGER = Logger.getLogger(AbaCodigoFonte.class.getName());
    private static final String TEMPLATE_ALGORITMO = carregarTemplate();

    private static final int TAMANHO_POOL_ABAS = 1;
    private static PoolAbasCodigoFonte poolAbasCodigoFonte;
    public static final float VALOR_INCREMENTO_FONTE = 2.0f;
    public static final float TAMANHO_MAXIMO_FONTE = 25.0f;
    public static final float TAMANHO_MINIMO_FONTE = 10.0f;

    private static final Icon lampadaAcesa = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "light_pix.png");
    private static final Icon lampadaApagada = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "light_pix_off.png");

    //private final Map<Plugin, JToggleButton> botoesPlugins = new HashMap<>();
    private final Map<Action, JButton> mapaBotoesAcoesPlugins = new HashMap<>();

    private Programa programaAnalisado; // compilado somente para análise
    private Programa programaCompilado; // compilado para execução
    private Future<Programa> tarefaCompilacao;

    private boolean isPrimeiroRecuperavel = false;
    private boolean podeSalvar = false;
    private boolean usuarioCancelouSalvamento = false;
    private boolean depurando = false;

    private JPanel painelTemporario;

    private Action acaoSalvarArquivo;
    private Action acaoSalvarComo;

    private FiltroArquivo filtroPrograma;
    //private JFileChooser dialogoSelecaoArquivo;//usando FileDialog ao invés de JFileChooser para evitar os problemas com look and feel

    private Action acaoExecutarPontoParada;
    private Action acaoExecutarPasso;
    private Action acaoInterromper;
    private Action acaoExibirOpcoesExecucao;
    
    private Action acaoAbrirArquivo;
    private Action acaoAbrirAjuda;

    //private Action acaoAumentarFonteArvore;
    //private Action acaoDiminuirFonteArvore;
    private boolean simbolosInspecionadosJaForamCarregados = false;//controla se os símbolos inspecionados já foram carregados do arquivo
    private String codigoFonteAtual;
    private static boolean desativouRecuperados = false;
    private boolean redimensionouParaBaixaResolucao = false;
    private static int numeroDocumento = 1;

    private final ExecutorService service = Executors.newSingleThreadExecutor(); // usando apenas uma thread, todas as compilações serão enfileiradas

    private IndicadorDeProgresso indicadorProgresso;

    private PainelConfigPlugins painelConfigPlugins;
    private AbaGerenciadorBibliotecas painelGerenciadorBibliotecas;
    private AbaGerenciadorTemplates painelGerenciadorTemplates;
    private PSPainelTabulado painelTabuladoUtilitarios;

    protected AbaCodigoFonte() {
        super("Sem título" + numeroDocumento, lampadaApagada, true);
        initComponents();
        configurarArvoreEstrutural();
        criarPainelTemporario();
        criarPainelPlugin();
        carregarConfiguracoes();
        configurarResolucao();
        configurarAcoes();
        configurarBarraDeBotoesDoEditor();
        configuraBarraDeBotoesDoPainelArvoreInspetor();
        instalarObservadores();
        configurarCursorBotoes();
        //carregarAlgoritmoPadrao();
        criarDicasInterface();
        painelRecuperados.setVisible(false);
        miniBarra.setVisible(false);
        painelTemplate.setVisible(false);
        botoesPlugin.setVisible(false);
        painelSaida.getConsole().setAbaCodigoFonte(AbaCodigoFonte.this);
        painelConfigPlugins.setAbaCodigoFonte(AbaCodigoFonte.this);
        inspetorDeSimbolos.setTextArea(editor.getTextArea());
        configurarCores();
        configuraLoader();
        configurarBotaoPlugin();
        painelTabuladoUtilitarios = new PSPainelTabulado();
        
        painelGerenciadorBibliotecas = new AbaGerenciadorBibliotecas(AbaCodigoFonte.this);
        painelGerenciadorTemplates = new AbaGerenciadorTemplates(AbaCodigoFonte.this);
        
        painelTemplate.add(painelTabuladoUtilitarios, BorderLayout.CENTER);
        
        
        painelTabuladoUtilitarios.adicionaAba(painelGerenciadorBibliotecas);
        painelTabuladoUtilitarios.adicionaAba(painelGerenciadorTemplates);
        painelTabuladoUtilitarios.setAbaAtual(0);
        //painelGerenciadorBibliotecas.setBorder(new CompoundBorder(new LineBorder(ColorController.FUNDO_BOTOES_EXPANSIVEIS, 2), new EmptyBorder(10, 10, 10, 10)));
    }

    public void configurarBotaoPlugin() {
        if (GerenciadorPlugins.getInstance().getPluginsCarregados().size() > 0) {
            WebButton btnConfigPlugin = new WebButton();
            
            WebButton botaoAcao = new WebButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exibirPainelPlugins();
                    botoesPlugin.setVisible(false);
                }
            });

            botaoAcao.setBorderPainted(false);
            botaoAcao.setOpaque(false);
            botaoAcao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            botaoAcao.setFocusPainted(false);
            botaoAcao.setFocusable(false);
            botaoAcao.setHideActionText(true);
            botaoAcao.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            botaoAcao.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);            
            botaoAcao.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "ajuda.png"));

            if (WeblafUtils.weblafEstaInstalado()) {
                WeblafUtils.configurarBotao(botaoAcao, ColorController.FUNDO_BOTOES_EXPANSIVEIS, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            }
            FabricaDicasInterface.criarTooltip(botaoAcao, "Configurações do plugin");
            botoesPlugin.add(botaoAcao);
            botoesPlugin.repaint();
            

            btnConfigPlugin.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    botoesPlugin.setVisible(!botoesPlugin.isVisible());
                    if(botoesPlugin.isVisible()){
                        WeblafUtils.configurarBotao(btnConfigPlugin, ColorController.FUNDO_BOTOES_EXPANSIVEIS, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
                    }else{
                        WeblafUtils.configurarBotao(btnConfigPlugin, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
                    }
                }
            });
            
            btnConfigPlugin.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "plugin64.png"));
            if (WeblafUtils.weblafEstaInstalado()) {
                WeblafUtils.configurarBotao(btnConfigPlugin, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            }
            
            FabricaDicasInterface.criarTooltip(btnConfigPlugin, "Exibir Ações dos Plugins instalados");
            barraFerramentas.add(btnConfigPlugin);            
            
        }
    }

    public void reseta() {
        painelSaida.selecionaConsole();
        painelGerenciadorBibliotecas.reseta();
        this.getCabecalho().configurarCores();
    }

    @Override
    public void configurarCores()
    {
        setBackground(ColorController.COR_PRINCIPAL);
        scrollOutlineTree.setCorner(JScrollPane.LOWER_RIGHT_CORNER, null);
        painelSaida.setForeground(ColorController.COR_LETRA);
        painelTemplate.setBackground(ColorController.COR_PRINCIPAL);
        inspetorDeSimbolos.setBackground(ColorController.COR_CONSOLE);
        inspetorDeSimbolos.setForeground(ColorController.COR_LETRA);
        labelTituloUtilitarios.setForeground(ColorController.COR_LETRA);
        painelConfigPlugins.setBackground(ColorController.COR_CONSOLE);
        painelConfigPlugins.setForeground(ColorController.COR_LETRA);
        treePanel.setBackground(ColorController.COR_PRINCIPAL);
        painelRecuperados.setBackground(ColorController.VERMELHO.brighter().brighter());
        painelRecuperados.setBorder(new LineBorder(ColorController.VERMELHO, 2));
        labelRecuperados.setForeground(Color.BLACK);
        botoesPlugin.setBackground(ColorController.FUNDO_BOTOES_EXPANSIVEIS);
        botoesPlugin.setOpaque(true);
        
        
        if (WeblafUtils.weblafEstaInstalado()) {

            WeblafUtils.configuraWeblaf(barraFerramentas);//tira a borda dos botões principais
            WeblafUtils.configuraWeblaf(botoesPlugin);//tira a borda dos botões principais
            WeblafUtils.configuraWebLaf(scrollInspetor);
            WeblafUtils.configuraWebLaf(webSeparator1);
            WeblafUtils.configuraWebLaf(webSeparator2);
            WeblafUtils.configuraWebLaf(campoBusca.getCampoBusca());
            WeblafUtils.configuraWebLaf(scrollOutlineTree);
            ((WebScrollPaneUI) scrollOutlineTree.getUI()).setDrawBackground(false);
            WeblafUtils.configurarBotao(btnFecharPainelTemplates, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 1);
            WeblafUtils.configurarBotao(btnExecutar, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnDepurar, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnInterromper, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnSalvar, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnAbrir, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnSalvarComo, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(btnAjuda, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(barraBotoesEditor, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(barraBotoesInspetorArvore, ColorController.TRANSPARENTE, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(fecharRecuperados, ColorController.TRANSPARENTE, ColorController.COR_LETRA, ColorController.FUNDO_ESCURO, ColorController.COR_LETRA, 5);
        }
    }

    private BarraDeBotoesExpansivel barraBotoesInspetorArvore;
    private BarraDeBotoesExpansivel barraBotoesEditor;

    private void atualizaPainelRecuperados() {
        Queue recuperados = PortugolStudio.getInstancia().getArquivosRecuperados();
        if (recuperados.isEmpty() || getPortugolDocumento().getFile() == null || desativouRecuperados) {
            painelRecuperados.setVisible(false);
            return;
        }
        boolean temRecuperado = false;
        String titulo_aba = getCabecalho().getTitulo();
        titulo_aba = titulo_aba.substring(0, titulo_aba.length() - 4);
        arquivosRecuperados.removeAll();
        for (Object recuperado : recuperados) {
            File arquivoRecuperado = (File) recuperado;
            String filename = arquivoRecuperado.getName();
            if (filename.equals(titulo_aba + ".recuperado") || filename.equals(titulo_aba + "_2.recuperado")) {
                temRecuperado = true;
                String codigoFonterecuperado;
                try {
                    codigoFonterecuperado = FileHandle.open(arquivoRecuperado);
                    WebButton button = new WebButton();
                    button.setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbaCodigoFonte abaCodigoFonte = AbaCodigoFonte.novaAba();
                            abaCodigoFonte.setCodigoFonte(codigoFonterecuperado, null, true);
                            TelaPrincipal t = PortugolStudio.getInstancia().getTelaPrincipal();
                            t.getPainelTabulado().adicionaAba(abaCodigoFonte);
                        }
                    });
                    if (redimensionouParaBaixaResolucao) {
                        FabricaDicasInterface.criarTooltip(button, arquivoRecuperado.getName());
                    } else {
                        button.setText(arquivoRecuperado.getName().substring(0, arquivoRecuperado.getName().length() - 11));
                    }
                    button.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "light_pix_off.png"));
                    WeblafUtils.configurarBotao(button, ColorController.TRANSPARENTE, Color.BLACK, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
                    arquivosRecuperados.add(button);
                } catch (Exception ex) {
                    PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
                }
            }
        }
        if (!temRecuperado) {
            painelRecuperados.setVisible(false);
        } else {
            labelRecuperados.setText("Houve um erro na finalização do Portugol Studio, mas temos arquivos recuperados");
            painelRecuperados.setVisible(true);
        }
        painelRecuperados.revalidate();
        arquivosRecuperados.revalidate();
        painelRecuperados.repaint();
        arquivosRecuperados.repaint();

    }

    private void configurarResolucao() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(()
                        -> {
                    if (Lancador.getInstance().getActualSize().width <= 1024) {
                        if (!redimensionouParaBaixaResolucao) {
                            redimensionouParaBaixaResolucao = true;
                        }
                    } else {
                        redimensionouParaBaixaResolucao = false;
                    }
                    atualizaPainelRecuperados();
                });
            }
        });
    }

    private void configuraBarraDeBotoesDoPainelArvoreInspetor() {
        barraBotoesInspetorArvore = new BarraDeBotoesExpansivel();
        barraBotoesInspetorArvore.setName("barraBotoesInspetorArvore");

        Icon iconeFonte = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "font.png");
        Icon iconeMais = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "plus2.png");
        Icon iconeMenos = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "minus.png");

        AbstractAction acaoAumentarFonte = new AbstractAction("", iconeMais) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTamanhoFonteArvoreInspetor(getTamanhoDaFonteArvoreInspetor() + VALOR_INCREMENTO_FONTE);
            }
        };

        AbstractAction acaoDiminuirFonte = new AbstractAction("", iconeMenos) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTamanhoFonteArvoreInspetor(getTamanhoDaFonteArvoreInspetor() - VALOR_INCREMENTO_FONTE);
            }
        };
        barraBotoesInspetorArvore.adicionaGrupoDeItems("Tamanho da fonte", iconeFonte, new Action[]{
            acaoAumentarFonte, acaoDiminuirFonte
        });

        DataTypeFilterView dataTypeFilterView = new DataTypeFilterView();
        dataTypeFilterView.setFilter(tree.getFilter().getDataTypeFilter());
        dataTypeFilterView.registerActions(this);

        SymbolTypeFilterView symbolTypeFilterView = new SymbolTypeFilterView();
        symbolTypeFilterView.setFilter(tree.getFilter().getSymbolTypeFilter());
        symbolTypeFilterView.registerActions(this);

        campoBusca.setSearchAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.getFilter().getSymbolNameFilter().setSearchString(campoBusca.getCampoBusca().getText());
            }
        });

        String cancelFilterName = "cancelFilterByName";
        Action cancelFilterByNameAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                campoBusca.getCampoBusca().setText("");
                tree.getFilter().getSymbolNameFilter().setSearchString("");

                SwingUtilities.invokeLater(()
                        -> {
                    editor.getTextArea().requestFocusInWindow();
                });
            }
        };

        campoBusca.getActionMap().put(cancelFilterName, cancelFilterByNameAction);
        campoBusca.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelFilterName);

        String doFilterName = "doFilterByName";
        Action doFilterByNameAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.getFilter().getSymbolNameFilter().setSearchString(campoBusca.getCampoBusca().getText());

                SwingUtilities.invokeLater(()
                        -> {
                    editor.getTextArea().requestFocusInWindow();
                });
            }
        };

        campoBusca.getActionMap().put(doFilterName, doFilterByNameAction);
        campoBusca.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), doFilterName);

        String filterName = "filterByName";
        Action filterByNameAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(()
                        -> {
                    campoBusca.getCampoBusca().requestFocusInWindow();
                    campoBusca.getCampoBusca().selectAll();
                });
            }
        };
        
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), filterName);
        getActionMap().put(filterName, filterByNameAction);

        barraBotoesInspetorArvore.adicionaSeparador();
        barraBotoesInspetorArvore.adicionarComponente(dataTypeFilterView);

        barraBotoesInspetorArvore.adicionaSeparador();
        barraBotoesInspetorArvore.adicionarComponente(symbolTypeFilterView);

        adicionaBotaoConfiguracaoInspetor(0);
    }

    private void criaControlesDaFonteDoEditor() {
        Icon iconeFonte = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "font.png");
        Icon iconeMais = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "plus2.png");
        Icon iconeMenos = IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "minus.png");

        AbstractAction acaoAumentarFonte = new AbstractAction("", iconeMais) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Font fonteAtual = editor.getTextArea().getFont();
                float novoTamanho = fonteAtual.getSize() + VALOR_INCREMENTO_FONTE;
                editor.setTamanhoFonteEditor(novoTamanho);
            }
        };

        AbstractAction acaoDiminuirFonte = new AbstractAction("", iconeMenos) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Font fonteAtual = editor.getTextArea().getFont();
                float novoTamanho = fonteAtual.getSize() - VALOR_INCREMENTO_FONTE;
                editor.setTamanhoFonteEditor(novoTamanho);
            }
        };
        barraBotoesEditor.adicionaGrupoDeItems("Tamanho da fonte", iconeFonte, new Action[]{
            acaoAumentarFonte, acaoDiminuirFonte
        });
    }

    public Action criaAcaoOpcoesExecucao() {
        String nome = "Exibir Opções de Execucao";
        if (Configuracoes.getInstancia().isExibirOpcoesExecucao()) {
            nome = "Parar de Exibir Opções de Execução";
        }

        acaoExibirOpcoesExecucao = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "help.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {

                Configuracoes.getInstancia().setExibirOpcoesExecucao(!Configuracoes.getInstancia().isExibirOpcoesExecucao());

            }
        };
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        acaoExibirOpcoesExecucao.putValue(Action.ACCELERATOR_KEY, atalho);
        getActionMap().put(nome, acaoExibirOpcoesExecucao);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        return acaoExibirOpcoesExecucao;
    }

    private void atualizarAcaoExibirOpcoesExecucao() {
        JMenuItem item = (JMenuItem) acaoExibirOpcoesExecucao.getValue("MenuItem");
        if (Configuracoes.getInstancia().isExibirOpcoesExecucao()) {
            item.setText("Parar de Exibir Opções de Execução");
        } else {
            item.setText("Exibir Opções de Execução");
        }
    }

    public Action criaAcaoPesquisarSubstituir() {

        String nome = "Pesquisar e substituir";
        AbstractAction acao = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "find.png")) {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PortugolStudio.getInstancia().getTelaProcurarSubstituirPanel().exibir(editor.getTextArea());
            }
        };
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        acao.putValue(Action.ACCELERATOR_KEY, atalho);
        getActionMap().put(nome, acao);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        return acao;
    }

    private Action criaAcaoExibirUtilitarios() {
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
        String nome = "Exibir Painel Utilitários";
        AbstractAction acaoPainelUtilitarios = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "biblioteca.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                painelTemplate.setVisible(! painelTemplate.isVisible());
            }
        };

        acaoPainelUtilitarios.putValue(Action.ACCELERATOR_KEY, atalho);

        getActionMap().put(nome, acaoPainelUtilitarios);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        return acaoPainelUtilitarios;
    }
    
    public Action criaAcaoCentralizarCodigoFonte() {
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, InputEvent.SHIFT_DOWN_MASK);
        String nome = "Centralizar código fonte";
        AbstractAction acaoCentralizarCodigoFonte = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "centralizar_codigo.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) getValue("MenuItem");
                Configuracoes configuracoes = Configuracoes.getInstancia();
                configuracoes.alterarCentralizarCondigoFonte();
                if (configuracoes.isCentralizarCodigoFonte()) {
                    item.setText("Descentralizar Código Fonte");
                } else {
                    item.setText("Centralizar Código Fonte");
                }
            }
        };

        acaoCentralizarCodigoFonte.putValue(Action.ACCELERATOR_KEY, atalho);

        getActionMap().put(nome, acaoCentralizarCodigoFonte);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        return acaoCentralizarCodigoFonte;
    }

    public boolean editorEstaExpandido() {
//        boolean divisorArvoreEditorExpandido = divisorArvoreEditor.getDividerLocation() > divisorArvoreEditor.getMaximumDividerLocation();
//        boolean divisorEditorConsoleExpandido = divisorEditorConsole.getDividerLocation() > divisorEditorConsole.getMaximumDividerLocation();
//        return divisorArvoreEditorExpandido && divisorEditorConsoleExpandido;
        return editorExpandido;
    }
    
    boolean editorExpandido = false;
    
    private void configuraLoader(){
        boolean usandoTemaDark = Configuracoes.getInstancia().isTemaDark();
        String caminhoIcone = String.format("/br/univali/ps/ui/icones/%s/grande/load.gif", usandoTemaDark ? "Dark" : "Portugol");
        Icon icone = new ImageIcon(getClass().getResource(caminhoIcone));
        indicadorProgresso = new IndicadorDeProgresso(this, icone, "Processando ...");
    }

    public MiniBarraDeFerramentas getMiniBarra() {
        return miniBarra;
    }
    
    
    
    private Action criaAcaoExpandirEditor() {
        AbstractAction acaoExpandir = new AbstractAction("Expandir", IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "expandir_componente.png")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JMenuItem item = (JMenuItem) getValue("MenuItem");
                if (!editorEstaExpandido()) {
                    if(!Lancador.getInstance().isMaximazed()){
                        Lancador.getInstance().maximize(true);
                    }
                    getPainelTabulado().ocultarContainerCabecalhos();
                    painelEditor.remove(barraFerramentas);
                    miniBarra.setVisible(true);
                    item.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "restaurar_componente.png"));
                    item.setText("Restaurar");
                    

                    
                    SwingUtilities.invokeLater(() -> {
                        divisorEditorConsole.setDividerLocation(1.0);
                        divisorArvoreEditor.setDividerLocation(1.0);
                        
                        editorExpandido = true;
                        
                    });
                } else {

                    getPainelTabulado().exibirContainerCabecalhos();
                    // Copiado do initComponents()
                    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 0;
                    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                    
                    painelEditor.add(barraFerramentas, gridBagConstraints);
                    miniBarra.setVisible(false);
                    editor.requestFocus();
                    item.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "expandir_componente.png"));
                    item.setText("Expandir");
                    
                    SwingUtilities.invokeLater(() -> {
                        divisorArvoreEditor.setDividerLocation(-1);
                        divisorEditorConsole.setDividerLocation(-1);                        
                        editorExpandido = false;
                    });
                }
            }
        };

        String nome = (String) acaoExpandir.getValue(AbstractAction.NAME);
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_DOWN_MASK);

        acaoExpandir.putValue(AbstractAction.ACCELERATOR_KEY, atalho);

        getActionMap().put(nome, acaoExpandir);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(atalho, nome);
        
        miniBarra.getBotaoRetrair().setAction(acaoExpandir);
        miniBarra.getBotaoRetrair().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "restaurar_componente.png"));
        miniBarra.getBotaoRetrair().setHideActionText(true);
        
        return acaoExpandir;
    }

    private void configurarBarraDeBotoesDoEditor() {
        barraBotoesEditor = new BarraDeBotoesExpansivel();
        barraBotoesEditor.setName("barraBotoesEditor");

        criaControlesDaFonteDoEditor();

        barraBotoesEditor.adicionaAcao(criaAcaoExpandirEditor());
        barraBotoesEditor.adicionaAcao(criaAcaoPesquisarSubstituir());
        barraBotoesEditor.adicionaAcao(criaAcaoCentralizarCodigoFonte());
        barraBotoesEditor.adicionaSeparador();
        barraBotoesEditor.adicionaAcao(criaAcaoExibirUtilitarios());
        criaMenuTemas();
        adicionaBotaoConfiguracaoEditor(0);
    }
    
    public void criaMenuTemas()
    {
        String[] temas = ColorController.listarTemas();
        Action[] acoes = new Action[temas.length+1];
                
        for (int i=0; i<temas.length; i++)
        {
            acoes[i] = new AbstractAction(temas[i])
            {
                @Override
                public void actionPerformed(ActionEvent evento)
                {
                    AbstractButton itemSelecionado = (AbstractButton) evento.getSource();
                    String tema = itemSelecionado.getText();
                    Configuracoes.getInstancia().TrocarTema(tema);
                }
            };
        }
        acoes[temas.length] = new AbstractAction("Editar Temas")
        {
            @Override
            public void actionPerformed(ActionEvent evento)
            {
                PortugolStudio.getInstancia().getTelaEditarTemas().setVisible(true);
            }
        };
        barraBotoesEditor.adicionaGrupoDeItems("Temas", IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "all_types.png"), acoes, false);
    }

    private void adicionaBotaoConfiguracaoInspetor(int margemDireita) {
        GridBagConstraints constrainsts = new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, margemDireita), 0, 0);

        treePanel.add(barraBotoesInspetorArvore, constrainsts);
        treePanel.setComponentZOrder(barraBotoesInspetorArvore, 0);
    }

    private void adicionaBotaoConfiguracaoEditor(int margemDireita) {
        GridBagConstraints constraints = new GridBagConstraints(3, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, margemDireita), 0, 0);

        painelEditor.add(barraBotoesEditor, constraints);
        painelEditor.setComponentZOrder(barraBotoesEditor, 0);
    }

    public static class NoTransferable implements Transferable {

        public static final DataFlavor NO_DATA_FLAVOR
                = new DataFlavor(List.class, "List");
        private List<NoDeclaracaoBase> nosDeclaracoes;

        public NoTransferable(List<NoDeclaracaoBase> nosDeclaracoes) {
            this.nosDeclaracoes = nosDeclaracoes;
        }

        public List<NoDeclaracaoBase> getNos() {
            return nosDeclaracoes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{
                NO_DATA_FLAVOR
            };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(NO_DATA_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return nosDeclaracoes;
        }
    }

    public static void inicializarPool() {
        try {
            SwingUtilities.invokeAndWait(()
                    -> {
                try {
                    //TODO: Verificar se podemos mover este código para um local melhor.
                    // Antes nós tinhamos o Applet, mas agora. Seguem comentários anteriores:

                    /*
                             inicializei o pool aqui para evitar chamar o construtor da classe AbaCodigoFonte quando o Applet está rodando.
                             O construtor de AbaCodigoFonte inicializa um FileChooser e utiliza a classe File, e isso causa uma exceção no Applet não assinado.
                     */
                    poolAbasCodigoFonte = new PoolAbasCodigoFonte(TAMANHO_POOL_ABAS);
                } catch (Exception excecao) {
                    LOGGER.log(Level.SEVERE, "Não foi possível inicializar o pool de abas de código fonte", excecao);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public static AbaCodigoFonte novaAba() {
        ajustarNumeroDocumento();
        if (poolAbasCodigoFonte == null) {
            LOGGER.log(Level.WARNING, "ATENÇÃO, não foi iniciado um Pool de Abas no inicio do programa. A aba será criada sem cache.");
            return new AbaCodigoFonte();
        }
        AbaCodigoFonte aba = (AbaCodigoFonte) poolAbasCodigoFonte.obter();
        aba.getCabecalho().setTitulo("Sem título" + numeroDocumento);
        aba.reseta();
        return aba;
    }

    private void configurarArvoreEstrutural() {
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        tree.getFilter().enableDataTypeFilter();
        tree.getFilter().getDataTypeFilter().acceptAll();
        tree.getFilter().enableSymbolNameFilter();

        tree.getFilter().addListener(()
                -> {
            salvaArquivo();
        });
    }

    private void criarPainelPlugin() {
        painelConfigPlugins = new PainelConfigPlugins();
        painelConfigPlugins.setBorder(null);
    }

    private void criarPainelTemporario() {
        painelTemporario = new JPanel();
        painelTemporario.setBorder(null);
        painelTemporario.setLayout(new GridLayout(1, 1));
        painelTemporario.setOpaque(false);
        painelTemporario.setFocusable(false);
        painelTemporario.setBackground(Color.RED);
    }

    private void carregarConfiguracoes() {
        Configuracoes configuracoes = Configuracoes.getInstancia();
        setTamanhoFonteArvoreInspetor(configuracoes.getTamanhoFonteArvore());
    }

    protected JFileChooser criarSeletorArquivo() {
        filtroPrograma = new FiltroArquivo("Programa do Portugol", "por");

        JFileChooser dialogoSelecaoArquivo = FabricaDeFileChooser.getFileChooserSalvamento();
        dialogoSelecaoArquivo.setMultiSelectionEnabled(true);
        dialogoSelecaoArquivo.setFileFilter(filtroPrograma);
        dialogoSelecaoArquivo.setAcceptAllFileFilterUsed(false);
        dialogoSelecaoArquivo.addChoosableFileFilter(filtroPrograma);

        dialogoSelecaoArquivo.setFileFilter(filtroPrograma);
        return dialogoSelecaoArquivo;
    }

    protected void configurarAcoes() {
        configurarAcaoSalvarArquivo();
        configurarAcaoSalvarComo();
        configurarAcaoExecutarPontoParada();
        configurarAcaoExecutarPasso();
        configurarAcaoInterromper();
        configurarAcaoAbrirAjuda();
        configurarAcaoAbrirArquivo();
    }

    private void configurarAcaoSalvarComo() {
        final String nome = "Salvar como";
        final KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK);

        acaoSalvarComo = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "save_as.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialogoSelecaoArquivo = criarSeletorArquivo();
                if (editor.getPortugolDocumento().getFile() != null) {
                    File arquivoAtual = editor.getPortugolDocumento().getFile();
                    dialogoSelecaoArquivo.setCurrentDirectory(arquivoAtual.getParentFile());
                    dialogoSelecaoArquivo.setSelectedFile(arquivoAtual);
                    Configuracoes.getInstancia().setCaminhoUltimoDiretorio(arquivoAtual.getParentFile());
                }
                else
                {
                    dialogoSelecaoArquivo.setCurrentDirectory(new File (Configuracoes.getInstancia().getCaminhoUltimoDiretorio()));
                    dialogoSelecaoArquivo.setSelectedFile(new File(""));
                }

                if (dialogoSelecaoArquivo.showSaveDialog(getPainelTabulado()) == JFileChooser.APPROVE_OPTION) {
                    File arquivo = dialogoSelecaoArquivo.getSelectedFile();
                        AbaCodigoFonte aba = PortugolStudio.getInstancia().getTelaPrincipal().obterAbaArquivo(arquivo);
                          
                        if (aba == null || !arquivo.exists()) {
                            editor.getPortugolDocumento().setFile(arquivo);
                            podeSalvar = true;
                            acaoSalvarArquivo.actionPerformed(e);
                        }
                        else
                        {
                            QuestionDialog.getInstance().showMessage("Este arquivo já está aberto em outra aba.\nPor favor feche o arquivo aberto antes de sobrescrevê-lo.", JOptionPane.WARNING_MESSAGE);
                            usuarioCancelouSalvamento = true;
                        }
                        Configuracoes.getInstancia().setCaminhoUltimoDiretorio(dialogoSelecaoArquivo.getCurrentDirectory());
                } else {
                    usuarioCancelouSalvamento = true;
                    Configuracoes.getInstancia().setCaminhoUltimoDiretorio(dialogoSelecaoArquivo.getCurrentDirectory());
                }
            }
        };

        getActionMap().put(nome, acaoSalvarComo);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        btnSalvarComo.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "save_as.png"));
        btnSalvarComo.setAction(acaoSalvarComo);
        miniBarra.getBotaoSalvarComo().setAction(acaoSalvarComo);
        miniBarra.getBotaoSalvarComo().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "save_as.png"));
        miniBarra.getBotaoSalvarComo().setHideActionText(true);
    }

    private void salvaArquivo() {

        if (podeSalvar) {
            try {
                final PortugolDocumento documento = editor.getPortugolDocumento();
                if (documento.getFile() != null) {
                    String texto = documento.getText(0, documento.getLength());
                    texto = inserirInformacoesPortugolStudio(texto);
                    PortugolStudio.getInstancia().salvarComoRecente(getArquivoComExtensao(documento.getFile()));
                    FileHandle.save(texto, getArquivoComExtensao(documento.getFile()));
                    documento.setChanged(false);
                }
            } catch (BadLocationException ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
            } catch (Exception ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
            }
        }
    }

    private void salvaArquivoRecuperavel() {

        if (podeSalvar) {
            try {
                final PortugolDocumento documento = editor.getPortugolDocumento();
                String filename = getCabecalho().getTitulo();
                if (filename.contains("*")) {
                    filename = filename.replace("*", "");
                }
                if (documento.getFile() != null) {
                    filename = documento.getFile().getName();
                    filename = filename.substring(0, filename.length() - 4);
//                    filename = filename+"_recuperado"; 
                    PortugolStudio.getInstancia().salvarCaminhoOriginalRecuperado(documento.getFile());
                }
                if (isPrimeiroRecuperavel) {
                    filename = filename + "_2";
                    isPrimeiroRecuperavel = false;
                } else {
                    isPrimeiroRecuperavel = true;
                }
                File arquivoRecuperavel = new File(Configuracoes.getInstancia().getDiretorioTemporario().getAbsolutePath() + "/" + filename);
                String texto = documento.getText(0, documento.getLength());
                texto = inserirInformacoesPortugolStudio(texto);
                FileHandle.save(texto, getArquivoComExtensaoRecuperacao(arquivoRecuperavel));
            } catch (BadLocationException ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
            } catch (Exception ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
            }
        }
    }

    private File getArquivoComExtensao(File arquivo) {
        int indiceExtensao = arquivo.getAbsolutePath().indexOf(".por");
        if (indiceExtensao < 0) {//não tem extensão
            return new File(arquivo.getAbsolutePath() + ".por");
        }
        return arquivo;
    }

    private File getArquivoComExtensaoRecuperacao(File arquivo) {
        int indiceExtensao = arquivo.getAbsolutePath().indexOf(".recuperado");
        if (indiceExtensao < 0) {//não tem extensão
            return new File(arquivo.getAbsolutePath() + ".recuperado");
        }
        return arquivo;
    }

    private void configurarAcaoSalvarArquivo() {
        final String nome = (String) "Salvar arquivo";
        final KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

        acaoSalvarArquivo = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "save.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final PortugolDocumento documento = editor.getPortugolDocumento();
                if (documento.getFile() != null) {
                    salvaArquivo();
                } else {
                    acaoSalvarComo.actionPerformed(e);
                }
            }

        };

        acaoSalvarArquivo.setEnabled(editor.getPortugolDocumento().isChanged());
        btnSalvar.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "save.png"));
        btnSalvar.setAction(acaoSalvarArquivo);
        
        miniBarra.getBotaoSalvar().setAction(acaoSalvarArquivo);
        miniBarra.getBotaoSalvar().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "save.png"));
        miniBarra.getBotaoSalvar().setHideActionText(true);
        
        getActionMap().put(nome, acaoSalvarArquivo);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
    }
    
    private void configurarAcaoAbrirArquivo() {
        final String nome = (String) "Abrir arquivo";
        final KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);

        acaoAbrirArquivo = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "open.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                 getPainelTabulado().getActionMap().get("Abrir arquivo").actionPerformed(e);
            }
        };
        btnAbrir.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "open.png"));
        btnAbrir.setAction(acaoAbrirArquivo);
        miniBarra.getBotaoAbrir().setAction(acaoAbrirArquivo);
        miniBarra.getBotaoAbrir().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "open.png"));
        miniBarra.getBotaoAbrir().setHideActionText(true);

        getActionMap().put(nome, acaoAbrirArquivo);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
    }
    
    private void configurarAcaoAbrirAjuda() {
        final String nome = (String) "Abrir ajuda";
        

        acaoAbrirAjuda = new AbstractAction(nome, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "helplow.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                 getPainelTabulado().getActionMap().get(PainelTabuladoPrincipal.ACAO_EXIBIR_AJUDA).actionPerformed(e);
            }
        };
        acaoAbrirAjuda.putValue(Action.SMALL_ICON, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "helplow.png"));
        acaoAbrirAjuda.putValue(Action.LARGE_ICON_KEY, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "helplow.png"));
        
        btnAjuda.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "helplow.png"));
        btnAjuda.setAction(acaoAbrirAjuda);
        
        miniBarra.getBotaoAjuda().setAction(acaoAbrirAjuda);
        miniBarra.getBotaoAjuda().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "helplow.png"));
        miniBarra.getBotaoAjuda().setHideActionText(true);
        
        getActionMap().put(nome, acaoAbrirAjuda);
        
    }

    /**
     * *
     * Classe privada para encapsular o início da execução "normal" e da
     * execução "passo a passo"
     */
    private class AcaoExecucao extends AbstractAction 
    {
        private final Estado estadoInicial;

        public AcaoExecucao(String nome, Estado estadoInicial)
        {
            super(nome);
            this.estadoInicial = estadoInicial;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            inspetorDeSimbolos.resetaDestaqueDosSimbolos();

            try {
                setaAtivacaoBotoesExecucao(false); // desabilita execução até que a execução tenha sido finalizada ou um break point tenha sido alcançado
                if (tarefaCompilacao == null) {
                    compilaProgramaParaExecucao();
                }
                JButton botao = null;
                if (e.getSource() instanceof JButton) {
                    botao = (JButton) e.getSource();
                }
                while (!tarefaCompilacao.isDone()) // aguarda (sem travar a EDT) até que a compilação para execução termine. Não é exatamente uma "solução bonita" :)
                {
                    setVisibilidadeLoader(true);
                    if (botao != null) {
                        botao.paintImmediately(0, 0, botao.getWidth(), botao.getHeight());
                    }
                    Thread.sleep(50);
                }

                programaAnalisado = programaCompilado = tarefaCompilacao.get();
                inspetorDeSimbolos.setPrograma(programaCompilado);

                executar(estadoInicial); // estado inicial da execução: executa até o próximo Ponto de parada ou "passo a passo"
            } catch (ErroCompilacao erroCompilacao) {
                exibirResultadoAnalise(erroCompilacao.getResultadoAnalise());
                
                setaAtivacaoBotoesExecucao(true); // pode executar                
            } catch (Exception ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
                LOGGER.log(Level.SEVERE, null, ex);
                setaAtivacaoBotoesExecucao(true); // pode executar                
            } finally {
                setVisibilidadeLoader(false);
            }
        }
    }

    private void configurarAcaoExecutarPontoParada() {
        acaoExecutarPontoParada = new AcaoExecucao("Executar", Estado.BREAK_POINT);


        String nome = "AcaoPontoParada";
        KeyStroke atalho = KeyStroke.getKeyStroke("shift F6");

        acaoExecutarPontoParada.putValue(Action.NAME, nome);
        acaoExecutarPontoParada.putValue(Action.ACCELERATOR_KEY, atalho); // F5 funciona
        acaoExecutarPontoParada.putValue(Action.LARGE_ICON_KEY, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "resultset_next.png"));
        acaoExecutarPontoParada.putValue(Action.SMALL_ICON, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "resultset_next.png"));

        getActionMap().put(nome, acaoExecutarPontoParada);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        btnExecutar.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "resultset_next.png"));
        btnExecutar.setAction(acaoExecutarPontoParada);
        miniBarra.getBotaoPlay().setAction(acaoExecutarPontoParada);
        miniBarra.getBotaoPlay().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "resultset_next.png"));
        miniBarra.getBotaoPlay().setHideActionText(true);
    }

    private void configurarAcaoExecutarPasso() {

        acaoExecutarPasso = new AcaoExecucao("Depurar", Estado.STEP_OVER);

        String nome = "AcaoPassoPasso";
        KeyStroke atalho = KeyStroke.getKeyStroke("shift F5");

        acaoExecutarPasso.putValue(Action.NAME, nome);
        acaoExecutarPasso.putValue(Action.ACCELERATOR_KEY, atalho);
        acaoExecutarPasso.putValue(Action.LARGE_ICON_KEY, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "step.png"));
        acaoExecutarPasso.putValue(Action.SMALL_ICON, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "step.png"));

        getActionMap().put(nome, acaoExecutarPasso);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        btnDepurar.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "step.png"));
        btnDepurar.setAction(acaoExecutarPasso);
        miniBarra.getBotaoPasso().setAction(acaoExecutarPasso);
        miniBarra.getBotaoPasso().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "step.png"));
        miniBarra.getBotaoPasso().setHideActionText(true);
    }

    private void configurarAcaoInterromper() {

        acaoInterromper = new AbstractAction("Interromper") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                interromper();
            }

        };

        String nome = (String) acaoInterromper.getValue(AbstractAction.NAME);
        KeyStroke atalho = KeyStroke.getKeyStroke("shift F7");

        acaoInterromper.setEnabled(false);
        acaoInterromper.putValue(Action.NAME, nome);
        acaoInterromper.putValue(Action.ACCELERATOR_KEY, atalho); // Tente F6, F8, F10. Nenhum funciona
        acaoInterromper.putValue(Action.LARGE_ICON_KEY, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "stop.png"));
        acaoInterromper.putValue(Action.SMALL_ICON, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "stop.png"));

        getActionMap().put(nome, acaoInterromper);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
        btnInterromper.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "stop.png"));
        btnInterromper.setAction(acaoInterromper);
        miniBarra.getBotaoParar().setAction(acaoInterromper);
        miniBarra.getBotaoParar().setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "stop.png"));
        miniBarra.getBotaoParar().setHideActionText(true);
    }

    private int getNumeroDeLinhas(String codigo) {
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(codigo));
            lineNumberReader.skip(Long.MAX_VALUE);
            return lineNumberReader.getLineNumber() + 1;
        } catch (IOException excecao) {
            LOGGER.log(Level.SEVERE, null, excecao);
        }
        return 0;
    }

    private void instalarObservadores() {
        PortugolParser.getParser(getEditor().getTextArea()).addPropertyChangeListener(PortugolParser.PROPRIEDADE_PROGRAMA_COMPILADO, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                programaAnalisado = (Programa) pce.getNewValue();

                int linhas = getNumeroDeLinhas(editor.getTextArea().getText());
                programaAnalisado.setNumeroLinhas(linhas);

                if (!simbolosInspecionadosJaForamCarregados) //é a primeira compilação?
                {
                    carregaSimbolosInspecionados(codigoFonteAtual, programaAnalisado);
                    simbolosInspecionadosJaForamCarregados = true;
                }

                inspetorDeSimbolos.setPrograma(programaAnalisado);

                //sempre que a árvore for gerada é necessário verificar 
                //quais são as linhas paráveis e adicionar pontos de parada nestas linhas
                BuscadorDeLinhasParaveis buscadorDeLinhasParaveis = new BuscadorDeLinhasParaveis();
                Set<Integer> linhasParaveis = buscadorDeLinhasParaveis.getLinhasParaveis(programaAnalisado);
                editor.getTextArea().criarPontosDeParadaDesativados(linhasParaveis);

                salvaArquivoRecuperavel();

                //Gambiarra pro botão não sumir :3
                SwingUtilities.invokeLater(() -> {
                    painelEditor.repaint();
                });

                setaAtivacaoBotoesExecucao(true);
            }
        });

        getEditor().getTextArea().addListenter((pontosDeParada) -> {

            if (programaAnalisado != null) {
                programaAnalisado.ativaPontosDeParada(pontosDeParada);
            }

            salvaArquivo();
        });

        Configuracoes configuracoes = Configuracoes.getInstancia();

        configuracoes.adicionarObservadorConfiguracao(AbaCodigoFonte.this, Configuracoes.EXIBIR_OPCOES_EXECUCAO);
        configuracoes.adicionarObservadorConfiguracao(AbaCodigoFonte.this, Configuracoes.TAMANHO_FONTE_ARVORE);
        editor.getPortugolDocumento().addPortugolDocumentoListener(AbaCodigoFonte.this);
        painelSaida.getAbaMensagensCompilador().adicionaAbaMensagemCompiladorListener(editor);
        adicionarAbaListener(AbaCodigoFonte.this);
        tree.observar(editor.getTextArea());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    editor.getTextArea().requestFocusInWindow();

                    if (Configuracoes.getInstancia().isExibirAvisoRenomear()) {
                        QuestionDialog.getInstance().showMessage(""
                                + "O Portugol Studio tem uma novidade! Agora você pode renomear elementos do seu programa\n"
                                + "como, por exemplo, variáveis e funções."
                                + "\n\n"
                                + "Na árvore estrutural do programa, localizada ao lado direito do editor de código fonte, -->\n"
                                + "dê um duplo clique sobre o nome do elemento que você quer renomear."
                                + "\n\n"
                                + "Você também pode renomear através do editor de código fonte, posicionando o cursor do teclado\n"
                                + "sobre o nome do elemento e pressionando a combinação de teclas: Ctrl + R."
                                + "\n\n"
                                + "Que a força esteja com você!!!", JOptionPane.INFORMATION_MESSAGE);
                        Configuracoes.getInstancia().setExibirAvisoRenomear(false);
                    }
                });
            }
        });

        tree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            TreePath path = e.getNewLeadSelectionPath();

            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object obj = node.getUserObject();
                TrechoCodigoFonte trechoCodigoFonte = null;

                if (obj instanceof NoDeclaracaoBase) {
                    trechoCodigoFonte = ((NoDeclaracaoBase) obj).getTrechoCodigoFonteNome();
                }

                if (trechoCodigoFonte != null) {
                    editor.selecionarTexto(trechoCodigoFonte.getLinha() - 1, trechoCodigoFonte.getColuna(), trechoCodigoFonte.getTamanhoTexto());
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());

                    if (selRow != -1) {
                        TreePath path = tree.getPathForLocation(e.getX(), e.getY());

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object obj = node.getUserObject();

                        if (obj instanceof NoDeclaracaoBase)
                        {
                            if ((programaCompilado != null && programaCompilado.isExecutando()))
                            {
                                QuestionDialog.getInstance().showMessage("Não é possível renomear enquanto o programa está executando. Interrompa o programa e tente novamente");
                                editor.getTextArea().requestFocusInWindow();
                            } else {
                                TrechoCodigoFonte trechoCodigoFonte = ((NoDeclaracaoBase) obj).getTrechoCodigoFonteNome();

                                try {
                                    String programa = editor.getPortugolDocumento().getCodigoFonte();
                                    int linha = trechoCodigoFonte.getLinha();
                                    int coluna = trechoCodigoFonte.getColuna() + 1;

                                    TelaRenomearSimbolo telaRenomearSimbolo = PortugolStudio.getInstancia().getTelaRenomearSimboloPanel();
                                    telaRenomearSimbolo.exibir(programa, linha, coluna);

                                    if (telaRenomearSimbolo.usuarioAceitouRenomear()) {
                                        String programaRenomeado = Portugol.renomearSimbolo(programa, linha, coluna, telaRenomearSimbolo.getNovoNome());
                                        editor.setCodigoFonteRenomeado(programaRenomeado);
                                    }
                                } catch (ExcecaoAplicacao ex) {
                                    PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
                                } catch (ErroAoRenomearSimbolo ex) {
                                    if (ex.getTipo() == ErroAoRenomearSimbolo.Tipo.ERRO_USUARIO) {
                                        PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(new ExcecaoAplicacao(ex.getMensagem(), ExcecaoAplicacao.Tipo.ERRO_USUARIO));
                                    } else {
                                        PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

    }

    protected void criarDicasInterface()
    {
        FabricaDicasInterface.criarTooltip(btnAbrir, "Abre um arquivo .por");
        FabricaDicasInterface.criarTooltip(btnAjuda, "Abre a ajuda com sintaxe e bibliotecas");
        FabricaDicasInterface.criarTooltip(btnExecutar, "Executa o programa até o próximo ponto de parada", acaoExecutarPontoParada);
        FabricaDicasInterface.criarTooltip(btnInterromper, "Interrompe a execução/depuração do programa atual", acaoInterromper);
        FabricaDicasInterface.criarTooltip(btnDepurar, "Executa o programa passo a passo", acaoExecutarPasso);
        FabricaDicasInterface.criarTooltip(btnSalvar, "Salva o programa atual no computador, em uma pasta escolhida pelo usuário", acaoSalvarArquivo);
        FabricaDicasInterface.criarTooltip(btnSalvarComo, "Salva uma nova cópia do programa atual no computador, em uma pasta escolhida pelo usuário", acaoSalvarComo);
        FabricaDicasInterface.criarTooltip(barraBotoesEditor.getCompomemtParaAdicionarDica(), "Personalizar as cores do Portugol");
        FabricaDicasInterface.criarTooltip(barraBotoesInspetorArvore.getCompomemtParaAdicionarDica(), "Personalizar a árvore estrutural e o inspetor de variáveis ...");
    }

    public Editor getEditor() {
        return editor;
    }
    
    public void adicionarBiblioteca(String biblioteca){
        String code = this.getPortugolDocumento().getCodigoFonte();
        if(! code.contains("inclua biblioteca "+biblioteca)){
            if(code.contains("programa\r\n{"))
            {
                code = code.replace("programa\r\n{", "programa\n{\n\tinclua biblioteca "+biblioteca);
            }
            else if(code.contains("programa \r\n{"))
            {
                code = code.replace("programa \r\n{", "programa\n{\n\tinclua biblioteca "+biblioteca);
            }
            else if(code.contains("programa \n{"))
            {
                code = code.replace("programa \n{", "programa\n{\n\tinclua biblioteca "+biblioteca);
            }
            else
            {
                code = code.replace("programa\n{", "programa\n{\n\tinclua biblioteca "+biblioteca);
            }            
            this.getEditor().setCodigoFonte(code);
            int index = code.indexOf("inclua biblioteca "+biblioteca);
            this.getEditor().getTextArea().setCaretPosition(index);
            
        }
    }
    public void removerBiblioteca(String biblioteca){
        String code = this.getPortugolDocumento().getCodigoFonte();
        if(code.contains("\tinclua biblioteca "+biblioteca)){
            String[] a = code.split("\tinclua biblioteca "+biblioteca);
            if(a[1].startsWith("\n")){
                a[1] = a[1].substring(1);
                code = "";
                for (String a1 : a) {
                    code += a1;
                }
                this.getEditor().setCodigoFonte(code);
            }else{
                String[] b = a[1].split("\n");
                StringBuilder ultimo = new StringBuilder(a[0]);
                for (int i = 1; i < b.length; i++) {
                    if(i>=2){
                        ultimo.append("\n");
                    }
                    ultimo.append(b[i]);
                }
                for (int i = 2; i < a.length; i++) {
                    ultimo.append(a[i]);
                }
                this.getEditor().setCodigoFonte(ultimo.toString());
            }
            int index = code.indexOf("programa");
            this.getEditor().getTextArea().setCaretPosition(index);
            
        }
    }
    

    public InspetorDeSimbolos getInspetor() 
    {
        return inspetorDeSimbolos;
    }
    
    public JTree getArvore()
    {
        return tree;
    }

    private void configurarCursorBotoes()
    {
        barraFerramentas.setOpaque(false);

        for (Component componente : barraFerramentas.getComponents()) {
            if (componente instanceof JButton) {
                JButton botao = (JButton) componente;

                botao.setOpaque(false);
                botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public void setCodigoFonte(String codigoFonte, final File arquivo, final boolean podeSalvar) {
        this.codigoFonteAtual = codigoFonte;//o código fonte completo (incluindo as informações do PortugolStudio) 
        //será utilizado mais adiante para carregar os símbolos inspecionados que foram salvos no arquivo
        PortugolParser parser = editor.getSuporteLinguagemPortugol().getPortugolParser();
        parser.resetUltimoCodigoAnalisado();
        codigoFonte = codigoFonte.replace("\r", "");
        simbolosInspecionadosJaForamCarregados = false;
        tree.reseta();
        inspetorDeSimbolos.reseta();
        editor.setCodigoFonte(codigoFonte);
        painelGerenciadorBibliotecas.updateBibliotecas();
        carregarInformacoesFiltroArvore(codigoFonte);

        PortugolDocumento document = editor.getPortugolDocumento();
        document.setFile(arquivo);
        document.setChanged(false);
        this.podeSalvar = podeSalvar;

        acaoSalvarArquivo.setEnabled(false);
        atualizaPainelRecuperados();
    }

    private void carregarInformacoesFiltroArvore(String codigoFonte) {
        carregarInformacoesFiltroArvoreTipoDado(codigoFonte);
        carregarInformacoesFiltroArvoreTipoSimbolo(codigoFonte);
    }

    private void carregarInformacoesFiltroArvoreTipoDado(String codigoFonte) {
        DataTypeFilter dataTypeFilter = tree.getFilter().getDataTypeFilter();
        Matcher avaliador = Pattern.compile("@FILTRO-ARVORE-TIPOS-DE-DADO[ ]*=[ ]*([ ]*(inteiro|real|logico|cadeia|caracter)[ ]*)(,[ ]*(inteiro|real|logico|cadeia|caracter)[ ]*)*;").matcher(codigoFonte);

        if (avaliador.find()) {
            String linha = avaliador.group();
            String valores[] = linha.split("=")[1].replace(";", "").split(",");

            dataTypeFilter.rejectAll();

            for (String tipo : valores) {
                dataTypeFilter.accept(TipoDado.obterTipoDadoPeloNome(tipo.trim()));
            }
        } else {
            dataTypeFilter.acceptAll();
        }
    }

    private void carregarInformacoesFiltroArvoreTipoSimbolo(String codigoFonte) {
        SymbolTypeFilter symbolTypeFilter = tree.getFilter().getSymbolTypeFilter();
        Matcher avaliador = Pattern.compile("@FILTRO-ARVORE-TIPOS-DE-SIMBOLO[ ]*=[ ]*([ ]*(variavel|vetor|matriz|funcao)[ ]*)(,[ ]*(variavel|vetor|matriz|funcao)[ ]*)*;").matcher(codigoFonte);

        if (avaliador.find()) {
            String linha = avaliador.group();
            String valores[] = linha.split("=")[1].replace(";", "").split(",");

            symbolTypeFilter.rejectAll();

            for (String tipoSimbolo : valores) {
                symbolTypeFilter.accept(SymbolTypeFilter.SymbolType.valueOf(tipoSimbolo.trim().toUpperCase()));
            }
        } else {
            symbolTypeFilter.acceptAll();
        }
    }

    private void carregaSimbolosInspecionados(final String codigoFonteCompleto, final Programa programa) {
        if (codigoFonteCompleto == null || programa == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String regex = "@SIMBOLOS-INSPECIONADOS[ ]*=[ ]* (\\{[_a-zA-Z0-9]+, [0-9]+, [0-9]+, [0-9]+\\}[-]?)+;";
                String informacoes = Utils.extrairInformacoesPortugolStudio(codigoFonteCompleto);
                Matcher avaliador = Pattern.compile(regex).matcher(informacoes);
                if (avaliador.find()) {
                    String linhas[] = avaliador.group().replace("@SIMBOLOS-INSPECIONADOS = ", "").replaceAll("[\\{\\};]", "").split("-");
                    for (String linha : linhas) {
                        String partes[] = linha.trim().split(",");
                        try {
                            String nomeDoSimbolo = partes[0].trim();
                            int linhaDoSimbolo = Integer.valueOf(partes[1].trim());
                            int colunaDoSimbolo = Integer.valueOf(partes[2].trim());
                            int tamanhoDoTextoDoSimbolo = Integer.valueOf(partes[3].trim());
                            NoDeclaracaoBase noDeclaracao = procuraNoDeclaracao(programa, nomeDoSimbolo, linhaDoSimbolo, colunaDoSimbolo, tamanhoDoTextoDoSimbolo);
                            if (noDeclaracao != null) {
                                inspetorDeSimbolos.adicionaNo(noDeclaracao);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }
            }
        });

    }
    
    public void showPainelUtilitarios(boolean show)
    {
        painelTemplate.setVisible(show);
    }

    private NoDeclaracaoBase procuraNoDeclaracao(Programa programa, final String nomeDoSimbolo, final int linhaDoSimbolo, final int colunaDoSimbolo, final int tamanhoDoTexto) throws ExcecaoVisitaASA {
        if (programa == null) {
            return null;
        }
        ProcuradorDeDeclaracao procuradorDeSimbolo = new ProcuradorDeDeclaracao(nomeDoSimbolo, linhaDoSimbolo, colunaDoSimbolo, tamanhoDoTexto);
        programa.getArvoreSintaticaAbstrata().aceitar(procuradorDeSimbolo);
        return procuradorDeSimbolo.getNoDeclaracao();//retorna null se não encontra o nó
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        divisorArvoreEditor = new javax.swing.JSplitPane();
        divisorEditorConsole = new javax.swing.JSplitPane();
        painelEditor = new javax.swing.JPanel();
        botoesPlugin = new javax.swing.JToolBar();
        barraFerramentas = new javax.swing.JToolBar();
        btnExecutar = new com.alee.laf.button.WebButton();
        btnDepurar = new com.alee.laf.button.WebButton();
        btnInterromper = new com.alee.laf.button.WebButton();
        webSeparator2 = new com.alee.laf.separator.WebSeparator();
        btnSalvar = new com.alee.laf.button.WebButton();
        btnSalvarComo = new com.alee.laf.button.WebButton();
        btnAbrir = new com.alee.laf.button.WebButton();
        webSeparator1 = new com.alee.laf.separator.WebSeparator();
        btnAjuda = new com.alee.laf.button.WebButton();
        miniBarra = new br.univali.ps.ui.editor.MiniBarraDeFerramentas();
        editor = new br.univali.ps.ui.editor.Editor();
        painelConsole = new javax.swing.JPanel();
        painelSaida = new br.univali.ps.ui.paineis.PainelSaida();
        painelInspetorArvore = new javax.swing.JPanel();
        painelTemplate = new javax.swing.JPanel();
        painelfecharTemplates = new javax.swing.JPanel();
        btnFecharPainelTemplates = new com.alee.laf.button.WebButton();
        labelTituloUtilitarios = new javax.swing.JLabel();
        divisorArvoreInspetor = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        campoBusca = new br.univali.ps.ui.rstautil.tree.SearchTextPanel();
        scrollOutlineTree = new javax.swing.JScrollPane();
        tree = new br.univali.ps.ui.rstautil.tree.PortugolOutlineTree();
        scrollInspetor = new javax.swing.JScrollPane();
        inspetorDeSimbolos = new br.univali.ps.ui.inspetor.InspetorDeSimbolos();
        painelRecuperados = new javax.swing.JPanel();
        labelRecuperados = new javax.swing.JLabel();
        arquivosRecuperados = new javax.swing.JPanel();
        fecharRecuperados = new com.alee.laf.button.WebButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 2, 2));
        setLayout(new java.awt.BorderLayout());

        divisorArvoreEditor.setBackground(new java.awt.Color(255, 255, 255));
        divisorArvoreEditor.setBorder(null);
        divisorArvoreEditor.setDividerSize(15);
        divisorArvoreEditor.setResizeWeight(1.0);
        divisorArvoreEditor.setDoubleBuffered(true);
        divisorArvoreEditor.setFocusable(false);
        divisorArvoreEditor.setMinimumSize(new java.awt.Dimension(550, 195));
        divisorArvoreEditor.setName("splitArvoreEditor"); // NOI18N
        divisorArvoreEditor.setOneTouchExpandable(true);

        divisorEditorConsole.setBorder(null);
        divisorEditorConsole.setDividerSize(15);
        divisorEditorConsole.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        divisorEditorConsole.setResizeWeight(1.0);
        divisorEditorConsole.setMinimumSize(new java.awt.Dimension(501, 460));
        divisorEditorConsole.setName("divisorEditorConsole"); // NOI18N
        divisorEditorConsole.setOneTouchExpandable(true);

        painelEditor.setFocusable(false);
        painelEditor.setMinimumSize(new java.awt.Dimension(500, 240));
        painelEditor.setName("painelEditor"); // NOI18N
        painelEditor.setOpaque(false);
        painelEditor.setPreferredSize(new java.awt.Dimension(500, 240));
        painelEditor.setLayout(new java.awt.GridBagLayout());

        botoesPlugin.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(348, 0, 0, 0);
        painelEditor.add(botoesPlugin, gridBagConstraints);

        barraFerramentas.setFloatable(false);
        barraFerramentas.setOrientation(javax.swing.SwingConstants.VERTICAL);
        barraFerramentas.setName("barraFerramentas"); // NOI18N
        barraFerramentas.setOpaque(false);

        btnExecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/resultset_next.png"))); // NOI18N
        btnExecutar.setFocusable(false);
        btnExecutar.setHideActionText(true);
        btnExecutar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecutar.setName("botaoExecutar"); // NOI18N
        btnExecutar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnExecutar);

        btnDepurar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/step.png"))); // NOI18N
        btnDepurar.setFocusable(false);
        btnDepurar.setHideActionText(true);
        btnDepurar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDepurar.setName("botaoDepurar"); // NOI18N
        btnDepurar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnDepurar);

        btnInterromper.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/stop.png"))); // NOI18N
        btnInterromper.setEnabled(false);
        btnInterromper.setFocusable(false);
        btnInterromper.setHideActionText(true);
        btnInterromper.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnInterromper.setName("botaoInterromper"); // NOI18N
        btnInterromper.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnInterromper);

        webSeparator2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 3));
        barraFerramentas.add(webSeparator2);

        btnSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/save.png"))); // NOI18N
        btnSalvar.setFocusable(false);
        btnSalvar.setHideActionText(true);
        btnSalvar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSalvar.setName("botaoSalvar"); // NOI18N
        btnSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnSalvar);

        btnSalvarComo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/save_as.png"))); // NOI18N
        btnSalvarComo.setFocusable(false);
        btnSalvarComo.setHideActionText(true);
        btnSalvarComo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSalvarComo.setName("botaoSalvarComo"); // NOI18N
        btnSalvarComo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnSalvarComo);

        btnAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/open.png"))); // NOI18N
        btnAbrir.setFocusable(false);
        btnAbrir.setHideActionText(true);
        btnAbrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAbrir.setName("botaoAbrir"); // NOI18N
        btnAbrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnAbrir);

        webSeparator1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 3));
        barraFerramentas.add(webSeparator1);

        btnAjuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/helplow.png"))); // NOI18N
        btnAjuda.setFocusable(false);
        btnAjuda.setHideActionText(true);
        btnAjuda.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAjuda.setName("botaoSalvar"); // NOI18N
        btnAjuda.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraFerramentas.add(btnAjuda);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        painelEditor.add(barraFerramentas, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        painelEditor.add(miniBarra, gridBagConstraints);

        editor.setMinimumSize(new java.awt.Dimension(350, 22));
        editor.setName("editor"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        painelEditor.add(editor, gridBagConstraints);

        divisorEditorConsole.setTopComponent(painelEditor);

        painelConsole.setDoubleBuffered(false);
        painelConsole.setName("painelConsole"); // NOI18N
        painelConsole.setOpaque(false);
        painelConsole.setLayout(new java.awt.GridBagLayout());

        painelSaida.setBorder(null);
        painelSaida.setMinimumSize(new java.awt.Dimension(150, 200));
        painelSaida.setName("painelSaida"); // NOI18N
        painelSaida.setPreferredSize(new java.awt.Dimension(200, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        painelConsole.add(painelSaida, gridBagConstraints);

        divisorEditorConsole.setRightComponent(painelConsole);

        divisorArvoreEditor.setLeftComponent(divisorEditorConsole);

        painelInspetorArvore.setMinimumSize(new java.awt.Dimension(250, 510));
        painelInspetorArvore.setName("painelInspetorArvore"); // NOI18N
        painelInspetorArvore.setOpaque(false);
        painelInspetorArvore.setPreferredSize(new java.awt.Dimension(270, 233));
        painelInspetorArvore.setLayout(new java.awt.BorderLayout());

        painelTemplate.setLayout(new java.awt.BorderLayout());

        painelfecharTemplates.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        painelfecharTemplates.setOpaque(false);
        painelfecharTemplates.setLayout(new java.awt.BorderLayout());

        btnFecharPainelTemplates.setText("x");
        btnFecharPainelTemplates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharPainelTemplatesActionPerformed(evt);
            }
        });
        painelfecharTemplates.add(btnFecharPainelTemplates, java.awt.BorderLayout.EAST);

        labelTituloUtilitarios.setText("# Painel Utilitários");
        painelfecharTemplates.add(labelTituloUtilitarios, java.awt.BorderLayout.CENTER);

        painelTemplate.add(painelfecharTemplates, java.awt.BorderLayout.PAGE_START);

        painelInspetorArvore.add(painelTemplate, java.awt.BorderLayout.PAGE_START);

        divisorArvoreInspetor.setDividerSize(15);
        divisorArvoreInspetor.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        divisorArvoreInspetor.setResizeWeight(1.0);
        divisorArvoreInspetor.setMinimumSize(new java.awt.Dimension(252, 510));
        divisorArvoreInspetor.setName("divisorArvoreInspetor"); // NOI18N
        divisorArvoreInspetor.setOneTouchExpandable(true);

        treePanel.setName("painelArvore"); // NOI18N
        treePanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        treePanel.add(campoBusca, gridBagConstraints);

        scrollOutlineTree.setBackground(new java.awt.Color(255, 255, 255));
        scrollOutlineTree.setBorder(null);
        scrollOutlineTree.setMinimumSize(new java.awt.Dimension(250, 23));
        scrollOutlineTree.setName("scrollArvore"); // NOI18N
        scrollOutlineTree.setPreferredSize(new java.awt.Dimension(250, 2));

        tree.setBackground(new java.awt.Color(153, 51, 0));
        tree.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        tree.setName("arvore"); // NOI18N
        tree.setOpaque(false);
        scrollOutlineTree.setViewportView(tree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        treePanel.add(scrollOutlineTree, gridBagConstraints);

        divisorArvoreInspetor.setTopComponent(treePanel);

        scrollInspetor.setBorder(null);
        scrollInspetor.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInspetor.setMinimumSize(new java.awt.Dimension(31, 150));
        scrollInspetor.setName("scrollInspetor"); // NOI18N
        scrollInspetor.setOpaque(false);
        scrollInspetor.setPreferredSize(new java.awt.Dimension(266, 200));

        inspetorDeSimbolos.setName("inspetor"); // NOI18N
        scrollInspetor.setViewportView(inspetorDeSimbolos);

        divisorArvoreInspetor.setBottomComponent(scrollInspetor);

        painelInspetorArvore.add(divisorArvoreInspetor, java.awt.BorderLayout.CENTER);

        divisorArvoreEditor.setRightComponent(painelInspetorArvore);

        add(divisorArvoreEditor, java.awt.BorderLayout.CENTER);

        painelRecuperados.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        painelRecuperados.setEnabled(false);
        painelRecuperados.setFocusCycleRoot(true);
        painelRecuperados.setName("painelRecuperados"); // NOI18N
        painelRecuperados.setLayout(new java.awt.BorderLayout());

        labelRecuperados.setText("Ouve algum problema no encerramento do Portugol, mas temos arquivos recuperados.");
        labelRecuperados.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 75, 1, 1));
        labelRecuperados.setName("labelRecuperados"); // NOI18N
        painelRecuperados.add(labelRecuperados, java.awt.BorderLayout.WEST);

        arquivosRecuperados.setName("painelArquivosRecuperados"); // NOI18N
        arquivosRecuperados.setOpaque(false);
        painelRecuperados.add(arquivosRecuperados, java.awt.BorderLayout.CENTER);

        fecharRecuperados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/window_close.png"))); // NOI18N
        fecharRecuperados.setName("botaoFecharRecuperados"); // NOI18N
        fecharRecuperados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fecharRecuperadosActionPerformed(evt);
            }
        });
        painelRecuperados.add(fecharRecuperados, java.awt.BorderLayout.EAST);

        add(painelRecuperados, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fecharRecuperadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fecharRecuperadosActionPerformed
        painelRecuperados.setVisible(false);
        desativouRecuperados = true;
    }//GEN-LAST:event_fecharRecuperadosActionPerformed

    private void btnFecharPainelTemplatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharPainelTemplatesActionPerformed
        this.getActionMap().get("Exibir Painel Utilitários").actionPerformed(evt);
    }//GEN-LAST:event_btnFecharPainelTemplatesActionPerformed

    private void interromper()
    {
        if (programaCompilado != null)
        {
            programaCompilado.interromper();
        }
    }

    public float getTamanhoDaFonteArvoreInspetor() {
        return tree.getFont().getSize();
    }

    public void setTamanhoFonteArvoreInspetor(float tamanho) {
        if ((tamanho != tree.getFont().getSize()) && (tamanho >= TAMANHO_MINIMO_FONTE) && (tamanho <= TAMANHO_MAXIMO_FONTE)) {
            Font novaFonte = tree.getFont().deriveFont(tamanho);
            tree.setFont(novaFonte);
            inspetorDeSimbolos.setTamanhoDaFonte(tamanho);
            Configuracoes.getInstancia().setTamanhoFonteArvore(tamanho);
        }
    }

    private void atualizaStatusAcaoSalvar(boolean documentoFoiModificado) {
        if (podeSalvar) {
            acaoSalvarArquivo.setEnabled(documentoFoiModificado);
        } else {
            acaoSalvarArquivo.setEnabled(false);
        }
    }

    private void compilaProgramaParaExecucao() throws IOException {
        if (tarefaCompilacao != null) {
            tarefaCompilacao.cancel(true);
        }

        tarefaCompilacao = service.submit(() -> {

            String codigoFonte = editor.getTextArea().getText();

            LOGGER.log(Level.CONFIG, "COMPILANDO para execução");

            String classPath = getClassPathParaCompilacao();
            String caminhoJavac = Caminhos.obterCaminhoExecutavelJavac();
            LOGGER.log(Level.CONFIG, "Compilando no classpath: {0}", classPath);
            LOGGER.log(Level.CONFIG, "Usando javac em : {0}", caminhoJavac);

            Programa programa = null;
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return programa;
                }
                programa = Portugol.compilarParaExecucao(codigoFonte, classPath, caminhoJavac);
                LOGGER.log(Level.CONFIG, "Compilação finalizada");
            } catch (ErroCompilacao erro) {
                programa = erro.getResultadoAnalise().getPrograma();
            } finally {
                liberaMemoriaAlocada();
            }

            return programa;
        });
    }

    private String getClassPathParaCompilacao() throws IOException {
        String classPathSeparator = !rodandoEmmWindows() ? ":" : ";";

        Configuracoes configuracoes = Configuracoes.getInstancia();
        if (Configuracoes.rodandoEmDesenvolvimento()) {
            return System.getProperty("java.class.path") + classPathSeparator + getPluginsPath();
        }

        File classpathDir = new File(configuracoes.getDiretorioAplicacao().getCanonicalPath(), "lib");

        String expandedClassPath = "";
        if (classpathDir.isDirectory()) {
            File jars[] = classpathDir.listFiles();

            for (File jar : jars) {
                expandedClassPath += jar.getCanonicalPath() + classPathSeparator;
            }
        }
        return expandedClassPath + getPluginsPath();
    }
    
    private String getPluginsPath() throws IOException
    {
        String classPathSeparator = !rodandoEmmWindows() ? ":" : ";";
        String expandedClassPath = "";
        File[] pluginsClassDir = new File(Configuracoes.getInstancia().getDiretorioPlugins().getCanonicalPath()).listFiles();
        
        if(pluginsClassDir == null)
        {
            return classPathSeparator;
        }
        
        for (File file : pluginsClassDir) {
            if(file.isDirectory())
            {
                for (File listFile : file.listFiles()) {
                    expandedClassPath += listFile.getCanonicalPath() + classPathSeparator;
                }
            }
            else
            {
                expandedClassPath += file.getCanonicalPath() + classPathSeparator;
            }
        }
        return expandedClassPath;
    }
    
    private static boolean rodandoEmmWindows() {
        String so = System.getProperty("os.name");

        return (so != null && so.toLowerCase().contains("win"));
    }

    private static void liberaMemoriaAlocada() {
        Runtime runtime = Runtime.getRuntime();
        long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
        LOGGER.log(Level.CONFIG, "Liberando memoria alocada - Total alocado ({0} MB)", memoriaUsada >> 20);
        System.gc();
    }

    @Override
    public void documentoModificado(boolean modificado) {
        // sempre que o documento é modificado precisamos descartar o programa já compilado e compilar novamente
        if (modificado) {
            try {
                compilaProgramaParaExecucao();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        SwingUtilities.invokeLater(() -> {

            atualizaStatusAcaoSalvar(modificado);

            atualizaCabecalho(modificado);

        });
    }

    private void atualizaCabecalho(boolean documentoFoiModificado) {
        if (documentoFoiModificado && podeSalvar) {
            getCabecalho().setIcone(lampadaApagada);
            if (!getCabecalho().getTitulo().endsWith("*")) {
                getCabecalho().setTitulo(getCabecalho().getTitulo() + "*");
            }
        } else {
            getCabecalho().setIcone(lampadaAcesa);
            if (getCabecalho().getTitulo().endsWith("*")) {
                getCabecalho().setTitulo(getCabecalho().getTitulo().substring(0, getCabecalho().getTitulo().length() - 1));
            }
        }
    }

    private boolean arquivoModificado() {
        return editor.getPortugolDocumento().isChanged() && podeSalvar;
    }

    @Override
    public boolean fechandoAba(Aba aba) {
        this.selecionar();
        usuarioCancelouSalvamento = false;

        if (programaCompilado != null && programaCompilado.isExecutando()) {
            programaCompilado.interromper();
        }

        if (arquivoModificado())
        {           
            int resp = QuestionDialog.getInstance().showConfirmMessage(String.format("O documento '%s' possui modificações, deseja Salvá-las?", getCabecalho().getTitulo()));
            if (resp == JOptionPane.YES_OPTION)
            {
                acaoSalvarArquivo.actionPerformed(null);

                if (usuarioCancelouSalvamento) {
                    return false;
                }
            } else if (resp == JOptionPane.CANCEL_OPTION || resp == JOptionPane.CLOSED_OPTION) {
                usuarioCancelouSalvamento = true;
                return false;
            }
        }
        return true;
    }

    @Override
    public void nomeArquivoAlterado(String nome) {
        ajustarNumeroDocumento();
        if (nome != null) {
            getCabecalho().setTitulo(nome);
        } else {
            getCabecalho().setTitulo("Sem título" + numeroDocumento);
        }
    }

    private static void ajustarNumeroDocumento() {

        boolean temAbaI = true;
        numeroDocumento = 1;
        for (int i = 1; temAbaI; i++) {
            numeroDocumento = i;
            temAbaI = verificaAbasSemTitulo(i);
        }
    }

    private static boolean verificaAbasSemTitulo(int i) {
        List<Aba> abas = PortugolStudio.getInstancia().getTelaPrincipal().getPainelTabulado().getAbas(AbaCodigoFonte.class);
        for (Aba aba : abas) {
            if (aba.getCabecalho().getTitulo().contains("Sem título" + i)) {
                return true;
            }
        }
        return false;
    }

    public PortugolDocumento getPortugolDocumento() {
        return editor.getPortugolDocumento();
    }

    private void executar(Estado estado) throws InterruptedException, ErroCompilacao
    {
        if (programaCompilado == null)
        {
            LOGGER.log(Level.SEVERE, "O programa está nulo, não é possível executar!");
            return;
        }

        if (!programaCompilado.isExecutando()) {
            SwingUtilities.invokeLater(() -> {
                AbaMensagemCompilador abaMensagens = painelSaida.getAbaMensagensCompilador();
                abaMensagens.limpar();
            });
            try {
                programaCompilado.setArquivoOrigem(editor.getPortugolDocumento().getFile());
                definirDiretorioTrabalho(programaCompilado);


                ResultadoAnalise resultadoAnalise = programaCompilado.getResultadoAnalise();
                if (resultadoAnalise.contemAvisos()) {
                    SwingUtilities.invokeLater(() -> {
                        exibirResultadoAnalise(resultadoAnalise);
                    });
                }
                if (!resultadoAnalise.contemErros()) {
                    programaCompilado.adicionarObservadorExecucao(new ObservadorExecucao());
                    programaCompilado.adicionarObservadorExecucao(editor);
                    programaCompilado.adicionarObservadorExecucao(inspetorDeSimbolos);

                    painelSaida.getConsole().registrarComoEntrada(programaCompilado);
                    painelSaida.getConsole().registrarComoSaida(programaCompilado);
                    SwingUtilities.invokeLater(() -> {
                        editor.iniciarExecucao(depurando);
                    });
                    programaCompilado.ativaPontosDeParada(editor.getLinhasComPontoDeParadaAtivados());
                    programaCompilado.executar(new String[]{}, estado);
                } else {
                    SwingUtilities.invokeLater(() -> {
                        exibirResultadoAnalise(resultadoAnalise);  
                    });
                    
                    //new LogManager(programaCompilado, editor.getTextArea().getText(), getNumeroDeLinhas(editor.getTextArea().getText()), PSAnalytics.URL_PADRAO, null);
                   
                    setaAtivacaoBotoesExecucao(true); // libera o botão de execução quando o programa tem erros - issue #358
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        else
        {
            if (estado == Estado.BREAK_POINT)
            {
                SwingUtilities.invokeLater(() -> {
                    editor.removerHighlightsDepuracao();
                });
            }

            programaCompilado.continuar(estado);
        }
    }

//    private void removePontosDeParadaInatingiveis(Set<Integer> linhasComPontosDeParadaValidos) {
//        editor.removePontosDeParadaInvalidos(linhasComPontosDeParadaValidos);
//    }
    private void definirDiretorioTrabalho(final Programa programa) {
        if (editor.getPortugolDocumento().getFile() != null) {
            programa.setDiretorioTrabalho(editor.getPortugolDocumento().getFile().getParentFile());
        } else {
            try {
                programa.setDiretorioTrabalho(new File(System.getProperty("user.dir")));
            } catch (SecurityException | IllegalArgumentException | NullPointerException excecao) {
                programa.setDiretorioTrabalho(new File("."));
                LOGGER.log(Level.INFO, "Impossível obter o diretório do usuário. Definindo o diretório atual como diretório de trabalho", excecao);
            }
        }
    }

    private void exibirResultadoAnalise(ResultadoAnalise resultadoAnalise) {
        SwingUtilities.invokeLater(() -> {

            for (ErroSintatico erro : resultadoAnalise.getErrosSintaticos()) {
                if (erro instanceof ErroExpressoesForaEscopoPrograma) {
                    try {
                        ErroExpressoesForaEscopoPrograma erroEx = (ErroExpressoesForaEscopoPrograma) erro;
                        int posicao = erroEx.getPosicao();
                        int linha = editor.getTextArea().getLineOfOffset(posicao);
                        int coluna = posicao - editor.getTextArea().getLineStartOffset(linha);

                        erroEx.setLinha(linha + 1);
                        erroEx.setColuna(coluna + 1);
                    } catch (BadLocationException ex) {

                    }
                }
            }

            AbaMensagemCompilador abaMensagensCompilador = painelSaida.getAbaMensagensCompilador();
            abaMensagensCompilador.atualizar(resultadoAnalise);
            abaMensagensCompilador.selecionar();
        });

    }

    private void exibirPopupAvisoCompilacao() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FabricaDicasInterface.mostrarNotificacao("O programa contém AVISOS de compilação, verifique a aba 'Mensagens'", 5000, IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "notification.png"));
            }
        });
    }

    private String inserirInformacoesPortugolStudio(String texto) {
        StringBuilder sb = new StringBuilder(texto);

        sb.append("\n/* $$$ Portugol Studio $$$ ");
        sb.append("\n * ");
        sb.append("\n * Esta seção do arquivo guarda informações do Portugol Studio.");
        sb.append("\n * Você pode apagá-la se estiver utilizando outro editor.");
        sb.append("\n * ");

        inserirInformacoesCursor(sb);
        inserirInformacoesDobramentoCodigo(sb);
        inserirInformacoesDosPontosDeParada(sb);
        inserirInformacoesDosSimbolosInspecionados(sb);
        inserirInformacoesFiltroArvore(sb);

        sb.append("\n */");

        return sb.toString();
    }

    private void inserirInformacoesDosSimbolosInspecionados(StringBuilder sb) {
        List<NoDeclaracaoBase> model = inspetorDeSimbolos.getNosInspecionados();
        StringBuilder sbItems = new StringBuilder();
        for (int i = 0; i < model.size(); i++) {
            sbItems.append("{");
            NoDeclaracaoBase no = model.get(i);
            String nome = no.getNome();
            String linha = String.valueOf(no.getTrechoCodigoFonteNome().getLinha());
            String coluna = String.valueOf(no.getTrechoCodigoFonteNome().getColuna());
            String tamanhoDoTexto = String.valueOf(no.getTrechoCodigoFonteNome().getTamanhoTexto());
            sbItems.append(nome).append(", ").append(linha).append(", ").append(coluna).append(", ").append(tamanhoDoTexto);
            sbItems.append((i < model.size() - 1) ? "}-" : "}");
        }
        sb.append(String.format("\n * @SIMBOLOS-INSPECIONADOS = %s;", sbItems));
    }

    private void inserirInformacoesDosPontosDeParada(StringBuilder sb) {
        List<Integer> linhasComPontoDeParada = new ArrayList<>(editor.getLinhasComPontoDeParadaAtivados());
        StringBuilder linhas = new StringBuilder();
        for (int i = 0; i < linhasComPontoDeParada.size(); i++) {
            linhas.append(linhasComPontoDeParada.get(i).toString());
            if (i < linhasComPontoDeParada.size() - 1) {
                linhas.append(", ");
            }
        }
        sb.append(String.format("\n * @PONTOS-DE-PARADA = %s;", linhas));
    }

    private void inserirInformacoesFiltroArvore(StringBuilder sb) {
        inserirInformacoesFiltroArvoreTipoDado(sb);
        inserirInformacoesFiltroArvoreTipoSimbolo(sb);
    }

    private void inserirInformacoesFiltroArvoreTipoDado(StringBuilder sb) {
        StringBuilder tipos = new StringBuilder();
        DataTypeFilter dataTypeFilter = tree.getFilter().getDataTypeFilter();

        if (!dataTypeFilter.getAcceptedDataTypes().isEmpty()) {
            for (TipoDado tipo : TipoDado.values()) {
                if (dataTypeFilter.isAccepting(tipo)) {
                    tipos.append(tipo.getNome());
                    tipos.append(", ");
                }
            }

            // Remove a última vírgula
            tipos = tipos.delete(tipos.length() - 2, tipos.length());

            sb.append(String.format("\n * @FILTRO-ARVORE-TIPOS-DE-DADO = %s;", tipos));
        }
    }

    private void inserirInformacoesFiltroArvoreTipoSimbolo(StringBuilder sb) {
        StringBuilder tipos = new StringBuilder();
        SymbolTypeFilter symbolTypeFilter = tree.getFilter().getSymbolTypeFilter();

        if (!symbolTypeFilter.getAcceptedSymbolTypes().isEmpty()) {
            for (SymbolTypeFilter.SymbolType tipoSimbolo : SymbolTypeFilter.SymbolType.values()) {
                if (symbolTypeFilter.isAccepting(tipoSimbolo)) {
                    tipos.append(tipoSimbolo.toString().toLowerCase());
                    tipos.append(", ");
                }
            }

            // Remove a última vírgula
            tipos = tipos.delete(tipos.length() - 2, tipos.length());

            sb.append(String.format("\n * @FILTRO-ARVORE-TIPOS-DE-SIMBOLO = %s;", tipos));
        }
    }

    private void inserirInformacoesCursor(final StringBuilder sb) {
        final int posicaoCursor = editor.getTextArea().getCaretPosition();

        if (posicaoCursor >= 0) {
            sb.append(String.format("\n * @POSICAO-CURSOR = %d; ", posicaoCursor));
        }
    }

    private void inserirInformacoesDobramentoCodigo(final StringBuilder sb) {
        final List<Integer> linhasCodigoDobradas = editor.getLinhasCodigoDobradas();

        if (linhasCodigoDobradas != null && !linhasCodigoDobradas.isEmpty()) {
            StringBuilder linhas = new StringBuilder("[");

            for (int i = 0; i < linhasCodigoDobradas.size(); i++) {
                linhas.append(linhasCodigoDobradas.get(i).toString());

                if (i < linhasCodigoDobradas.size() - 1) {
                    linhas.append(", ");
                }
            }

            linhas.append("]");

            sb.append(String.format("\n * @DOBRAMENTO-CODIGO = %s;", linhas));
        }
    }

    public String getCodigoFonte() {
        return getEditor().getTextArea().getText();
    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Configuracoes.EXIBIR_OPCOES_EXECUCAO:
                atualizarAcaoExibirOpcoesExecucao();
                break;

            case Configuracoes.TAMANHO_FONTE_ARVORE:
                setTamanhoFonteArvoreInspetor((Float) evt.getNewValue());
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (indicadorProgresso != null) {
            indicadorProgresso.desenha(g, getCentroEditor());
        }
    }

    private Point getCentroEditor() {
        Rectangle editorBounds = editor.getBounds();
        return new Point((int) editorBounds.getCenterX(), (int) editorBounds.getCenterY());
    }

    /**
     * *
     * Sobrescrevendo o mÃ©todo da classe Component para desenhar apenas a Ã¡rea
     * do loader. Este mÃ©todo Ã© chamado sempre que um outro quadro do GIF
     * estÃ¡ disponÃ­vel para ser desenhado. A animaÃ§Ã£o do GIF depende desse
     * mecanismo definido em {
     *
     * @see ImageObserver}. Isso resolve o problema do loader demorando demais
     * para desaparecer.
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (!indicadorProgresso.estaVisivel()) {
            return false;
        }

        if (infoflags == ImageObserver.FRAMEBITS) {
            Point centroEditor = getCentroEditor();
            repaint(indicadorProgresso.getBounds(centroEditor));
        }

        return true;
    }

    private void setVisibilidadeLoader(final boolean visivel) {
        indicadorProgresso.setVisibilidade(visivel);
        paintImmediately(getBounds());
    }

    public void exibirPainelSaida() {
        if (editorEstaExpandido()) {
            divisorEditorConsole.setDividerLocation(-1);
            revalidate();
        }
    }

    public void ocultarPainelSaida() {
        if (editorEstaExpandido()) {
            divisorEditorConsole.setDividerLocation(1.0);
            revalidate();
        }
    }

    public void carregarAlgoritmoPadrao() {
        podeSalvar = true;
        editor.setCodigoFonte(TEMPLATE_ALGORITMO);
        carregarInformacoesFiltroArvore(TEMPLATE_ALGORITMO);
        atualizaPainelRecuperados();
    }
    
    public void setarTemplate(String nome){
        try {
            editor.setCodigoFonte(FileHandle.read(ClassLoader.getSystemResourceAsStream("br/univali/ps/dominio/template_" + nome + ".por")));
            painelGerenciadorBibliotecas.updateBibliotecas();
        } catch (Exception ex) {
            Logger.getLogger(AbaCodigoFonte.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    private static String carregarTemplate() {
        try {
            return FileHandle.read(ClassLoader.getSystemResourceAsStream("br/univali/ps/dominio/template.por"));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void instalarPlugin(final Plugin plugin) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                painelConfigPlugins.addModeloLista(plugin);
                painelInspetorArvore.validate();
            }
        });
    }

    public void exibirPainelPlugins() {
        scrollInspetor.remove(inspetorDeSimbolos);
        scrollInspetor.setViewportView(painelConfigPlugins);
        divisorArvoreInspetor.setDividerLocation(0.7);
        painelInspetorArvore.validate();
    }

    public void ocultarPainelPlugins() {
        scrollInspetor.remove(painelConfigPlugins);
        scrollInspetor.setViewportView(inspetorDeSimbolos);
        divisorArvoreInspetor.setDividerLocation(-1);
    }

    @Override
    public void desinstalarPlugin(final Plugin plugin) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                painelConfigPlugins.removeModeloLista(plugin);
                painelInspetorArvore.validate();
            }
        });
    }

    @Override
    public String obterCodigoFonteUsuario() {
        return editor.getPortugolDocumento().getCodigoFonte();
    }

    public void exibirErros(ResultadoAnalise resultado) {
        getPainelSaida().getAbaMensagensCompilador().atualizar(resultado);
        getEditor().exibirErros(resultado);
    }

    @Override
    public ASAPrograma obterASAProgramaCompilado() {
        if (programaCompilado != null) {
            return programaCompilado.getArvoreSintaticaAbstrata();
        }

        return null;
    }

    @Override
    public ASAPrograma obterASAProgramaAnalisado() {
        if (programaAnalisado != null) {
            return programaAnalisado.getArvoreSintaticaAbstrata();
        }

        return null;
    }

    @Override
    public void instalarAcaoPlugin(final Plugin plugin, final Action acao) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WebButton botaoAcao = new WebButton(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        acao.actionPerformed(e);
                        botoesPlugin.setVisible(false);
                    }
                });
                botaoAcao.setIcon((Icon)acao.getValue(Action.SMALL_ICON));
                botaoAcao.setBorderPainted(false);
                botaoAcao.setOpaque(false);
                botaoAcao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                botaoAcao.setFocusPainted(false);
                botaoAcao.setFocusable(false);
                botaoAcao.setHideActionText(true);
                botaoAcao.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
                botaoAcao.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

                if (WeblafUtils.weblafEstaInstalado()) {
                    WeblafUtils.configurarBotao(botaoAcao, ColorController.FUNDO_BOTOES_EXPANSIVEIS, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
                }
                FabricaDicasInterface.criarTooltip(botaoAcao, acao.getValue(Action.NAME).toString());
                botoesPlugin.add(botaoAcao);
                botoesPlugin.repaint();
                mapaBotoesAcoesPlugins.put(acao, botaoAcao);
            }
        });
    }

    @Override
    public void desinstalarAcaoPlugin(Plugin plugin, final Action acao) {
        SwingUtilities.invokeLater(() -> {
            WebButton botaoAcao = (WebButton) mapaBotoesAcoesPlugins.get(acao);
            botoesPlugin.remove(botaoAcao);
            botoesPlugin.repaint();
            mapaBotoesAcoesPlugins.remove(acao, botaoAcao);
        });
    }

    @Override
    public void exibirPainelFlutuante(final JComponent origem, final JPanel conteudo, final boolean painelOpaco) {
        ocultarPainelFlutuante();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //painelFlutuante = criarPainelFlutuante(origem, conteudo, painelOpaco);
                //painelFlutuante.setVisible(true);
            }
        });
    }

    @Override
    public void ocultarPainelFlutuante() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                if (painelFlutuante != null && painelFlutuante.isVisible()) {
//                    painelFlutuante.setVisible(false);
//                    painelFlutuante = null;
//                }
            }
        });
    }
    
    @Override
    public void destacarTrechoCodigoFonte(int linha, int coluna, int tamanho) {
        editor.destacarTrechoCodigoFonte(linha, coluna, tamanho);
    }

    protected JButton getBtnSalvar() {
        return btnSalvar;
    }

    protected JButton getBtnSalvarComo() {
        return btnSalvarComo;
    }

    protected JSplitPane getDivisorEditorArvore() {
        return divisorArvoreEditor;
    }

    private void redefinirAba() {
        editor.getPortugolDocumento().setFile(null);
//        carregarAlgoritmoPadrao();
        editor.getTextArea().discardAllEdits();
        painelSaida.getConsole().limparConsole();
        editor.desabilitarCentralizacaoCodigoFonte();
        painelSaida.getAbaMensagensCompilador().limpar();
        painelSaida.getAbaMensagensCompilador().selecionar();

        editor.getPortugolDocumento().setChanged(true);
        getCabecalho().setTitulo("Sem título" + numeroDocumento + "*");
        numeroDocumento++;
        getCabecalho().setIcone(lampadaApagada);

        tree.desinstalaListenersDosFiltros(); // desinstala listener dos filtros antes de resetar os filtros para evitar que a árvore estrutural seja recriada a cada alteração dos filtros
        tree.getFilter().getDataTypeFilter().acceptAll();
        tree.getFilter().getSymbolTypeFilter().acceptAll();

        podeSalvar = false;

        tarefaCompilacao.cancel(true);
        tarefaCompilacao = null;
        programaAnalisado = programaCompilado = null;

    }

    private boolean podeFechar() {
        boolean podeFechar = !arquivoModificado() || (arquivoModificado() && !usuarioCancelouSalvamento);
        if (programaCompilado != null) {
            podeFechar &= !programaCompilado.isExecutando();
        }

        return podeFechar;
    }

    public BarraDeBotoesExpansivel getBarraBotoesInspetorArvore() {
        return barraBotoesInspetorArvore;
    }

    public BarraDeBotoesExpansivel getBarraBotoesEditor() {
        return barraBotoesEditor;
    }

    private final class ObservadorExecucao extends ObservadorExecucaoBasico {

        @Override
        public void execucaoIniciada(final Programa programa) {
            SwingUtilities.invokeLater(()
                    -> {
                acaoInterromper.setEnabled(true);
                painelSaida.getConsole().selecionar();

                painelSaida.getConsole().limparConsole();

                if (programa.getResultadoAnalise().contemAvisos()) {
                    exibirPopupAvisoCompilacao();
                }

                painelSaida.getConsole().setExecutandoPrograma(true);
            });
        }

        @Override
        public void execucaoEncerrada(final Programa programa, final ResultadoExecucao resultadoExecucao) {
            SwingUtilities.invokeLater(()
                    -> {
                AbaConsole console = painelSaida.getConsole();
                editor.finalizarExecucao(resultadoExecucao);

                console.removerPopupLeia();

                if (resultadoExecucao.getModoEncerramento() == ModoEncerramento.NORMAL) {
                    console.escreverNoConsole("\nPrograma finalizado. Tempo de execução: " + resultadoExecucao.getTempoExecucao() + " milissegundos");                    
                } else if (resultadoExecucao.getModoEncerramento() == ModoEncerramento.ERRO) {
                    console.escreverNoConsole("\nOcorreu um erro durante a execução do programa: " + resultadoExecucao.getErro().getMensagem());
                    console.escreverNoConsole("\nLinha: " + resultadoExecucao.getErro().getLinha() + ", Coluna: " + (resultadoExecucao.getErro().getColuna() + 1));
                    Timer timer = new Timer(100, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            painelSaida.selecionaConsole();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else if (resultadoExecucao.getModoEncerramento() == ModoEncerramento.INTERRUPCAO) {
                    console.escreverNoConsole("\nO programa foi interrompido!");                    
                }
                
                //new LogManager(programaCompilado, editor.getTextArea().getText(), getNumeroDeLinhas(editor.getTextArea().getText()), PSAnalytics.URL_PADRAO, resultadoExecucao.getErro());
                                                
                painelSaida.selecionaConsole();
                ocultarPainelSaida();
                acaoInterromper.setEnabled(false);
                setaAtivacaoBotoesExecucao(true);
                painelSaida.getConsole().setExecutandoPrograma(false);   
                liberaMemoriaAlocada();
            });

        }

        @Override
        public void execucaoPausada() {
            setaAtivacaoBotoesExecucao(false);
        }

        @Override
        public void execucaoResumida() {
            setaAtivacaoBotoesExecucao(true);
        }

        @Override
        public void highlightLinha(int linha) {
            // executado quando um break point ou um passo (execução passo-a-passo) é alcançado 
            setaAtivacaoBotoesExecucao(true);
        }
    }

    private static class PoolAbasCodigoFonte extends PoolAbstrato {

        public PoolAbasCodigoFonte(int tamanho) {
            super(tamanho);
        }

        @Override
        protected AbaCodigoFonte criarObjeto() {
            AbaCodigoFonte abaCodigoFonte = new AbaCodigoFonte();

            criarInstanciaPlugin(abaCodigoFonte);

            abaCodigoFonte.adicionarAbaListener(new AbaListener() {
                @Override
                public boolean fechandoAba(Aba aba) {
                    AbaCodigoFonte abaCodigoFonte = (AbaCodigoFonte) aba;
                    if (abaCodigoFonte.podeFechar()) {
                        abaCodigoFonte.redefinirAba();

                        abaCodigoFonte.showPainelUtilitarios(false);
                        /* Ao fechar a aba precisamos desinstalar todos os plugins instalados nela. Fazemos isto,
                         * para garantir que quando a aba for reaproveitada a partir do pool, ela não irá conter dados
                         * da utilização anterior
                         */
                        /*
                         * Logo após, instalamos todos os plugins novamente, para garantir que quando a aba for
                         * reaproveitada a partir do pool, já estará inicializada com os plugins
                         */
                        criarInstanciaPlugin(abaCodigoFonte);
                        devolver(abaCodigoFonte);

                        liberaMemoriaAlocada();

                        return true;
                    }

                    return false;
                }
            });

            return abaCodigoFonte;
        }
    }

    //Captura uma nova instância do gerenciador de plugins
    private static void criarInstanciaPlugin(AbaCodigoFonte abaCodigoFonte) {
        try {
            GerenciadorPlugins.getInstance().desinstalarPlugins(abaCodigoFonte);
            GerenciadorPlugins.getInstance().instalarPlugins(abaCodigoFonte);
        } catch (ErroInstalacaoPlugin erro) {
            PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(new ExcecaoAplicacao(erro.getMessage(), erro, ExcecaoAplicacao.Tipo.ERRO_PROGRAMA));
        }
    }

    private void setaAtivacaoBotoesExecucao(boolean ativados) {
        SwingUtilities.invokeLater(() -> {

            acaoExecutarPasso.setEnabled(ativados);
            acaoExecutarPontoParada.setEnabled(ativados);

        });
    }
    
    public PainelSaida getPainelSaida() {
        return this.painelSaida;
    }

    public JScrollPane getScrollInspetor() {
        return scrollInspetor;
    }

    public JPanel getPainelTemporario() {
        return painelTemporario;
    }

    public String getCodigoFonteAtual() {
        return codigoFonteAtual;
    }

    public AbaGerenciadorBibliotecas getPainelGerenciadorBibliotecas() {
        return painelGerenciadorBibliotecas;
    }

    public AbaGerenciadorTemplates getPainelGerenciadorTemplates() {
        return painelGerenciadorTemplates;
    }

    public PSPainelTabulado getPainelTabuladoUtilitarios() {
        return painelTabuladoUtilitarios;
    }

    public JToolBar getBarraFerramentas() {
        return barraFerramentas;
    }

    public JToolBar getBotoesPlugin() {
        return botoesPlugin;
    }

    public InspetorDeSimbolos getInspetorDeSimbolos() {
        return inspetorDeSimbolos;
    }

    public JPanel getPainelInspetorArvore() {
        return painelInspetorArvore;
    }

    public JPanel getPainelTemplate() {
        return painelTemplate;
    }

    public JPanel getTreePanel() {
        return treePanel;
    }

    public Programa getProgramaAnalisado() {
        return programaAnalisado;
    }

    public Programa getProgramaCompilado() {
        return programaCompilado;
    }

    public JPanel getPainelConsole() {
        return painelConsole;
    }

    public JPanel getPainelEditor() {
        return painelEditor;
    }
    
    
    @Override
    public void registrarBiblioteca(Class<? extends Biblioteca> biblioteca) {
        try {
            Portugol.getGerenciadorBibliotecas().registrarBibliotecaExterna(biblioteca);
        } catch (ErroCarregamentoBiblioteca ex) {
            Logger.getLogger(AbaCodigoFonte.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel arquivosRecuperados;
    private javax.swing.JToolBar barraFerramentas;
    private javax.swing.JToolBar botoesPlugin;
    private com.alee.laf.button.WebButton btnAbrir;
    private com.alee.laf.button.WebButton btnAjuda;
    private com.alee.laf.button.WebButton btnDepurar;
    private com.alee.laf.button.WebButton btnExecutar;
    private com.alee.laf.button.WebButton btnFecharPainelTemplates;
    private com.alee.laf.button.WebButton btnInterromper;
    private com.alee.laf.button.WebButton btnSalvar;
    private com.alee.laf.button.WebButton btnSalvarComo;
    private br.univali.ps.ui.rstautil.tree.SearchTextPanel campoBusca;
    private javax.swing.JSplitPane divisorArvoreEditor;
    private javax.swing.JSplitPane divisorArvoreInspetor;
    private javax.swing.JSplitPane divisorEditorConsole;
    private br.univali.ps.ui.editor.Editor editor;
    private com.alee.laf.button.WebButton fecharRecuperados;
    private br.univali.ps.ui.inspetor.InspetorDeSimbolos inspetorDeSimbolos;
    private javax.swing.JLabel labelRecuperados;
    private javax.swing.JLabel labelTituloUtilitarios;
    private br.univali.ps.ui.editor.MiniBarraDeFerramentas miniBarra;
    private javax.swing.JPanel painelConsole;
    private javax.swing.JPanel painelEditor;
    private javax.swing.JPanel painelInspetorArvore;
    private javax.swing.JPanel painelRecuperados;
    private br.univali.ps.ui.paineis.PainelSaida painelSaida;
    private javax.swing.JPanel painelTemplate;
    private javax.swing.JPanel painelfecharTemplates;
    private javax.swing.JScrollPane scrollInspetor;
    private javax.swing.JScrollPane scrollOutlineTree;
    private br.univali.ps.ui.rstautil.tree.PortugolOutlineTree tree;
    private javax.swing.JPanel treePanel;
    private com.alee.laf.separator.WebSeparator webSeparator1;
    private com.alee.laf.separator.WebSeparator webSeparator2;
    // End of variables declaration//GEN-END:variables
}
