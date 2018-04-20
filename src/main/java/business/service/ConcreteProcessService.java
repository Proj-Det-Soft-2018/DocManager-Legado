/**
 * 
 */
package business.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import business.exception.ValidationException;
import business.model.Process;
import business.model.Situation;
import persistence.DaoFactory;
import persistence.ProcessoDao;
import persistence.exception.DatabaseException;

/**
 * @author clah
 *@since 24/03/2018
 */
public class ConcreteProcessService extends Observable implements ProcessService {

	private static final URL FO_TEMPLATE_PATH = ConcreteProcessService.class.getResource("/fo_templates/xml2fo.xsl");
	private static final Logger LOGGER = Logger.getLogger(ConcreteProcessService.class);

	private ProcessoDao processoDao;

	// Usuário para autorização -- Apache Shiro
	private Subject currentUser;

	// Subsídios para geração de PDF -- Apache Xalan/FOP
	private Transformer xmlToFoTransformer;
	private FopFactory fopFactory;
	private FOUserAgent foUserAgent;

	// Singleton
	private static final ConcreteProcessService instance = new ConcreteProcessService();

	private ConcreteProcessService() {
		processoDao = DaoFactory.getProcessDao();

		// Inicilização do Apache Shiro -- utiliza o resources/shiro.ini
		IniRealm iniRealm = new IniRealm("classpath:shiro.ini");
		SecurityManager secutiryManager = new DefaultSecurityManager(iniRealm);
		SecurityUtils.setSecurityManager(secutiryManager);
		currentUser = SecurityUtils.getSubject();

		xmlToFoTransformer = generateTransformer();
		fopFactory = generateFopFactory();
		foUserAgent = generateFOUserAgent();
	}

	public static ConcreteProcessService getInstance() {
		return instance;
	}


	@Override
	public void save(Process process) throws ValidationException, DatabaseException {
		//Antes de salvar verificar os campos que nao podem ser nulos
		this.validarNumeroDuplicado(process.getNumero());

		processoDao.salvar(process);
		this.notifyObservers();
	}

	@Override
	public void update(Process process) throws DatabaseException {
		processoDao.atualizar(process);
		this.notifyObservers();			
	}

	@Override
	public void delete(Process process, String admUser, String password) throws DatabaseException {

		if (!this.currentUser.isAuthenticated()) {
			UsernamePasswordToken token = new UsernamePasswordToken(admUser, password);
			token.setRememberMe(true);

			currentUser.login(token); // Joga uma AuthenticationException
		}

		if (currentUser.hasRole("admin")) {
			processoDao.deletar(process);
			this.notifyObservers();
		}

		currentUser.logout();
	}

	public List<Process> getList() throws ValidationException, DatabaseException{
		return processoDao.pegarTodos();
	}

	public List<Process> search(String number, String name, String cpf, int situation, int organization, int subject) throws ValidationException, DatabaseException {

		boolean invalidNumber = (number == null || number.isEmpty());
		boolean invalidName = (name == null || name.isEmpty());
		boolean invalidCpf = (cpf == null || cpf.isEmpty());
		boolean invalidSituation = (situation == 0);
		boolean invalidOrganization = (organization == 0);
		boolean invalidSubject = (subject == 0);

		if(invalidNumber && invalidName && invalidCpf && invalidSituation && invalidOrganization && invalidSubject) {
			throw new ValidationException("Não foram inseridos valores para busca!");
		}
		return processoDao.buscaComposta(number, name, cpf, organization, subject, situation);
	}

	public byte[] getPdf(Process process) {
		
		String xml = process.toXml();
		String fo = xml2FoTransform(xml);
		
		return fo2PdfTransform(fo);
	}

	/**
	 *  Método procura no banco se tem outro processo com o mesmo número. Se tem, o registro deve
	 *  estar com a situação definida como concluída. Caso contrário, pede confirmação do 
	 *  usuário para modificar situacao do registro antigo como concluido.
	 *  
	 * @param numero Numero do processo que está sendo inserido.
	 * @throws ValidationException 
	 * @throws DatabaseException 
	 */
	private void validarNumeroDuplicado(String numero) throws ValidationException, DatabaseException {
		List<Process> duplicados = processoDao.buscarPorNumero(numero);
		if(duplicados != null && !duplicados.isEmpty()) {
			//verifica se a situacao dos processos encontrados estao como concluido
			for (Process processo : duplicados) {
				if(!(processo.getSituacao().ordinal()==Situation.CONCLUIDO.ordinal()) ) {
					//TODO tratar e criar Exception
					throw new ValidationException("Existe outro processo cadastrado com situação não concluída");
				}				
			}			
		}		
	}

	private String xml2FoTransform(String xml) {

		String fo = null;

		if (xml != null) {
			StringReader sr = new StringReader(xml);
			StringWriter sw = new StringWriter();

			try {
				// Faz a conversão de XML para XSL:FO
				if (this.xmlToFoTransformer != null) {

					StreamSource xmlSource = new StreamSource(sr); 
					StreamResult foResult = new StreamResult(sw);
					this.xmlToFoTransformer.transform(xmlSource, foResult);
					// Pega a string gerada
					fo = sw.toString();
				}
			} catch (TransformerException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {

				// Fecha o reader e o writer
				try {
					sr.close();
					sw.close();
				} catch (IOException e) {
					// Não conseguiu fechar o writer
					LOGGER.fatal(e.getMessage(), e);
				}	
			}
		}

		return fo;
	}

	private byte[] fo2PdfTransform(String fo) {

		byte[] pdfData;

		try (
				StringReader sourceReader = new StringReader(fo);
				ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
			) {
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, this.foUserAgent, resultStream);

			// Configura um transformador utilizando as configurações padrão
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();

			StreamSource src = new StreamSource(sourceReader);
			// O resultado é processaso pelo FOP para geração do PDF
			SAXResult res = new SAXResult(fop.getDefaultHandler());

			// Executa a transformação
			transformer.transform(src, res);

			InputStream auxStream = new ByteArrayInputStream(resultStream.toByteArray());

			pdfData = IOUtils.toByteArray(auxStream);

		} catch (FOPException | TransformerException | IOException e) {
			LOGGER.error(e.getMessage(), e);
			pdfData = new byte[0];	
		}


		return pdfData;
	}

	private Transformer generateTransformer() {

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document xslDoc = db.parse(FO_TEMPLATE_PATH.openStream());
			DOMSource xslSource = new DOMSource(xslDoc);

			return tf.newTransformer(xslSource);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	private FopFactory generateFopFactory() {

		FopFactory newFopFactory = null;

		String path = FO_TEMPLATE_PATH.getPath();
		String filePath = "file://" + path.substring(0, path.lastIndexOf("/fo_templates/xml2fo.xsl"));

		File config = new File("src/main/resources/fop.xconf");
		
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			// Lê o arquivo de configuração do FOP e seta o campo <base> para a pasta "resources"
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document fopConfDoc = db.parse(config);
			Element element = (Element) fopConfDoc.getElementsByTagName("base").item(0);
			element.setTextContent(filePath);

			// Transforma o w3c.Document em InputStream
			DOMSource xmlSource = new DOMSource(fopConfDoc);
			StreamResult outputTarget = new StreamResult(outputStream);
			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

			// Gera a fábrica com o arquivo de configuração
			newFopFactory = FopFactory.newInstance(new URI(filePath), inputStream);
			inputStream.close();
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

		}

		return newFopFactory;
	}

	private FOUserAgent generateFOUserAgent() {
		FOUserAgent newFOUserAgent = null;

		if (this.fopFactory != null) {
			newFOUserAgent = fopFactory.newFOUserAgent();
			// Configurações do FOUserAgente -- basicamente seta as propriedades do PDF
			newFOUserAgent.setTitle("Certidão");
			newFOUserAgent.setAuthor("Subsistema Integrado de Atenção à Saúde do Servidor - SIASS");
			newFOUserAgent.setSubject("Situação de Processo");
			newFOUserAgent.setCreator("DocManager");
		}
		return newFOUserAgent;
	}
}