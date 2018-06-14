package br.univali.ps.ui.abas;

import br.univali.ps.ui.paineis.PSPainelTabulado;
import br.univali.ps.ui.utils.IconFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class Aba extends JPanel
{
    private static List<Class<? extends Aba>> classesFilhas = new ArrayList<>();
    
    private CabecalhoAba cabecalho;
    private PSPainelTabulado painelTabulado;
    private List<AbaListener> listeners;
    private boolean removivel = false;
    

    public Aba() {
        if (!classesFilhas.contains(this.getClass()))
        {
            classesFilhas.add(this.getClass());
        }
        
        this.listeners = new ArrayList<>();
        this.cabecalho = criarCabecalhoPadrao("Sem título", IconFactory.createIcon(IconFactory.CAMINHO_ICONES_PEQUENOS, "unknown.png"), false);
                
        Date date = new Date();
        this.setName("aba"+date.getTime());
    }
    
    
    
    public boolean isSelected(){
        return painelTabulado.getAbaSelecionada() == this;
    }
    
    public void setPainelTabulado(PSPainelTabulado painelTabulado)
    {
        this.painelTabulado = painelTabulado;
    }

    
     public Aba(String titulo, Icon icone, boolean removivel)
     {
         this();
         this.cabecalho = criarCabecalhoPadrao(titulo, icone, removivel);
     }
    
//    public Aba(CabecalhoAba cabecalhoAba)
//    {
//        this();        
//        this.cabecalho = cabecalhoAba;
//    }
    
    public static List<Class<? extends Aba>> classesFilhas()
    {
        return new ArrayList<>(classesFilhas);
    }

    private CabecalhoAba criarCabecalhoPadrao(String titulo, Icon icone, boolean removivel)
    {
        CabecalhoAba cabecalhoPadrao = new CabecalhoAba(this);
        this.removivel = removivel;
        cabecalhoPadrao.setTitulo(titulo);
        cabecalhoPadrao.setIcone(icone);
        cabecalhoPadrao.setBotaoFecharVisivel(removivel);
        cabecalhoPadrao.configurarCores();
        
        return cabecalhoPadrao;
    }
    
//    public void adicionar(PainelTabulado painelTabulado)
//    {
////        this.painelTabulado = painelTabulado;
////        this.painelTabulado.add(this);
////        this.painelTabulado.setTabComponentAt(painelTabulado.indexOfComponent(this), cabecalho);
////        this.painelTabulado.setSelectedComponent(this);
//    }

    public CabecalhoAba getCabecalho()
    {
        return cabecalho;
    }

    protected void setCabecalho(CabecalhoAba cabecalho)
    {
        this.cabecalho = cabecalho;
    }
    
    public PSPainelTabulado getPainelTabulado()
    {
        return painelTabulado;
    }
    
    public void setRemovivel(boolean removivel)
    {
        cabecalho.setBotaoFecharVisivel(removivel);
    }
    public boolean isRemovivel()
    {
        return removivel;
    }

    public boolean fechar()
    {        
        boolean podeFechar = true;

        for (AbaListener listener : listeners)
        {
            if (!listener.fechandoAba(this))
            {
                podeFechar = false;
            }
        }

        if (podeFechar)
        {
            if (painelTabulado != null)
            {
                painelTabulado.remove(this);
                SwingUtilities.invokeLater(() -> {
                    painelTabulado.revalidate();
                    painelTabulado.repaint();
                    painelTabulado = null;
                });               
            }
        }        

        return podeFechar;
    }

    public void selecionar()
    {
        if (painelTabulado != null)
        {
            if(painelTabulado.contemAba(this)){
                painelTabulado.mudarParaAba(this);
            }
        }
    }

    public void adicionarAbaListener(AbaListener listener)
    {
        if (!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }

    public void removerAbaListener(AbaListener listener)
    {
        listeners.remove(listener);
    }
}
