/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.univali.ps.ui.editor;

import br.univali.ps.nucleo.Configuracoes;
import br.univali.ps.ui.swing.ColorController;
import br.univali.ps.ui.swing.Themeable;
import br.univali.ps.ui.swing.weblaf.WeblafUtils;
import br.univali.ps.ui.utils.FabricaDicasInterface;
import com.alee.laf.button.WebButton;

/**
 *
 * @author Adson Esteves
 */
public class MiniBarraDeFerramentas extends javax.swing.JPanel implements Themeable{

    /**
     * Creates new form MiniBarraDeFerramentas
     */
    public MiniBarraDeFerramentas() {
        initComponents();
        configurarCores();
        configurarTooltips();
        right.setRight();
    }
    
    public void configurarTooltips(){
        FabricaDicasInterface.criarTooltip(botaoPlay, "Executa o programa até o próximo ponto de parada");
        FabricaDicasInterface.criarTooltip(botaoAjuda, "Abre a ajuda com sintaxe e bibliotecas");
        FabricaDicasInterface.criarTooltip(botaoParar, "Interrompe a execução/depuração do programa atual");
        FabricaDicasInterface.criarTooltip(botaoPasso, "Executa o programa passo a passo");
        FabricaDicasInterface.criarTooltip(botaoSalvar, "Salva o programa atual no computador, em uma pasta escolhida pelo usuário");
        FabricaDicasInterface.criarTooltip(botaoSalvarComo, "Salva uma nova cópia do programa atual no computador, em uma pasta escolhida pelo usuário");
        FabricaDicasInterface.criarTooltip(botaoAbrir, "Abre um arquivo .por");
        FabricaDicasInterface.criarTooltip(botaoRetrair, "Sai do modo expandido");
    }

    @Override
    public void configurarCores() {
        
        
        if(Configuracoes.getInstancia().isTemaDark()){
            jPanel1.setBackground(ColorController.FUNDO_BOTOES_EXPANSIVEIS);
            if (WeblafUtils.weblafEstaInstalado()) {
                WeblafUtils.configurarBotao(botaoRetrair, ColorController.FUNDO_BOTOES_EXPANSIVEIS, ColorController.COR_LETRA, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, 5);
            }
        }else{
            jPanel1.setBackground(ColorController.FUNDO_ESCURO);
            if (WeblafUtils.weblafEstaInstalado()) {
                WeblafUtils.configurarBotao(botaoRetrair, ColorController.FUNDO_ESCURO, ColorController.COR_LETRA, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, 5);
            }
        }
        if (WeblafUtils.weblafEstaInstalado()) {
            WeblafUtils.configurarBotao(botaoPlay, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoAjuda, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoParar, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoPasso, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoSalvar, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoSalvarComo, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            WeblafUtils.configurarBotao(botaoAbrir, ColorController.COR_PRINCIPAL, ColorController.COR_LETRA, ColorController.COR_DESTAQUE, ColorController.COR_LETRA, 5);
            
        }
        
        
    }

    public WebButton getBotaoRetrair() {
        return botaoRetrair;
    }

    public void setBotaoRetrair(WebButton botaoRetrair) {
        this.botaoRetrair = botaoRetrair;
    }
    
    public WebButton getBotaoAbrir() {
        return botaoAbrir;
    }

    public void setBotaoAbrir(WebButton botaoAbrir) {
        this.botaoAbrir = botaoAbrir;
    }

    public WebButton getBotaoAjuda() {
        return botaoAjuda;
    }

    public void setBotaoAjuda(WebButton botaoAjuda) {
        this.botaoAjuda = botaoAjuda;
    }

    public WebButton getBotaoParar() {
        return botaoParar;
    }

    public void setBotaoParar(WebButton botaoParar) {
        this.botaoParar = botaoParar;
    }

    public WebButton getBotaoPasso() {
        return botaoPasso;
    }

    public void setBotaoPasso(WebButton botaoPasso) {
        this.botaoPasso = botaoPasso;
    }

    public WebButton getBotaoPlay() {
        return botaoPlay;
    }

    public void setBotaoPlay(WebButton botaoPlay) {
        this.botaoPlay = botaoPlay;
    }

    public WebButton getBotaoSalvar() {
        return botaoSalvar;
    }

    public void setBotaoSalvar(WebButton botaoSalvar) {
        this.botaoSalvar = botaoSalvar;
    }

    public WebButton getBotaoSalvarComo() {
        return botaoSalvarComo;
    }

    public void setBotaoSalvarComo(WebButton botaoSalvarComo) {
        this.botaoSalvarComo = botaoSalvarComo;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        right = new br.univali.ps.ui.editor.minibar.DiagonalSidePanel();
        left = new br.univali.ps.ui.editor.minibar.DiagonalSidePanel();
        jPanel1 = new javax.swing.JPanel();
        botaoPlay = new com.alee.laf.button.WebButton();
        botaoPasso = new com.alee.laf.button.WebButton();
        botaoParar = new com.alee.laf.button.WebButton();
        botaoSalvar = new com.alee.laf.button.WebButton();
        botaoSalvarComo = new com.alee.laf.button.WebButton();
        botaoAjuda = new com.alee.laf.button.WebButton();
        botaoAbrir = new com.alee.laf.button.WebButton();
        botaoRetrair = new com.alee.laf.button.WebButton();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        right.setMaximumSize(null);
        right.setMinimumSize(null);
        right.setPreferredSize(new java.awt.Dimension(20, 39));

        javax.swing.GroupLayout rightLayout = new javax.swing.GroupLayout(right);
        right.setLayout(rightLayout);
        rightLayout.setHorizontalGroup(
            rightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        rightLayout.setVerticalGroup(
            rightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );

        add(right, java.awt.BorderLayout.LINE_END);

        left.setMaximumSize(null);
        left.setMinimumSize(null);
        left.setPreferredSize(new java.awt.Dimension(20, 39));

        javax.swing.GroupLayout leftLayout = new javax.swing.GroupLayout(left);
        left.setLayout(leftLayout);
        leftLayout.setHorizontalGroup(
            leftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        leftLayout.setVerticalGroup(
            leftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        add(left, java.awt.BorderLayout.LINE_START);

        botaoPlay.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/resultset_next.png"))); // NOI18N
        botaoPlay.setFocusable(false);
        jPanel1.add(botaoPlay);

        botaoPasso.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoPasso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/step.png"))); // NOI18N
        botaoPasso.setFocusable(false);
        jPanel1.add(botaoPasso);

        botaoParar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoParar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/stop.png"))); // NOI18N
        botaoParar.setFocusable(false);
        jPanel1.add(botaoParar);

        botaoSalvar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/save.png"))); // NOI18N
        botaoSalvar.setFocusable(false);
        jPanel1.add(botaoSalvar);

        botaoSalvarComo.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoSalvarComo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/save_as.png"))); // NOI18N
        botaoSalvarComo.setFocusable(false);
        jPanel1.add(botaoSalvarComo);

        botaoAjuda.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoAjuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/helplow.png"))); // NOI18N
        botaoAjuda.setFocusable(false);
        jPanel1.add(botaoAjuda);

        botaoAbrir.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        botaoAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/folder_closed.png"))); // NOI18N
        botaoAbrir.setFocusable(false);
        jPanel1.add(botaoAbrir);

        botaoRetrair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/restaurar_componente.png"))); // NOI18N
        jPanel1.add(botaoRetrair);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.alee.laf.button.WebButton botaoAbrir;
    private com.alee.laf.button.WebButton botaoAjuda;
    private com.alee.laf.button.WebButton botaoParar;
    private com.alee.laf.button.WebButton botaoPasso;
    private com.alee.laf.button.WebButton botaoPlay;
    private com.alee.laf.button.WebButton botaoRetrair;
    private com.alee.laf.button.WebButton botaoSalvar;
    private com.alee.laf.button.WebButton botaoSalvarComo;
    private javax.swing.JPanel jPanel1;
    private br.univali.ps.ui.editor.minibar.DiagonalSidePanel left;
    private br.univali.ps.ui.editor.minibar.DiagonalSidePanel right;
    // End of variables declaration//GEN-END:variables
}
