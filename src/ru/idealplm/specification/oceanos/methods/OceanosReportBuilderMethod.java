package ru.idealplm.specification.oceanos.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.SpecificationSettings;
import ru.idealplm.utils.specification.methods.IReportBuilderMethod;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

public class OceanosReportBuilderMethod implements IReportBuilderMethod{

	private Specification specification = Specification.getInstance();
	private PDFBuilder pdfBuilder;
	
	public OceanosReportBuilderMethod(InputStream templateStream, InputStream configStream) {
		try {
			pdfBuilder = new PDFBuilder(templateStream, configStream);
		} catch (Exception e) {
			System.out.println("Unable to create PDFBuilder\n" + "templateStream==null?"+(templateStream==null)+"\nconfigStream==null?"+(configStream==null));
			e.printStackTrace();
		}
	}
	
	@Override
	public File buildReportFile() {
		System.out.println("...METHOD... ReportBuilderMethod");
		File reportFile = null;
		try {
			copy(OceanosReportBuilderMethod.class.getResourceAsStream("/icons/iconOceanos.jpg"), new File(specification.getXmlFile().getParentFile().getAbsolutePath()+"\\iconOceanos.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Specification.settings.setTemplateStream(OceanosReportBuilderMethod.class.getResourceAsStream("/pdf/OceanosSpecPDFTemplate.xsl"));
		//Specification.settings.setConfigStream(OceanosReportBuilderMethod.class.getResourceAsStream("/pdf/userconfig.xml"));
		
		pdfBuilder.passSourceFile(specification.getXmlFile(), this);
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		reportFile = pdfBuilder.getReport();
		return reportFile;
	}
	
	public static void copy(InputStream source, File dest) throws IOException {
        try {
            FileOutputStream os = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = source.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                os.close();
            }
        } catch (Exception ex){
        	ex.printStackTrace();
        } finally {
        	if(source!=null){
        		source.close();
        	}
        }
    }
	
	public void writeToConsole(Object what, String name){
		System.out.println("======= WRITING " + name + " ========");
		System.out.println("class="+what.getClass().getName());
		try{
			if(what.getClass().equals(File.class)){
				try (BufferedReader br = new BufferedReader(new FileReader((File)what))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	System.out.println(line);
				    }
				}
			} else if (what.getClass().equals(FileInputStream.class)){
				try (BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)what, "UTF-8"))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	System.out.println(line);
				    }
				}
			} else {
				System.out.println("=====not file nor stream");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
