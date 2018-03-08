package br.univali.ps.ui.imagens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Fillipi
 */
public class Gradiente extends JPanel {

    private static final int scale = 2;
    private Color cor2 = new Color(84, 147, 190);
    private Color cor1 = new Color(4, 52, 88);
    private int size = (cor2.getRed() - cor1.getRed()) * scale;

    private void calcularTamanho()
    {
        this.size = (cor2.getRed() - cor1.getRed()) * scale;
    }
    
    public void setCor1(Color cor1)
    {
        this.cor1 = cor1;
        this.calcularTamanho();
    }

    public void setCor2(Color cor2)
    {
        this.cor2 = cor2;
        this.calcularTamanho();
    }

    public Color getCor1()
    {
        return cor1;
    }

    public Color getCor2()
    {
        return cor2;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    protected void paintComponent( Graphics g ) {
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, cor1, 0, h, cor2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
    
    public Gradiente()
    {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
