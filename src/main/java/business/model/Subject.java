package business.model;

import java.util.ArrayList;
import java.util.List;

public enum Subject {
	
	NULL("- Inválido -"),
	GENERIC_1("Aposentadoria por Invalidez"),
	GENERIC_2("Avaliação para fins de pensão"),
	GENERIC_3("Remoção por motivo de saúde do servidor ou de pessoa de sua família e " + 
			"Movimentação do Prontuário de Saúde de Servidor Removido"),
	GENERIC_4("Horário especial para servidor com deficiência e para o servidor com familiar com " + 
			"deficiência"),
	GENERIC_5("Constatação de deficiência dos candidatos aprovados em concurso público em " + 
			"vaga de pessoa com deficiência"),
	GENERIC_6("Avaliação de sanidade mental do servidor para fins de Processo Administrativo " + 
			"Disciplinar"),
	GENERIC_7("Recomendação para tratamento de acidentados em serviço em instituição " + 
			"privada à conta de recursos públicos"),
	GENERIC_8("Readaptação funcional de servidor por redução de capacidade laboral"),
	GENERIC_9("Avaliação de servidor aposentado por invalidez para fins de reversão"),
	GENERIC_10("Avaliação de servidor aposentado para constatação de invalidez por doença " + 
			"especificada no §1o do art. 186, para fins de integralização de proventos"),
	GENERIC_11("Avaliação da capacidade laborativa de servidor em disponibilidade"),
	GENERIC_12("Exame para investidura em cargo público"),
	GENERIC_13("Pedido de reconsideração e recurso"),
	GENERIC_14("Avaliação para isenção de imposto de renda"),
	GENERIC_15("Avaliação de idade mental de dependente para concessão de auxílio pré-escolar"),
	GENERIC_16("Avaliação de servidor portador de deficiência para comprovação da necessidade " + 
			"de acompanhamento de viagem a serviço"),
	GENERIC_17("Avaliação da capacidade laborativa por recomendação superior"),
	GENERIC_18("Comunicação de doença de notificação compulsória");
	
	private String text;
	
	Subject(String text){
		this.text = text;
	}
	
	public String getText() {
		return text;
	}



	public static List<String> getAssuntos() {
		List<String> listaAssuntos = new ArrayList<>();
		for(Subject assunto : Subject.values()) {
			listaAssuntos.add(assunto.text);
		}
		listaAssuntos.remove(0);
		return listaAssuntos;
	}
	
	public static Subject getAssuntoPorId(int id){
		return Subject.values()[id];
	}
	

}
