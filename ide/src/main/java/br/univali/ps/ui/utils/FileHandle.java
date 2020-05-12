package br.univali.ps.ui.utils;

import br.univali.ps.nucleo.ExcecaoAplicacao;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

public final class FileHandle
{
    private static final Logger LOGGER = Logger.getLogger(FileHandle.class.getName());
    private static final String charsetPadrao = "UTF-8";

    public static void save(String text, File file) throws Exception{
        save(text, file, charsetPadrao);
    }
    public static void save(String text, File file, String charset) throws Exception
    {
        if(file.getParentFile().exists())
        {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)))
            {
                writer.write(text);
                writer.flush();
                writer.close();
            }
            catch(IOException e) {
                if (e.getMessage().contains("Permission denied") || e.getMessage().contains("Acesso negado")) {
                    String pasta = file.getParentFile().getAbsolutePath();
                    throw new ExcecaoAplicacao("Você não possuí permissão de escrita para a pasta '" + pasta + "'", ExcecaoAplicacao.Tipo.ERRO_USUARIO);
                }
                else if (e.getMessage().contains("enough space on the disk") || e.getMessage().contains("insuficiente no disco")) {
                    String pasta = file.getName();
                    if(!pasta.contains("recuperavel"))
                    throw new ExcecaoAplicacao("Não há espaço no disco", ExcecaoAplicacao.Tipo.ERRO_USUARIO);
                }
                else {
                    throw e;
                }
            }
        }
    }

    public static String open(File file) throws Exception
    {
        if (file == null)
        {
            throw new ExcecaoAplicacao("Arquivo nulo não pode ser aberto.", ExcecaoAplicacao.Tipo.ERRO_USUARIO);
        }

        if (file.isDirectory())
        {
            throw new ExcecaoAplicacao("O arquivo solicitado é um diretório.", ExcecaoAplicacao.Tipo.ERRO_USUARIO);
        }

        if (!file.canRead())
        {
            throw new ExcecaoAplicacao("Acesso negado ao arquivo. Você não possuí permissão de leitura para esse arquivo", ExcecaoAplicacao.Tipo.ERRO_USUARIO);
        }

        return read(file);
    }

    private static String read(File file) throws Exception
    {        
        return read(new FileInputStream(file));
    }

    public static String read(InputStream inputStream) throws Exception
    {
        byte[] fileContent = IOUtils.toByteArray(inputStream);
        EncodingDetector detector = new EncodingDetector();
        
        String charset = charsetPadrao;
        charset = detector.detect(inputStream, fileContent);
        String fileText = new String(fileContent, charset);
        inputStream.close();
        return fileText;
    }

    public static String read(InputStream inputStream, String charset) throws Exception
    {
        StringBuilder reading = new StringBuilder();

        try (InputStreamReader isr = new InputStreamReader(inputStream, charset); BufferedReader reader = new BufferedReader(isr))
        {
            int read;
            char[] buffer = new char[4096];
            
            while ((read = reader.read(buffer, 0, buffer.length)) > 0)
            {
                reading.append(buffer, 0, read);
            }
        }
        catch (IOException ex)
        {
            throw new ExcecaoAplicacao(ex, ExcecaoAplicacao.Tipo.ERRO_USUARIO);
            
        }

        return reading.toString().replaceAll("\r\n", "\n");
    }
}
