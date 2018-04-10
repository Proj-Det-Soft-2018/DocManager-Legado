package apresentacao;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import negocio.dominio.Interessado;
import negocio.fachada.FachadaArmazenamento;
import negocio.fachada.FachadaNegocio;
import negocio.servico.ValidationException;
import utils.widget.MaskedContactTextField;
import utils.widget.MaskedTextField;

/**
 * @author hugotho
 * 
 */
public class ControleDialogInteressado implements Initializable {
	
	private static final String MASK_CPF = "###.###.###-##";
	
	private FachadaArmazenamento fachada;
	private Interessado interessadoOriginal;
	MaskedTextField maskCpf;
	
	@FXML
	private VBox raiz;
	
	@FXML
	private Label lblAlerta;
	
	@FXML
	private Label lblTxtCpf;
	
	@FXML
	private TextField txtNome;
	
	@FXML
	private MaskedContactTextField txtContato;
	
	@FXML
	private Button btnCancelar;
	
	@FXML
	private Button btnSalvar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.fachada = FachadaNegocio.getInstance();
		this.interessadoOriginal = null;
		this.maskCpf = new MaskedTextField(MASK_CPF);
	}
	
	public void setCpfOnForm(String cpf) {
		this.maskCpf.setPlainText(cpf);
		this.lblTxtCpf.setText(this.maskCpf.getText());
	}
	
	public void populeForm(Interessado interessadoOriginal) {
		this.raiz.getChildren().remove(this.lblAlerta);
		this.interessadoOriginal = interessadoOriginal;
		
		this.maskCpf.setPlainText(interessadoOriginal.getCpf());
		this.lblTxtCpf.setText(this.maskCpf.getText());
		
		this.txtNome.setText(interessadoOriginal.getNome());
		this.txtContato.setContactPlainText(interessadoOriginal.getContato());
	}
	
	@FXML
	private void fecharJanela() {
		Stage janela = (Stage) this.raiz.getScene().getWindow();
		if (janela != null)
			janela.close();
	}

	@FXML
	private void salvar() {
		Interessado interessado = new Interessado();
		boolean failure = false;
		StringBuilder failureMsg = new StringBuilder();
		
		try {
			interessado.setCpf(this.maskCpf.plainTextProperty().getValue());
		} catch (ValidationException ve) {
			failure = true;
			failureMsg.append(ve.getMessage());
		}
		
		try {
			interessado.setNome(this.txtNome.getText());
		} catch (ValidationException ve) {
			failure = true;
			if (failureMsg.length() != 0) {
				failureMsg.append("\n\n");
			}
			failureMsg.append(ve.getMessage());
		}
		
		try {
			interessado.setContato(this.txtContato.plainTextProperty().getValue());
		} catch (ValidationException ve) {
			failure = true;
			if (failureMsg.length() != 0) {
				failureMsg.append("\n\n");
			}
			failureMsg.append(ve.getMessage());
		}
		
		if (failure) {
			failureMsg.append("\n\n");
			Alert alert = new Alert(AlertType.ERROR, failureMsg.toString());
			alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(
					node -> {
						((Label)node).setMinHeight(Region.USE_PREF_SIZE);
						((Label)node).setTextFill(Color.RED);
					});
			alert.setHeaderText(null);
			alert.setGraphic(null);
	        alert.initOwner(raiz.getScene().getWindow());

	        alert.showAndWait();
		}
		else {
			if(interessadoOriginal == null) {
				fachada.salvar(interessado);
			} else {
				interessado.setId(interessadoOriginal.getId());
				fachada.atualizar(interessado);
			}
			this.fecharJanela();
		}
	}
}