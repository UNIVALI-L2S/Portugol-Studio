/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.univali.ps.ui.telas;

import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.ps.nucleo.Configuracoes;
import br.univali.ps.ui.ColorController;
import br.univali.ps.ui.Themeable;
import br.univali.ps.ui.telas.utils.DicaInterface;
import br.univali.ps.ui.utils.IconFactory;
import br.univali.ps.ui.weblaf.WeblafUtils;
import com.alee.extended.image.DisplayType;
import com.alee.extended.image.WebImage;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 *
 * @author LITE
 */
public class TelaDicas extends JDialog implements Themeable{

    private final List<DicaInterface> dicas;
    private Integer item=0;
    
    /**
     * Creates new form TelaDicas
     */
    public TelaDicas() {
        initComponents();
        configurarCores();
        this.setIconImage(IconFactory.getDefaultWindowIcon());
        this.setTitle("Dicas de Inteface");
        String dir = IconFactory.CAMINHO_IMAGENS+"/dicas";
        dicas = loadHints(dir);
        atualiza(item);
        exibirSempre.setSelected(true);
        
        configurarNavegacaoPeloTeclado();
        setModal(true);
    }
    
    @Override
    public void configurarCores(){
        if(WeblafUtils.weblafEstaInstalado()){
            WeblafUtils.configurarBotao(webButton1, ColorController.FUNDO_CLARO, Color.white, ColorController.FUNDO_ESCURO, Color.orange, 15);
            WeblafUtils.configurarBotao(webButton2, ColorController.FUNDO_CLARO, Color.white, ColorController.FUNDO_ESCURO, Color.orange, 15);
        }
        titleLabel.setBackground(ColorController.FUNDO_ESCURO);
        mainPanel.setBackground(ColorController.FUNDO_CLARO);
        descriptionLabel.setForeground(ColorController.COR_LETRA);
        exibirSempre.setForeground(ColorController.COR_LETRA);
    }
    
    private void configurarNavegacaoPeloTeclado()
    {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Proxima");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Anterior");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Fechar");
        
        actionMap.put("Proxima", new AbstractAction() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                webButton2.doClick();
            }
        });
        
        
        actionMap.put("Anterior", new AbstractAction() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                webButton1.doClick();
            }
        });
        
         actionMap.put("Fechar", new AbstractAction() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                dispose();
            }
        });
    }
    
    private void atualiza(Integer indice){
        
        imagePane.removeAll();
        DicaInterface dicaInterface=dicas.get(indice);
        WebImage image = new WebImage(dicaInterface.getImagem());
        image.setDisplayType ( DisplayType.fitComponent );
        imagePane.add(image);
        titleLabel.setText("<html><head></head><body>"+(indice+1)+"/"+dicas.size()+" - "+dicaInterface.getTitulo()+"</body></html>");
        descriptionLabel.setText("<html><head></head><body>"+dicaInterface.getDescricao()+"</body></html>");
    }
    
    private List<DicaInterface> loadHints(String dir){
        List<DicaInterface> lista = new ArrayList<>();
        Properties prop = new Properties();
        try {
            InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(dir+"/index.properties");
            prop.load(new InputStreamReader(resourceAsStream, "UTF-8"));
            for(int i=0; i<Integer.parseInt(prop.getProperty("dicas")); i++){
                String nome = "dica"+i+".";
                String titulo = prop.getProperty(nome+"title");
                String descricao = prop.getProperty(nome+"description");
                InputStream imageStream = ClassLoader.getSystemClassLoader().getResourceAsStream(dir+"/"+prop.getProperty(nome+"image"));
                Image imagem = ImageIO.read(imageStream);
                DicaInterface dica = new DicaInterface(titulo, descricao, imagem);
                lista.add(dica);
            }
            return lista;
        }
        catch(Exception e){
            
        }
        return null;
    }

    public static void main(String[] args) {
        TelaDicas dicas = new TelaDicas();
        dicas.setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        carrouselPane = new javax.swing.JPanel();
        hintPane = new javax.swing.JPanel();
        imagePane = new javax.swing.JPanel();
        descriptionPane = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        webButton1 = new com.alee.laf.button.WebButton();
        jPanel3 = new javax.swing.JPanel();
        webButton2 = new com.alee.laf.button.WebButton();
        scrollPane = new javax.swing.JPanel();
        optionPane = new javax.swing.JPanel();
        exibirSempre = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(228, 241, 254));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainPanel.setMinimumSize(new java.awt.Dimension(640, 480));
        mainPanel.setPreferredSize(new java.awt.Dimension(640, 550));
        mainPanel.setLayout(new java.awt.BorderLayout());

        carrouselPane.setOpaque(false);
        carrouselPane.setLayout(new java.awt.BorderLayout());

        hintPane.setOpaque(false);
        hintPane.setLayout(new java.awt.BorderLayout());

        imagePane.setBackground(new java.awt.Color(51, 51, 51));
        imagePane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        imagePane.setLayout(new java.awt.BorderLayout());
        hintPane.add(imagePane, java.awt.BorderLayout.CENTER);

        descriptionPane.setOpaque(false);
        descriptionPane.setLayout(new java.awt.BorderLayout());

        titleLabel.setBackground(new java.awt.Color(49, 104, 146));
        titleLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(255, 255, 255));
        titleLabel.setText("Título");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        titleLabel.setOpaque(true);
        descriptionPane.add(titleLabel, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());

        descriptionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        descriptionLabel.setMaximumSize(new java.awt.Dimension(34, 150));
        descriptionLabel.setMinimumSize(new java.awt.Dimension(34, 125));
        descriptionLabel.setPreferredSize(new java.awt.Dimension(34, 125));
        jPanel1.add(descriptionLabel, java.awt.BorderLayout.CENTER);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/grande/previous.png"))); // NOI18N
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(webButton1, new java.awt.GridBagConstraints());

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_START);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.GridBagLayout());

        webButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/grande/next.png"))); // NOI18N
        webButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton2ActionPerformed(evt);
            }
        });
        jPanel3.add(webButton2, new java.awt.GridBagConstraints());

        jPanel1.add(jPanel3, java.awt.BorderLayout.LINE_END);

        descriptionPane.add(jPanel1, java.awt.BorderLayout.CENTER);

        hintPane.add(descriptionPane, java.awt.BorderLayout.SOUTH);

        carrouselPane.add(hintPane, java.awt.BorderLayout.CENTER);

        scrollPane.setOpaque(false);
        scrollPane.setLayout(new java.awt.BorderLayout());
        carrouselPane.add(scrollPane, java.awt.BorderLayout.SOUTH);

        mainPanel.add(carrouselPane, java.awt.BorderLayout.CENTER);

        optionPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
        optionPane.setOpaque(false);
        optionPane.setPreferredSize(new java.awt.Dimension(0, 30));
        optionPane.setLayout(new java.awt.BorderLayout());

        exibirSempre.setBackground(new java.awt.Color(250, 250, 250));
        exibirSempre.setText("Mostrar Dicas ao Iniciar");
        exibirSempre.setOpaque(false);
        exibirSempre.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                exibirSempreStateChanged(evt);
            }
        });
        optionPane.add(exibirSempre, java.awt.BorderLayout.CENTER);

        mainPanel.add(optionPane, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        item--;
        if(item<0){
            item=dicas.size()-1;
        }
        atualiza(item);
    }//GEN-LAST:event_webButton1ActionPerformed

    private void webButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton2ActionPerformed
       item=(item+1)%dicas.size();
       atualiza(item);
    }//GEN-LAST:event_webButton2ActionPerformed

    private void exibirSempreStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_exibirSempreStateChanged
        Configuracoes configuracoes = Configuracoes.getInstancia();
        configuracoes.setExibirDicasInterface(exibirSempre.isSelected());
    }//GEN-LAST:event_exibirSempreStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel carrouselPane;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPane;
    private javax.swing.JCheckBox exibirSempre;
    private javax.swing.JPanel hintPane;
    private javax.swing.JPanel imagePane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel optionPane;
    private javax.swing.JPanel scrollPane;
    private javax.swing.JLabel titleLabel;
    private com.alee.laf.button.WebButton webButton1;
    private com.alee.laf.button.WebButton webButton2;
    // End of variables declaration//GEN-END:variables
}
