package negocio.dominio;

import java.util.ArrayList;
import java.util.List;

public enum Orgao {
	
	NULL("- Inválido -"),
	UFRN("UNIVERSIDADE FED. DO RIO GRANDE DO NORTE"),
	DPF("DEPARTAMENTO DE POLICIA FEDERAL"),
	MTE("MINISTERIO DO TRABALHO E EMPREGO"),
	DPRF("DEPTO. DE POLICIA RODOVIARIA FEDERAL"),
	FUNAI("FUNDACAO NACIONAL DO INDIO"),
	MAPA("MINIST.DA AGRICULTURA,PECUARIA E ABAST."),
	MF("MINISTERIO DA FAZENDA"),
	MJ("MINISTERIO DA FAZENDA"),
	MPOG("MINISTERIO DO PLANEJ. DESENV. E GESTAO"),
	IPHAN("INSTITUTO DO PATR.HIST.E ART. NACIONAL"),
	UFERSA("UNIVERSIDADE FED. RURAL DO SEMI-ARIDO"),
	FUNASA("FUNDACAO NACIONAL DE SAUDE"),
	DNPM("DEPARTAMENTO NAC. DE PRODUCAO MINERAL"),
	ANVISA("AGENCIA NACIONAL DE VIGILANCIA SANITARIA"),
	DPU("DEFENSORIA PUBLICA DA UNIAO"),
	DNIT("DEPARTAMENTO NAC.DE INFRAEST. DE TRANSP."),
	AGU("ADVOCACIA-GERAL DA UNIAO"),
	MCTI("MINIST.DA CIENCIA, TECNOLOGIA E INOVACAO"),
	IBAMA("INST. BR. MEIO AMB. REC. NAT. RENOVAVEIS"),
	INCRA("INSTITUTO NAC. DE COLONIZ E REF AGRARIA"),
	DNOCS("DEPTO. NACIONAL DE OBRAS CONTRA AS SECAS"),
	ICMBIO("INSTITUTO CHICO MENDES CONSERV.BIODIVER."),
	IBGE("FUND. INST. BRASIL. GEOG. E ESTATISTICA"),
	CGU("MINISTERIO DA TRANSP. FISCAL. E CGU");
	
	private String nomeExt;
	
	Orgao(String nome) {
		this.nomeExt = nome;
	}	
	
	public static List<String> getOrgaos() {
		List<String> listaOrgaos = new ArrayList<String>();
		for(Orgao orgao : Orgao.values()) {
			listaOrgaos.add(orgao.name() + " - " + orgao.nomeExt);
		}
		listaOrgaos.remove(0);
		return listaOrgaos;
	}
	
	public static Orgao getOrgaoPorId(int id) throws ValidationException{
		if(id == 0) {
			throw new ValidationException("Você não selecionou o Orgão!", "Orgao", "O campo Orgão é obrigatório.");
		}
		else {
			return Orgao.values()[id];
		}
	}
	
}
