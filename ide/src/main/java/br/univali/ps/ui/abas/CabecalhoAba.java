package br.univali.ps.ui.abas;

import br.univali.ps.ui.swing.ColorController;
import br.univali.ps.ui.swing.Themeable;
import br.univali.ps.ui.swing.weblaf.WeblafUtils;
import br.univali.ps.ui.utils.FabricaDicasInterface;
import br.univali.ps.ui.utils.IconFactory;
import com.alee.laf.button.WebButtonUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CabecalhoAba extends JPanel implements Themeable{
    private final Aba aba;
    int maxWidth = 999;
    public CabecalhoAba(Aba aba)
    {
        initComponents();
        this.aba = aba;
        if(WeblafUtils.weblafEstaInstalado()){
            ((WebButtonUI)botaoFechar.getUI()).setUndecorated(true);
        }
        configurarCores();
        configuraIcones();
    }
    
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2d = (Graphics2D) grphcs;
        if(this instanceof BotoesControleAba){
            if(getAba().isSelected()){
                g2d.setPaint(ColorController.FUNDO_MEDIO);
            }else{
                g2d.setPaint(ColorController.COR_CONSOLE);
            }
            
        }else if(getAba().isRemovivel()){
            if(getAba().isSelected()){
                g2d.setPaint(ColorController.COR_PRINCIPAL);
            }else{
                g2d.setPaint(ColorController.FUNDO_CLARO);
            }
        }else{
            if(getAba().isSelected()){
                g2d.setPaint(ColorController.COR_CONSOLE);
            }else{
                g2d.setPaint(ColorController.COR_PRINCIPAL);
            }
        }
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        
        if(this instanceof BotoesControleAba){
            
        }else if(getAba().isRemovivel()){
            if(getAba().isSelected()){
                g2d.setPaint(ColorController.AMARELO);
            }else{
                g2d.setPaint(ColorController.FUNDO_CLARO);
            }
        }else{
            
        }
        g2d.fillRect(0, 0, getWidth(), 1);
        if(this instanceof BotoesControleAba){
            
        }else if(getAba().isRemovivel()){
                g2d.setPaint(ColorController.FUNDO_ESCURO);
        }else{
        }
        g2d.fillRect(0, 0, 1, getHeight());

    }
    
    @Override
    public void configurarCores(){
        jLTitulo.setForeground(ColorController.COR_LETRA);
        setBackground(ColorController.FUNDO_MEDIO);
    }
    
    private void configuraIcones()
    {
        botaoFechar.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "white_close.png"));        
    }
    
    protected Aba getAba()
    {
        return aba;
    }

    public void setTitulo(String titulo)
    {
        this.jLTitulo.setText(titulo);
        FabricaDicasInterface.criarTooltipEstatica(botaoFechar, "Fechar - "+getTitulo());
    }

    public void setIcone(Icon icone)
    {
        this.jLIcone.setIcon(icone);
    }

    public void setBotaoFecharVisivel(boolean removivel)
    {
        botaoFechar.setVisible(removivel);
    }

    public String getTitulo()
    {
        return jLTitulo.getText();
    }

    public Icon getIcone()
    {
        return jLIcone.getIcon();
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize); //To change body of generated methods, choose Tools | Templates.
        if(this.getPreferredSize().width<(jLIcone.getPreferredSize().getWidth() + botaoFechar.getPreferredSize().getWidth()))
        {
            jLIcone.setVisible(false);
            jLTitulo.setVisible(false);
        }
        else
        {
            jLIcone.setVisible(true);
            jLTitulo.setVisible(true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLIcone = new javax.swing.JLabel();
        jLTitulo = new javax.swing.JLabel();
        botaoFechar = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 0, 4));
        setFocusable(false);
        setMaximumSize(new java.awt.Dimension(300, 50));
        setMinimumSize(new java.awt.Dimension(110, 30));
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.GridBagLayout());

        jLIcone.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLIcone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/light_pix.png"))); // NOI18N
        jLIcone.setAlignmentX(0.5F);
        jLIcone.setMaximumSize(new java.awt.Dimension(18, 18));
        jLIcone.setName("labelIcone"); // NOI18N
        jLIcone.setPreferredSize(new java.awt.Dimension(17, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 6;
        add(jLIcone, gridBagConstraints);

        jLTitulo.setText("jLabel2");
        jLTitulo.setFocusable(false);
        jLTitulo.setName("labelTitulo"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jLTitulo, gridBagConstraints);

        botaoFechar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/pequeno/white_close.png"))); // NOI18N
        botaoFechar.setBorder(null);
        botaoFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botaoFechar.setFocusable(false);
        botaoFechar.setName("botaoFechar"); // NOI18N
        botaoFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoFecharActionPerformed(evt);
            }
        });
        add(botaoFechar, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    private void botaoFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoFecharActionPerformed
        aba.fechar();
    }//GEN-LAST:event_botaoFecharActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botaoFechar;
    private javax.swing.JLabel jLIcone;
    private javax.swing.JLabel jLTitulo;
    // End of variables declaration//GEN-END:variables
    public int getTamanhoCabecalho(){
        setBorder(new EmptyBorder(2, 2, 2, 2));
        int larguraBotao = (botaoFechar.isVisible()) ? botaoFechar.getPreferredSize().width : 0;
        int larguraIcone = jLIcone.getPreferredSize().width;
        int larguraTitulo = jLTitulo.getPreferredSize().width;
        return larguraIcone + larguraTitulo + larguraBotao + 10;
    }
    
    public void setMaxWidth(int max){
        this.maxWidth = max;
    }

    boolean isBotaoFecharVisivel()
    {
        return botaoFechar.isVisible();
    }
}
