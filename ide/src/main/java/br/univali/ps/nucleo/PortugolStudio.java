package br.univali.ps.nucleo;

import br.univali.portugol.nucleo.Portugol;
import br.univali.portugol.nucleo.bibliotecas.base.ErroCarregamentoBiblioteca;
import br.univali.ps.DetectorViolacoesThreadSwing;
import br.univali.ps.plugins.base.GerenciadorPlugins;
import br.univali.ps.ui.Lancador;
import br.univali.ps.ui.utils.FabricaDeFileChooser;
import br.univali.ps.ui.Splash;
import br.univali.ps.ui.telas.TelaRenomearSimbolo;
import br.univali.ps.ui.telas.TelaPrincipal;
import br.univali.ps.ui.abas.AbaCodigoFonte;
import br.univali.ps.ui.editor.PSFindReplace;
import br.univali.ps.ui.paineis.PainelPluginsInstalados;
import br.univali.ps.ui.telas.TelaDicas;
import br.univali.ps.ui.telas.TelaErrosPluginsBibliotecas;
import br.univali.ps.ui.telas.TelaInformacoesPlugin;
import br.univali.ps.ui.telas.TelaLicencas;
import br.univali.ps.ui.telas.TelaCustomBorder;
import br.univali.ps.ui.utils.FabricaDicasInterface;
import br.univali.ps.ui.swing.weblaf.WeblafUtils;
import br.univali.ps.ui.swing.weblaf.jOptionPane.QuestionDialog;
import br.univali.ps.ui.telas.Sobre;
import br.univali.ps.ui.telas.TelaAtalhos;
import br.univali.ps.ui.telas.TelaEditarTemas;
import br.univali.ps.ui.telas.TelaRelatarBug;
import br.univali.ps.ui.utils.FileHandle;
import br.univali.ps.ui.window.OutsidePanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Luiz Fernando Noschang
 * @since 22/08/2011
 */
public final class PortugolStudio
{

    private static final Logger LOGGER = Logger.getLogger(PortugolStudio.class.getName());
    
    private static PortugolStudio instancia = null;

    private final List<File> arquivosIniciais = new ArrayList<>();
    private final List<File> diretoriosPluginsInformadosPorParametro = new ArrayList<>();

    private final Random random = new Random(System.nanoTime());
    private final List<String> dicas = new ArrayList<>();
    private final List<Integer> dicasExibidas = new ArrayList<>();
    private Queue<File> arquivosRecentes = new LinkedList();
    private Queue<File> arquivosRecuperados = new LinkedList();
    private Queue<File> arquivosRecuperadosOriginais = new LinkedList();

    private String versao = null;
    private boolean depurando = false;
    
    /* Garante que o Portugol Studio não seja fechado enquanto o jar inicializador-ps.jar está sendo substituído. Assim evitamos problemas com a atualização */
    private boolean atualizandoInicializador = false;

    private JDialog telaSobre = null;
    private JDialog telaRelatarBug = null;
    private OutsidePanel outSidePanel;
    private TelaPrincipal telaPrincipal = null;
    private TelaCustomBorder telaInformacoesPlugin = null;
    private TelaErrosPluginsBibliotecas telaErrosPluginsBibliotecas = null;
    private TelaCustomBorder telaPluginsInstalados = null;
    private TelaCustomBorder telaLicencas = null;
    private TelaCustomBorder telaRenomearSimbolo = null;
    private TelaCustomBorder telaPesquisarSubstituir = null;
    private TelaCustomBorder telaEditarTemas = null;
    
    private JDialog telaDicas = null;
    private JDialog telaAtalhosTeclado = null;
        
    private TratadorExcecoes tratadorExcecoes = null;
    
    private static boolean portugolCarregado = false;
    
    private PortugolStudio()
    {   
        readRecents();
        readRecuperaveis();
        readOriginais();
    }
    
    public void readRecents(){
        File f = Configuracoes.getInstancia().getCaminhoArquivosRecentes();
        arquivosRecentes.clear();
        try {
            String arquivo = FileHandle.open(f);
            String [] caminhos = arquivo.split("\n");
            for (String caminho : caminhos) {
                File recente = new File(caminho);
                if(recente.exists()){
                    arquivosRecentes.add(recente);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Não foi possível carregar os Arquivos recentemente utilizados pelo Portugol Studio.");
        }

    }
    public void readRecuperaveis(){
        File diretorioTemporario = Configuracoes.getInstancia().getDiretorioTemporario();
        if (!diretorioTemporario.exists()) {
            diretorioTemporario.mkdirs();
        }

        for (File arquivo : diretorioTemporario.listFiles()) {            
            if (!arquivo.isDirectory())
            {
                if(isArquivoRecuperado(arquivo))
                {
                    if(arquivo.getName().contains("Sem título"))
                    {
                        arquivosIniciais.add(arquivo);
                    }
                    else
                    {
                        arquivosRecuperados.add(arquivo);
                    }                    
                }                
            }
        }
    }
    
    public void readOriginais(){
        File f = Configuracoes.getInstancia().getCaminhoArquivosRecuperadosOriginais();
        if(!f.exists())
        {
            return;
        }        
        try {
            String arquivo = FileHandle.open(f);
            String [] caminhos = arquivo.split("\n");
            for (String caminho : caminhos) {
                File original = new File(caminho);
                if(original.exists()){
                    arquivosIniciais.add(original);
                }

            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Não foi possível carregar os Arquivos Originais utilizados pelo Portugol Studio");
        }

    }
    
    public boolean isArquivoRecuperado(File arquivo)
    {
        String fileName = arquivo.getName();
        if(fileName.endsWith(".recuperado"))
        {
            return true;
        } 
        return false;
    }
    
    
    public static PortugolStudio getInstancia()
    {
        if (instancia == null)
        {
            instancia = new PortugolStudio();
        }

        return instancia;
    }
    
    public static boolean isPortugolCarregado() {
    	return portugolCarregado;
    }

    public void iniciarNovaInstancia(String[] parametros)
    {
        LOGGER.log(Level.INFO, "Iniciando nova instancia do PS");
        if (versaoJavaCorreta())
        {
            String dica = obterProximaDica();
            Splash.exibir(dica, 9);

            //inicializarMecanismoLog();
            Splash.definirProgresso(18, "step2.png");

            instalarDetectorExcecoesNaoTratadas();
            Splash.definirProgresso(27, "step3.png");

            processarParametrosLinhaComando(parametros);
            Splash.definirProgresso(36, "step4.png");

            instalarDetectorVialacoesNaThreadSwing();
            Splash.definirProgresso(45, "step4.png");

            LOGGER.log(Level.INFO, "Instalando LAF...");
            definirLookAndFeel();
            Splash.definirProgresso(54, "step5.png");
            LOGGER.log(Level.INFO, "LAF Instalado!");

            LOGGER.log(Level.INFO, "Carregando e configurando fontes...");
            registrarFontes();
            Splash.definirProgresso(63, "step5.png");

            definirFontePadraoInterface();
            Splash.definirProgresso(72, "step6.png");
            LOGGER.log(Level.INFO, "Fontes configuradas!");

            /* 
             * Os plugins devem sempre ser carregados antes de inicializar o Pool de abas, 
             * caso contrário, os plugins não serão corretamente instalado nas abas ao criá-las
             */
            LOGGER.log(Level.INFO, "Carregando plugins e bibliotecas...");
            carregarPlugins();
            Splash.definirProgresso(81, "step7.png");

            carregarBibliotecas();
            Splash.definirProgresso(90, "step8.png");
            LOGGER.log(Level.INFO, "Plugins e bibliotecas carregados!");

            LOGGER.log(Level.INFO, "Inicializando pool de abas...");
            AbaCodigoFonte.inicializarPool();
            Splash.definirProgresso(100, "step9.png");
            LOGGER.log(Level.INFO, "Pool inicializado!");

            try
            {
                LOGGER.log(Level.INFO, "Exibindo tela principal");
                exibirTelaPrincipal();
            }
            catch (ExcecaoAplicacao excecaoAplicacao)
            {
                getTratadorExcecoes().exibirExcecao(excecaoAplicacao);
                finalizar(1);
            }

            Splash.ocultar();
        }
    }
    
    public Queue<File> getRecentFilesQueue(){
        return arquivosRecentes;
    }
    
    public List<File> getArquivosOriginais()
    {
        return arquivosIniciais;
    }

    public Queue<File> getArquivosRecuperados() {
        return arquivosRecuperados;
    }    
    
    public void finalizar(int codigo)
    {
        if (PortugolStudio.getInstancia().isAtualizandoInicializador())
        {
            FabricaDicasInterface.mostrarNotificacao("O Portugol Studio está finalizando uma ação e encerrará em instantes", 2000);
        }
        if (Configuracoes.getInstancia().getDiretorioTemporario().exists())
        {
            FileUtils.deleteQuietly(Configuracoes.getInstancia().getDiretorioTemporario());
        }
        
        while (PortugolStudio.getInstancia().isAtualizandoInicializador())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(PortugolStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Lancador.getInstance().finalizarMutex();
        Configuracoes.getInstancia().salvar();
        System.exit(codigo);
        
        Lancador.getInstance().finalizarServico();
    }

    public String obterProximaDica()
    {
        if (dicas.isEmpty())
        {
            carregarDicas();
            carregarDicasExibidas();
        }

        if (dicasExibidas.size() == dicas.size())
        {
            dicasExibidas.clear();
        }

        if (!dicas.isEmpty())
        {
            int indice = random.nextInt(dicas.size());

            while (dicasExibidas.contains(indice))
            {
                indice = (indice + 1) % dicas.size();
            }

            dicasExibidas.add(indice);
            salvarDicasExibidas();

            return dicas.get(indice);
        }

        return null;
    }

    private void carregarDicas()
    {
        String linha;

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dicas.txt"), "UTF-8")))
        {
            while ((linha = leitor.readLine()) != null)
            {
                if (linha.trim().length() != 0 && !linha.startsWith("#"))
                {
                    dicas.add(linha);
                }
            }
        }
        catch (IOException excecao)
        {
            LOGGER.log(Level.SEVERE, "Erro ao carregar as dicas da Splash Screen", excecao);
        }
    }

    private void carregarDicasExibidas()
    {
        File arquivoDicas = Configuracoes.getInstancia().getCaminhoArquivoDicas();
        String linha;

        if (arquivoDicas.exists())
        {
            try (BufferedReader leitor = new BufferedReader(new FileReader(arquivoDicas)))
            {
                while ((linha = leitor.readLine()) != null)
                {
                    if (linha.trim().length() != 0 && !linha.startsWith("#"))
                    {
                        dicasExibidas.add(Integer.parseInt(linha));
                    }
                }
            }
            catch (IOException excecao)
            {
                LOGGER.log(Level.SEVERE, "Erro ao carregar as dicas já exibidas", excecao);
            }
        }
    }

    private void salvarDicasExibidas()
    {
        if (!dicasExibidas.isEmpty())
        {
            File arquivoDicas = Configuracoes.getInstancia().getCaminhoArquivoDicas();

            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoDicas)))
            {
                for (Integer indice : dicasExibidas)
                {
                    escritor.write(indice.toString());
                    escritor.newLine();
                }
            }
            catch (IOException excecao)
            {
                LOGGER.log(Level.SEVERE, "Erro ao salvar as dicas já exibidas", excecao);
            }
        }
    }
    
    public void salvarComoRecente(File arquivoRecente)
    {
        
        if(arquivosRecentes.contains(arquivoRecente))
        {
            arquivosRecentes.remove(arquivoRecente);
        }
        arquivosRecentes.add(arquivoRecente);
        if(arquivosRecentes.size()>6)
        {
            arquivosRecentes.poll();
        }
        File arquivosRecentesFile = Configuracoes.getInstancia().getCaminhoArquivosRecentes();

        String escritor = "";
        for (File arquivo : arquivosRecentes) {
            escritor += arquivo.getAbsolutePath()+"\n";
        }
        try {
            FileHandle.save(escritor, arquivosRecentesFile);
        } catch (Exception ex) {
            Logger.getLogger(GerenciadorPlugins.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void salvarCaminhoOriginalRecuperado(File arquivoOriginal)
    {
        if(arquivosRecuperadosOriginais.contains(arquivoOriginal))
        {
            arquivosRecuperadosOriginais.remove(arquivoOriginal);
        }
        arquivosRecuperadosOriginais.add(arquivoOriginal);
        
        if(!Configuracoes.getInstancia().getDiretorioTemporario().exists())
        {
            return;
        }        
        File arquivosOriginais = Configuracoes.getInstancia().getCaminhoArquivosRecuperadosOriginais();

        String escritor = "";
        for (File arquivo : arquivosRecuperadosOriginais) {
            escritor += arquivo.getAbsolutePath()+"\n";
        }
        try {
            FileHandle.save(escritor, arquivosOriginais);
        } catch (Exception ex) {
            Logger.getLogger(GerenciadorPlugins.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean versaoJavaCorreta()
    {
        try
        {
            String property = System.getProperty("java.specification.version");

            if (Double.valueOf(property) < 1.7)
            {
                QuestionDialog.getInstance().showMessage("Para executar o Portugol Studio é preciso utilizar o Java 1.7 ou superior.", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }
        catch (HeadlessException | NumberFormatException excecao)
        {
            QuestionDialog.getInstance().showMessage("Não foi possível determinar a versão do Java. O Portugol Studio será encerrado!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // private void inicializarMecanismoLog()
    // {
    //     final InputStream inputStream = TelaPrincipal.class.getResourceAsStream("/logging.properties");

    //     try
    //     {
    //         LogManager.getLogManager().readConfiguration(inputStream);
    //     }
    //     catch (final IOException excecao)
    //     {
    //         Logger.getAnonymousLogger().severe("Não foi possível localizar o arquivo de configuração de log 'logging.properties'");
    //         Logger.getAnonymousLogger().log(Level.SEVERE, excecao.getMessage(), excecao);
    //     }
    // }

    private void instalarDetectorExcecoesNaoTratadas()
    {
        Thread.setDefaultUncaughtExceptionHandler(getTratadorExcecoes());
    }

    private void processarParametrosLinhaComando(final String[] parametros)
    {
        if (parametros != null)
        {
            processarParametroModoDepuracao(parametros);
            processarParametroArquivosIniciais(parametros);
            processarParametroDiretoriosPlugins(parametros);
            processarParametroUriAtualizacao(parametros);
        }
    }

    private void processarParametroUriAtualizacao(final String[] parametros)
    {
        if (parametroExiste("-atualizacao=*", parametros))
        {
            String parametro = obterParametro("-atualizacao=*", parametros);

            String uri = parametro.split("=")[1].trim();

            if (uri.length() > 0)
            {
                try
                {
                    Configuracoes.getInstancia().setUriAtualizacao(new URI(uri).toString());
                }
                catch (URISyntaxException excecao)
                {

                }
            }
        }
    }

    private void processarParametroDiretoriosPlugins(final String[] parametros)
    {
        if (parametroExiste("-plugins=*", parametros))
        {
            String parametro = obterParametro("-plugins=*", parametros);

            String descDiretorios = parametro.split("=")[1];
            String[] diretorios = descDiretorios.split(",");

            if (diretorios != null && diretorios.length > 0)
            {
                for (String diretorio : diretorios)
                {
                    diretoriosPluginsInformadosPorParametro.add(new File(diretorio));
                }
            }
        }
    }

    private void processarParametroModoDepuracao(final String[] parametros)
    {
        setDepurando(true);
    }

    private boolean parametroExiste(String nome, String[] parametros)
    {
        for (String parametro : parametros)
        {
            if (nome.endsWith("*") && parametro.startsWith(nome.replace("*", "")))
            {
                return true;
            }
            else
            {
                if (!nome.endsWith("*") && parametro.equals(nome))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void processarParametroArquivosIniciais(String[] argumentos)
    {
        if (argumentos != null && argumentos.length > 0)
        {
            for (String argumento : argumentos)
            {
                File arquivo = new File(argumento);

                if (arquivo.exists() && arquivo.isFile() && arquivo.canRead())
                {
                    arquivosIniciais.add(arquivo);
                }
            }
        }
    }

    private void instalarDetectorVialacoesNaThreadSwing()
    {
        if (Configuracoes.rodandoEmDesenvolvimento()) 
        {
            RepaintManager.setCurrentManager(new DetectorViolacoesThreadSwing());
        }
    }

    private void definirLookAndFeel()
    {

        SwingUtilities.invokeLater(() ->
        {
            try
            {
                WeblafUtils.instalaWeblaf();
                FabricaDeFileChooser.inicializar();//cria as instâncias de JFileChooser com o look and feel do sistema antes que o WebLaf seja instalado
            }
            catch (Exception e)
            {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

    }

    private void registrarFontes()
    {
        final String path = "br/univali/ps/ui/fontes/";

        final String[] fontes
                =
                {
                    "OpenSans-Bold.ttf",
                    "OpenSans-Italic.ttf",
                    "OpenSans-Regular.ttf",
                    "dejavu_sans_mono.ttf",
                    "dejavu_sans_mono_bold.ttf",
                    "dejavu_sans_mono_bold_oblique.ttf",
                    "dejavu_sans_mono_oblique.ttf",
                    "tahoma.ttf",
                    "tahomabd.ttf"
                };

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        GraphicsEnvironment ambienteGrafico = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String nome : fontes)
        {
            try
            {
                Font fonte = Font.createFont(Font.TRUETYPE_FONT, classLoader.getResourceAsStream(path + nome));
                ambienteGrafico.registerFont(fonte);
            }
            catch (FontFormatException | IOException excecao)
            {
                final String mensagem = String.format("Não foi possível registrar a fonte '%s' no ambiente", nome);

                LOGGER.log(Level.INFO, mensagem, excecao);
            }
        }
    }

    private void definirFontePadraoInterface()
    {
//        try
//        {
//            SwingUtilities.invokeAndWait(() ->
//            {
//                Enumeration keys = UIManager.getDefaults().keys();
//
//                while (keys.hasMoreElements())
//                {
//                    Object key = keys.nextElement();
//                    Object value = UIManager.get(key);
//
//                    if (value instanceof javax.swing.plaf.FontUIResource)
//                    {
//                        /*
//                         * Não está funcionando. O swing altera a fonte padrão para a maioria dos componentes,
//                         * mas não todos. Além disso, o tamanho da fonte da árvore estrutural para de funcionar
//                         *
//                         */
//                        //UIManager.put(key, new Font("Tahoma", Font.PLAIN, 11));
//                    }
//                }
//            });
//        }
//        catch (InterruptedException | InvocationTargetException excecao)
//        {
//            LOGGER.log(Level.INFO, "Não foi possível definir uma fonte padrão na interface do usuário", excecao);
//        }
    }
    
    public void carregarRecuperados(){
        List<File> files = PortugolStudio.getInstancia().getArquivosOriginais();
        PortugolStudio.getInstancia().getTelaPrincipal().abrirArquivosCodigoFonte(files);
    }
    
    private void removerPluginsDefinidos(File removerPlugins)
    {
        if(removerPlugins.exists())
        {
            try {
            String arquivo = FileHandle.open(removerPlugins);
            FileUtils.forceDelete(removerPlugins);
            String [] caminhos = arquivo.split("\n");
            for (String caminho : caminhos) {
                File pastaPlugin = new File(caminho);
                if(pastaPlugin.exists() && pastaPlugin.isDirectory())
                {
                    FileUtils.deleteDirectory(pastaPlugin);
                }
            }            
            } catch (Exception ex) {
                PortugolStudio.getInstancia().tratadorExcecoes.exibirExcecao(ex);
            }
        }
    }

    private void carregarPlugins()
    {
        GerenciadorPlugins gerenciadorPlugins = GerenciadorPlugins.getInstance();
        Configuracoes configuracoes = Configuracoes.getInstancia();
        
        removerPluginsDefinidos(new File(configuracoes.getDiretorioConfiguracoes(), "desinstalarPlugins.txt"));

        if (configuracoes.getDiretorioPlugins() != null)
        {
            File diretorioPlugins = configuracoes.getDiretorioPlugins();

            LOGGER.log(Level.INFO, "Inicializando plugins em: {0}", diretorioPlugins.getAbsolutePath());

            if (diretorioPlugins.exists())
            {
                for (File pastaPlugin : listarPastasPlugins(diretorioPlugins))
                {
                    LOGGER.log(Level.INFO, "Inicializando plugin {0}", pastaPlugin.getAbsolutePath());
                    gerenciadorPlugins.incluirDiretorioPlugin(pastaPlugin);
                }
            }
        }

        for (File diretorio : diretoriosPluginsInformadosPorParametro)
        {
            gerenciadorPlugins.incluirDiretorioPlugin(diretorio);
        }

        gerenciadorPlugins.carregarPlugins();
    }

    private void carregarBibliotecas()
    {
        try 
        {
            Portugol.getGerenciadorBibliotecas().registrarBibliotecaExterna(br.univali.ps.nucleo.biblioteca.PortugolStudio.class);
        }
        catch (ErroCarregamentoBiblioteca ex)
        {
            Logger.getLogger(PortugolStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<File> listarPastasPlugins(File diretorioPlugins)
    {
        File[] diretorios = diretorioPlugins.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });

        return Arrays.asList(diretorios);
    }

    private void exibirTelaPrincipal() throws ExcecaoAplicacao
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    
                    Lancador.getInstance().getJFrame().setUndecorated(true);
                    outSidePanel = new OutsidePanel();
                    Lancador.getInstance().getJFrame().add(outSidePanel);
                    telaPrincipal = outSidePanel.getTelaPrincipal();
                    telaPrincipal.setArquivosIniciais(arquivosIniciais);
                    Lancador.getInstance().getJFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    Lancador.getInstance().getJFrame().pack();
                    Lancador.getInstance().getJFrame().setLocationRelativeTo(null);
                    Lancador.getInstance().getJFrame().setVisible(true);
                    Lancador.getInstance().setOlderSize(new Dimension(800, 600));
                    Lancador.getInstance().maximize(true);
                    
                    Lancador.getInstance().getJFrame().revalidate();
                    portugolCarregado = true;
                }
            });
        }
        catch (InterruptedException | InvocationTargetException ex)
        {
            throw new ExcecaoAplicacao("Não foi possível iniciar o Portugol Studio", ex, ExcecaoAplicacao.Tipo.ERRO_PROGRAMA);
        }
    }

    public String getVersao()
    {
        if (versao == null)
        {
            versao = carregarVersao();
        }

        return versao;
    }

    private String carregarVersao()
    {
        try
        {
            Properties propriedades = new Properties();
            propriedades.load(getClass().getClassLoader().getResourceAsStream("version.properties"));

            StringBuilder version = new StringBuilder();
            
            version.append(propriedades.getProperty("majorVersion"));
            version.append(".");
            version.append(propriedades.getProperty("minorVersion"));
            version.append(".");
            version.append(propriedades.getProperty("buildVersion"));
            
            if (propriedades.containsKey("revisionVersion"))
            {
                version.append(".");
                version.append(propriedades.getProperty("revisionVersion"));
            }
            
            if (propriedades.containsKey("releaseName") && !propriedades.getProperty("releaseName").trim().isEmpty())
            {
                version.append(" ");
                version.append(propriedades.getProperty("releaseName"));
            }
            
            return version.toString();
        }
        catch (IOException excecao)
        {
            LOGGER.log(Level.SEVERE, "Erro ao carregar o arquivo de versão", excecao);
        }

        return "Indefinida";
    }

    public TelaPrincipal getTelaPrincipal()
    {
        if (telaPrincipal == null)
        {
            telaPrincipal = new TelaPrincipal();
            telaPrincipal.setArquivosIniciais(arquivosIniciais);
        }
        return telaPrincipal;
    }

    public boolean isDepurando()
    {
        return depurando;
    }

    public void setDepurando(boolean depurando)
    {
        this.depurando = depurando;
    }

    public TratadorExcecoes getTratadorExcecoes()
    {
        if (tratadorExcecoes == null)
        {
            tratadorExcecoes = new TratadorExcecoes();
        }

        return tratadorExcecoes;
    }

    public JDialog getTelaSobre()
    {
        if (telaSobre == null)
        {
            telaSobre = new TelaCustomBorder(new Sobre(), "Sobre");
            
        }

        telaSobre.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaSobre;
    }
    public JDialog getTelaRelatarBug()
    {
        if (telaRelatarBug == null)
        {
            telaRelatarBug = new TelaCustomBorder(new TelaRelatarBug(), "Relatar Bug");            
        }

        telaRelatarBug.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaRelatarBug;
    }

    public JDialog getTelaAtalhosTeclado()
    {
        if (telaAtalhosTeclado == null)
        {
            telaAtalhosTeclado = new TelaCustomBorder(new TelaAtalhos(), "Atalhos de Teclado");
        }

        telaAtalhosTeclado.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaAtalhosTeclado;
    }
    
    public JDialog getTelaDicas()
    {
        if (telaDicas == null)
        {
            telaDicas = new TelaCustomBorder(new TelaDicas(), "Dicas");
        }

        telaDicas.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaDicas;
    }
    
    public JDialog getTelaPluginsInstalados()
    {
        if (telaPluginsInstalados == null)
        {
            telaPluginsInstalados = new TelaCustomBorder("Plugins Instalados");
            telaPluginsInstalados.setPanel(new PainelPluginsInstalados(telaPluginsInstalados));
        }

        telaPluginsInstalados.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaPluginsInstalados;
    }

    public TelaCustomBorder getTelaInformacoesPlugin()
    {
        if (telaInformacoesPlugin == null)
        {
            telaInformacoesPlugin = new TelaCustomBorder("Informações do Plugin");
            TelaInformacoesPlugin painelInformacoesPlugin = new TelaInformacoesPlugin();
            telaInformacoesPlugin.setPanel(painelInformacoesPlugin);
        }
        telaInformacoesPlugin.setPreferredSize(new Dimension(640, 480));
        telaInformacoesPlugin.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaInformacoesPlugin;
    }

    public TelaErrosPluginsBibliotecas getTelaErrosPluginsBibliotecas()
    {
        if (telaErrosPluginsBibliotecas == null)
        {
            telaErrosPluginsBibliotecas = new TelaErrosPluginsBibliotecas();
        }

        telaErrosPluginsBibliotecas.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaErrosPluginsBibliotecas;
    }

    public JDialog getTelaLicencas()
    {
        if (telaLicencas == null)
        {
            telaLicencas = new TelaCustomBorder("Licenças") ;
            telaLicencas.setPanel(new TelaLicencas(telaLicencas));
            
            telaLicencas.setSize(640, 550);
        }

        telaLicencas.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaLicencas;
    }
    
    public JDialog getTelaEditarTemas()
    {
        if (telaEditarTemas == null)
        {
            telaEditarTemas = new TelaCustomBorder("Editar Temas") ;
            telaEditarTemas.setPanel(new TelaEditarTemas(telaEditarTemas));
            
            telaEditarTemas.setSize(640, 550);
        }

        telaEditarTemas.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaEditarTemas;
    }
    
    public JDialog getTelaPesquisarSubstituir()
    {
        if (telaPesquisarSubstituir == null)
        {
            telaPesquisarSubstituir = new TelaCustomBorder("Localizar e Substituir") ;
            PSFindReplace findReplace = new PSFindReplace(telaPesquisarSubstituir);
            findReplace.setPreferredSize(new Dimension(550, 190));
            telaPesquisarSubstituir.setPanel(findReplace);
        }

        telaPesquisarSubstituir.setLocationRelativeTo(Lancador.getInstance().getJFrame());

        return telaPesquisarSubstituir;
    }
    
    public PSFindReplace getTelaProcurarSubstituirPanel()
    {
        if (telaPesquisarSubstituir == null)
        {
            telaPesquisarSubstituir = new TelaCustomBorder("Localizar e Substituir") ;
            PSFindReplace findReplace = new PSFindReplace(telaPesquisarSubstituir);
            findReplace.setPreferredSize(new Dimension(550, 190));
            telaPesquisarSubstituir.setPanel(findReplace);
        }
        
        telaPesquisarSubstituir.setLocationRelativeTo(Lancador.getInstance().getJFrame());
        
        return (PSFindReplace) telaPesquisarSubstituir.getPanel();
    }

    public TelaRenomearSimbolo getTelaRenomearSimboloPanel()
    {
        if (telaRenomearSimbolo == null)
        {
            telaRenomearSimbolo = new TelaCustomBorder("renomear");
            telaRenomearSimbolo.setPanel(new TelaRenomearSimbolo(telaRenomearSimbolo));
        }
        
        telaRenomearSimbolo.setLocationRelativeTo(Lancador.getInstance().getJFrame());
        
        return (TelaRenomearSimbolo) telaRenomearSimbolo.getPanel();
    }

    public synchronized boolean isAtualizandoInicializador()
    {
        return atualizandoInicializador;
    }

    public synchronized void setAtualizandoInicializador(boolean atualizandoInicializador)
    {
        this.atualizandoInicializador = atualizandoInicializador;
    }

    private String obterParametro(String nome, String[] parametros)
    {
        for (String parametro : parametros)
        {
            if (nome.endsWith("*") && parametro.startsWith(nome.replace("*", "")))
            {
                return parametro;
            }
            else
            {
                if (!nome.endsWith("*") && parametro.equals(nome))
                {
                    return parametro;
                }
            }
        }

        return null;
    }

    // private void exibirParametros(String[] parametros)
    // {
    //     for (String parametro : parametros)
    //     {
    //         LOGGER.log(Level.INFO, "Parametro: {0}", parametro);
    //     }
    // }
        
}
