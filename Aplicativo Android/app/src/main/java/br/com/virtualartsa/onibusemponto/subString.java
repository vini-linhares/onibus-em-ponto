package br.com.virtualartsa.onibusemponto;

import java.text.Normalizer;

/**
 * Created by Schiavetto on 06/09/2017.
 */

public class subString {

    public static String trocarString(String texto){
        texto = texto.toUpperCase();
        texto = texto.replace("VL.", "Vila");
        texto = texto.replace("JD.", "Jardim");
        texto = texto.replace("TERM.", "Terminal");
        texto = texto.replace("PRINC.", "Princesa");
        texto = texto.replace("PRINC.", "Princesa");
        texto = texto.replace("PQ.", "Parque");
        texto = texto.replace("CONJ.", "Conjunto");
        texto = texto.replace("HAB.", "Habitacional");
        texto = texto.replace("PÇA.", "Praça");
        texto = texto.replace("AV.", "Avenida");
        texto = texto.replace("PTE.", "Ponte");
        texto = texto.replace("DR.", "Doutor");
        texto = texto.replace("BR.", "Barão");
        texto = texto.replace("HOSP.", "Hospital");
        texto = texto.replace("SHOP.", "Shopping");
        texto = texto.replace("CAP.", "Capitão");
        texto = texto.replace("PROF.", "Professor");
        texto = texto.replace("AL.", "Alameda");
        texto = texto.replace("CHÁC.", "Chácara");
        texto = texto.replace("PAS.", "Passagem");
        texto = texto.replace("SUB.", "Subterrânea");
        texto = texto.replace("SEN.", "Senador");
        texto = texto.replace("VIAD.", "Viaduto");
        texto = texto.replace("STO.", "Santo");
        texto = texto.replace("STA.", "Santa");
        texto = texto.replace("NSA.", "Nossa");
        texto = texto.replace("PE.", "Padre");
        texto = texto.replace("LGO.", "Largo");
        texto = texto.replace("BRIG.", "Brigadeiro");
        texto = texto.replace("DQ.", "Duque");
        texto = texto.replace("ROD.", "Rodovia");
        texto = texto.replace("S/N.", "Sem nome");
        texto = texto.replace("CEL.", "Coronel");
        texto = texto.replace("CONS.", "Conselheiro");
        texto = texto.replace("GEN.", "General");
        texto = texto.replace("CB.", "Cabo");
        texto = texto.replace("TTE.", "Tenente");
        texto = texto.replace("MAL.", "Marechal");
        texto = texto.replace("ALM.", "Almirante");
        texto = texto.replace("SD.", "Soldado");
        texto = texto.replace("SG.", "Sargento");
        texto = texto.replace("ESTR.", "Estrada");
        texto = texto.replace("REG.", "Regente");
        texto = texto.replace("BRIG.", "Brigadeiro");
        texto = texto.replace("DA.", "Dona");
        texto = texto.replace("ARQ.", "Arquiteto");
        texto = texto.replace("D.", "Dom");
        texto = texto.replace("R.", "Rua");
        texto = texto.replace("S.", "São");
        return texto;
    }

    public static String trocarAbrev(String texto){
        texto = texto.replace("VL.", "Vila");
        texto = texto.replace("JD.", "Jardim");
        texto = texto.replace("TERM.", "Terminal");
        texto = texto.replace("PRINC.", "Princesa");
        texto = texto.replace("PQ.", "Parque");
        texto = texto.replace("D.", "Dom");
        texto = texto.replace("CONJ.", "Conjunto");
        texto = texto.replace("HAB.", "Habitacional");
        texto = texto.replace("PÇA.", "Praça");
        texto = texto.replace("R.", "Rua");
        texto = texto.replace("AV.", "Avenida");
        texto = texto.replace("PTE.", "Ponte");
        texto = texto.replace("DR.", "Doutor");
        texto = texto.replace("BR.", "Barão");
        texto = texto.replace("HOSP.", "Hospital");
        texto = texto.replace("SHOP.", "Shopping");
        return texto;
    }




    public static String destrocaAbrev(String texto){
        texto = texto.toLowerCase();
        texto = texto.replace("vila", "VL.");
        texto = texto.replace("jardim", "JD.");
        texto = texto.replace("terminal", "TERM.");
        texto = texto.replace("princesa", "PRINC.");
        texto = texto.replace("parque", "PQ.");
        texto = texto.replace("conjunto", "CONJ.");
        texto = texto.replace("habitacional", "HAB.");
        texto = texto.replace("praça", "PÇA.");
        texto = texto.replace("avenida", "AV.");
        texto = texto.replace("ponte", "PTE.");
        texto = texto.replace("doutor", "DR.");
        texto = texto.replace("barão", "BR.");
        texto = texto.replace("hospital", "HOSP.");
        texto = texto.replace("shopping", "SHOP.");
        texto = texto.replace("capitão", "CAP.");
        texto = texto.replace("professor", "PROF.");
        texto = texto.replace("alameda", "AL.");
        texto = texto.replace("chácara", "CHÁC.");
        texto = texto.replace("passagem", "PAS.");
        texto = texto.replace("subterrânea", "SUB.");
        texto = texto.replace("senador", "SEN.");
        texto = texto.replace("viaduto", "VIAD.");
        texto = texto.replace("santo", "STO.");
        texto = texto.replace("santa", "STA.");
        texto = texto.replace("nossa", "NSA.");
        texto = texto.replace("padre", "PE.");
        texto = texto.replace("largo", "LGO.");
        texto = texto.replace("brigadeiro", "BRIG.");
        texto = texto.replace("duque", "DQ.");
        texto = texto.replace("rodovia", "ROD.");
        texto = texto.replace("sem nome", "S/N.");
        texto = texto.replace("coronel", "CEL.");
        texto = texto.replace("conselheiro", "CONS.");
        texto = texto.replace("general", "GEN.");
        texto = texto.replace("cabo", "CB.");
        texto = texto.replace("tenente", "TTE.");
        texto = texto.replace("marechal", "MAL.");
        texto = texto.replace("almirante", "ALM.");
        texto = texto.replace("soldado", "SD.");
        texto = texto.replace("sargento", "SG.");
        texto = texto.replace("estrada", "ESTR.");
        texto = texto.replace("regente", "REG.");
        texto = texto.replace("brigadeiro", "BRIG.");
        texto = texto.replace("dona", "DA.");
        texto = texto.replace("arquiteto", "ARQ.");
        texto = texto.replace("dom", "D.");
        texto = texto.replace("rua", "R.");
        texto = texto.replace("são", "S.");
        return texto;
    }

    public static String trocarString2(String texto){
        texto = texto.replace(" II", "segundo");
        texto = texto.replace(" e ", "");
        texto = texto.replace(" ", "");
        texto = texto.replace("-", "");
        texto = texto.replace("/", "");
        return texto;
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public static String formatarString(String texto){

        texto = trocarString2(texto);
        texto = trocarString(texto);
        texto = texto.toLowerCase();
        texto = removerAcentos(texto);

        return texto;
    }

}
