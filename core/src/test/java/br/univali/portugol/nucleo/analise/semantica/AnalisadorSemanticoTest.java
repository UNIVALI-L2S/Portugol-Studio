package br.univali.portugol.nucleo.analise.semantica;

import br.univali.portugol.nucleo.ErroCompilacao;
import br.univali.portugol.nucleo.Portugol;
import br.univali.portugol.nucleo.programa.Programa;
import br.univali.portugol.nucleo.analise.ResultadoAnalise;
import br.univali.portugol.nucleo.analise.semantica.erros.ErroSimboloNaoDeclarado;
import br.univali.portugol.nucleo.analise.semantica.erros.ErroSimboloNaoInicializado;
import br.univali.portugol.nucleo.analise.semantica.erros.ErroTiposIncompativeis;
import br.univali.portugol.nucleo.asa.TipoDado;
import static org.junit.Assert.*;
import org.junit.Test;

public final class AnalisadorSemanticoTest
{
	
	@Test (expected = ErroCompilacao.class)
    public void testParaMultideclaradoForaDeEscopo() throws ErroCompilacao{
        Portugol.compilarParaAnalise(
        		  "programa"
				 +"{"
				 +"	funcao inicio()"
				 +"	{"
				 +"		para(inteiro i = 0, j = 0, k = 5,l; i < 5; i++){"
				 +"			l = 1"
				 +"			escreva(i + j + k + l)"
				 +"		}"
				 +"		escreva(j)"
				 +"	}"
				 +"}"
        );
    }
	
	@Test (expected = ErroCompilacao.class)
    public void testParaMultideclaradoLoopVariaveis() throws ErroCompilacao{
        Portugol.compilarParaAnalise(
        		  "programa"
				 +"{"
				 +"	funcao inicio()"
				 +"	{"
				 +"		para(inteiro i = k, j = i, k = j; i < 5; i++){"
				 +"			escreva(i + \" \" + j + \" \" + k + \"\n\")"
				 +"		}"
				 +"	}"
				 +"}"
        );
    }
	
	@Test
    public void testParaMultideclaradoFuncional(){
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
            		  "programa"
    				 +"{"
    				 +"	funcao inicio()"
    				 +"	{"
    				 +"		para(inteiro i = 0, j = 0, k = 5,l; i < 5; i++){"
    				 +"			para(j = 1; j < 5; i++){"
    				 +"				j = 5"
    				 +"			}"
    				 +"			para(k = 5; k < 5; k++){"
    				 +"				"
    				 +"			}"
    				 +"			para(l = 0; l < 5; l++){"
    				 +"				"
    				 +"			}"
    				 +"			escreva(\"i=\" + i + \";j=\" + j + \";k=\" + k + \";l=\" + l + \"\n\")"
    				 +"		}"
    				 +"		para(inteiro i = 2, j = i, k = j; i < 5; i++){"
    				 +"			escreva(i + \" \" + j + \" \" + k + \"\n\")"
    				 +"		}"
    				 +"	}"
    				 +"}"
            );
            
            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
	
    @Test (expected = ErroCompilacao.class)
    public void testFuncaoLeiaComFuncao() throws ErroCompilacao {
        
        Portugol.compilarParaAnalise(
            "    programa {             " +
            "	funcao inicio() {       " +
            "		leia(teste())   " +
            "	}                       " +
            "                           " +
            "	funcao teste(){}        " +
            "}"
        );
    }

    @Test (expected = ErroCompilacao.class)
    public void testFuncaoLeiaComConstanteDeBiblioteca() throws ErroCompilacao {
        
        Portugol.compilarParaAnalise(
                "programa                       "
                + "{ inclua biblioteca Graficos "
                + " funcao inicio(){            "
                + "   leia(Graficos.COR_AMARELO)"
                + " }                           "
                + "}                            "
        );
    }
    
    @Test (expected = ErroCompilacao.class)
    public void testFuncaoLeiaComVariavelConstante() throws ErroCompilacao {
        
        Portugol.compilarParaAnalise(
                "programa                       "
                + "{                            "
                + " funcao inicio(){            "
                + "   const inteiro x = 1       "
                + "   leia(x)                   "
                + " }                           "
                + "}                            "
        );
    }

    @Test (expected = ErroCompilacao.class)
    public void testFuncaoLeiaComConstante() throws ErroCompilacao {
        
        Portugol.compilarParaAnalise(
                "programa                       "
                + "{                            "
                + " funcao inicio(){            "
                + "   leia(10)                   "
                + " }                           "
                + "}                            "
        );
    }
    
    @Test (expected = ErroCompilacao.class)
    public void testFuncaoLeiaSemParametros() throws ErroCompilacao {
        // a função leia não pode ser usada sem parametros
        Portugol.compilarParaAnalise(
                "programa                       "
                + "{                            "
                + " funcao inicio(){            "
                + "   leia()                   "
                + " }                           "
                + "}                            "
        );
    }
    
    @Test
    public void testFuncaoLeiaComMatriz() {
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
                    "programa                       "
                    + "{                            "
                    + " funcao inicio(){            "
                    + "   inteiro a[3][2]                 "
                    + "   leia(a[0][0])                   "
                    + " }                           "
                    + "}                            "
            );
            
            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testFuncaoLeiaComVetor() {
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
                    "programa                       "
                    + "{                            "
                    + " funcao inicio(){            "
                    + "   inteiro a[3]                 "
                    + "   leia(a[0])                   "
                    + " }                           "
                    + "}                            "
            );
            
            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testFuncaoLeiaComVariavel() {
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
                    "programa                       "
                    + "{                            "
                    + " funcao inicio(){            "
                    + "   inteiro a                 "
                    + "   leia(a)                   "
                    + " }                           "
                    + "}                            "
            );
            
            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testFuncaoLeiaComVariasVariaveis() {
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
                    "programa                       "
                    + "{                            "
                    + " funcao inicio(){            "
                    + "   inteiro a,b,c                 "
                    + "   leia(a,b,c)                   "
                    + " }                           "
                    + "}                            "
            );

            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    
     @Test
    public void testReferenciaMatriz()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa                       "
                    + "{                            "
                    + " funcao inicio(){            "
                    + "   inteiro a[2][2]           "
                    + "   real x = 1.0              "
                    + "   escreva(a[x][0])          "
                    + " }                           "
                    + "}                            "
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            //assertEquals("Tipos incompatíveis! O índice do vetor deve ser uma expressão do tipo \"" + TipoDado.INTEIRO + "\" mas foi passada uma expressão do tipo \"" + TipoDado.CADEIA + "\".", resultado.getErros().get(0).getMensagem());
        }
    }
    
    @Test
    public void testReferenciaVetor()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa"
                    + "{"
                    + " funcao inicio(){"
                    + "   inteiro a[] = {1, 2, 3}"
                    + "   escreva(a[\"1\"])       "
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! O índice do vetor deve ser uma expressão do tipo \"" + TipoDado.INTEIRO + "\" mas foi passada uma expressão do tipo \"" + TipoDado.CADEIA + "\".", resultado.getErros().get(0).getMensagem());
        }
    }

    @Test
    public void testMenosUnario()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa"
                    + "{"
                    + " funcao inicio(){"
                    + "   cadeia a = \"teste\""
                    + "   escreva(-a)        "
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! A operação \"menos unário\" espera uma expressão do tipo \"" + TipoDado.INTEIRO + "\" ou \"" + TipoDado.REAL + "\" mas foi passada uma expressão do tipo \"" + TipoDado.CADEIA + "\".", resultado.getErros().get(0).getMensagem());
        }
    }

    @Test
    public void testNoNao()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa"
                    + "{"
                    + " funcao inicio(){"
                    + "   cadeia a = \"teste\""
                    + "   se (nao a){"
                    + "     escreva(\"nao a\")"
                    + "   }        "
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! A operação de negação espera uma expressão do tipo \"" + TipoDado.LOGICO + "\" mas foi passada uma expressão do tipo \"" + TipoDado.CADEIA + "\".", resultado.getErros().get(0).getMensagem());
        }
    }

    @Test
    public void testVariavelNaoInicializada()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa"
                    + "{"
                    + " funcao inicio(){"
                    + "   inteiro a, b"
                    + "   escreva(a)"
                    + "   b = a + 2"
                    + "   a = a + b"
                    + "   escreva(b)"
                    + "   a = 1"
                    + "   escreva(a)"
                    + " }"
                    + "}"
            );

            fail("Era esperado tres erros semanticos");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado tres erros semanticos", 3, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroSimboloNaoInicializado.class.getName(), ErroSimboloNaoInicializado.class, resultado.getErros().get(0).getClass());
        }
    }

    @Test
    public void testTipoIncompativelExpressaoSOMAInteiroLogico()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  inteiro a = 1"
                    + "  logico b = verdadeiro"
                    + "  b = a + b"
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
        }
    }

    @Test
    public void testVariavelNaoDeclaradaEmExpressaoSoma()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  inteiro a = 0, b = 1"
                    + "  a = a + c"
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Era esperado uma instancia de " + ErroSimboloNaoDeclarado.class.getName(), ErroSimboloNaoDeclarado.class, resultado.getErros().get(0).getClass());
        }
    }

    
    @Test
    public void testEscolhaCodigoCorreto()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  inteiro a = 1"
                    + "  escolha (a) {"
                    + "     caso 2:"
                    + "         pare"
                    + "     caso 1:"
                    + "         pare"
                    + "     caso contrario:"
                    + "         pare"
                    + "  }"
                    + " }"
                    + "}"
            );
        }
        catch (ErroCompilacao erroCompilacao)
        {
            assertEquals("Nao era esperado um erro semantico", 0, erroCompilacao.getResultadoAnalise().getErros().size());
        }
    }

    @Test
    public void testAmbasExpressoesEscolhaDiferentesInteiroCaracter()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  logico a = verdadeiro"
                    + "  escolha (a) {"
                    + "     caso 2.0:"
                    + "         pare"
                    + "     caso 'a':"
                    + "         pare"
                    + "  }"
                    + " }"
                    + "}"
            );

            fail("Eram esperados dois erros semanticos");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperados dois erros semanticos", 2, resultado.getErros().size());
            assertEquals("Tipos incompativeis na expressao do caso " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! O comando \"escolha\" espera uma expressão do tipo \"" + TipoDado.INTEIRO + "\" ou \"" + TipoDado.CARACTER + "\" mas foi passada uma expressão do tipo \"" + TipoDado.LOGICO + "\".", resultado.getErros().get(0).getMensagem());
            assertEquals("Tipos incompatíveis! A expressão esperada para esse caso deveria ser do tipo \"" + TipoDado.INTEIRO + "\" ou \"" + TipoDado.CARACTER + "\" mas foi passada uma expressão do tipo \"" + TipoDado.REAL + "\".", resultado.getErros().get(1).getMensagem());
        }
    }

    @Test
    public void testExpressaoPrimeiroCasoIgualExpressaoEscolha()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  caracter a = 'a'"
                    + "  escolha (a) {"
                    + "     caso 1:"
                    + "         pare"
                    + "     caso 'a':"
                    + "         pare"
                    + "  }"
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Tipos incompativeis na expressao do caso " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! A expressão esperada para esse caso deveria ser do tipo \"" + TipoDado.CARACTER + "\" mas foi passada uma expressão do tipo \"" + TipoDado.INTEIRO + "\".", resultado.getErros().get(0).getMensagem());
        }
    }

    @Test
    public void testExpressaoEscolhaInteiroOuCaracter()
    {
        try
        {
            Portugol.compilarParaAnalise(
                    "programa {"
                    + " funcao inicio() {"
                    + "  cadeia a = \"a\""
                    + "  escolha (a) {"
                    + "     caso 1:"
                    + "         pare"
                    + "     caso 'a':"
                    + "         pare"
                    + "  }"
                    + " }"
                    + "}"
            );

            fail("Era esperado um erro semantico");
        }
        catch (ErroCompilacao erroCompilacao)
        {
            ResultadoAnalise resultado = erroCompilacao.getResultadoAnalise();

            assertEquals("Era esperado um erro semantico", 1, resultado.getErros().size());
            assertEquals("Tipos incompativeis na expressao do escolha " + ErroTiposIncompativeis.class.getName(), ErroTiposIncompativeis.class, resultado.getErros().get(0).getClass());
            assertEquals("Tipos incompatíveis! O comando \"escolha\" espera uma expressão do tipo \"" + TipoDado.INTEIRO + "\" ou \"" + TipoDado.CARACTER + "\" mas foi passada uma expressão do tipo \"" + TipoDado.CADEIA + "\".", resultado.getErros().get(0).getMensagem());
        }
    }
    
    @Test
    public void testTiposDiferentesEscolhaAninhado()
    {
        try
        {
            Programa programa = Portugol.compilarParaAnalise(
                    "programa" +
                    "{" +
                    "   funcao inicio()" +
                    "   {" +
                    "       inteiro a =1" +
                    "       caracter b = 'a'" +
                    "" +
                    "       escolha(a)" +
                    "       {" +
                    "           caso 1:" +
                    "" +
                    "               escolha(b)" +
                    "               {" +
                    "                   caso 'a':" +
                    "                       escreva(\"\\nletra a\")" +
                    "                   pare" +
                    "                   caso 'b':" +
                    "                       escreva(\"\\nletra B\")" +
                    "                   pare" +
                    "               }" +
                    "               escreva(\"\1\")" +
                    "           pare" +
                    "" +
                    "           caso 2:" +
                    "" +
                    "               escolha(b)" +
                    "               {" +
                    "                   caso 'a':" +
                    "" +
                    "                       escolha (a)" +
                    "                       {" +
                    "                           caso 1:" +
                    "                               escreva(\"teste\")" +
                    "                           pare							" +
                    "                       }" +
                    "                   pare" +
                    "" +
                    "                   caso 'b':" +
                    "                       escreva(\"Erro\")" +
                    "                   pare" +
                    "               }" +
                    "" +
                    "               escreva(\"\2\")" +
                    "           pare	" +
                    "       }		" +
                    "   }" +
                    "}"
            );

            ResultadoAnalise resultado = programa.getResultadoAnalise();
            
            assertTrue("O programa deveria ter compilado sem erros e avisos", !resultado.contemAvisos() && !resultado.contemErros());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
}
