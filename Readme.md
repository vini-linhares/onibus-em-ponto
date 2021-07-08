# Ônibus em ponto
Ônibus em Ponto foi um dos projetos vencedores do Vaitec, em parceria com a prefeitura de São Paulo, organizado pela [AdeSampa](http://adesampa.com.br/) (Agência São Paulo de desenvolvimento). O objetivo era a criação de tecnologias para melhorar o bem estar e empreendedorismo na cidade.

O projeto teve a temática de mobilidade urbana, e foi voltado para facilitar o acesso de pessoas com deficiência visual e baixa visão no transporte público. Tinha duas frentes:

### API
Criação de uma api de transporte público urbano, complementar à api Olho Vivo da SPTrans, fornecendo dados que na época faltavam na api Olho Vivo. Como, por exemplo: Ponto mais próximo de localização; Itinerário de ônibus; e cálculo de distância entre ônibus e parada pretendida.
A linguagem usada foi PHP com banco de dados MySQL, e a estratégia utilizada foi um cruzamento entre a base de dados públicos do sistema de transporte fornecida pela prefeitura de São Paulo, api Olho Vivo, api Google Maps, e site da SPTrans.

### Aplicativos
Criação de aplicativos nativos Android (Java) e iOS (swift) que facilitava pessoas com deficiência visual, baixa visão e idosos utilizarem o transporte público na cidade de São Paulo.
Para o desenvolvimento dos aplicativos foram utilizados recursos como geolocalização, sintaxe de voz e reconhecimento de voz. Os aplicativos utilizaram também as funções da api do projeto.

A api e o código dos aplicativos foram disponibilizados publicamente para fomentar o desenvolvimento de tecnologias semelhantes na cidade. O projeto foi descontinuado porque entendemos que seu papel foi cumprido, visto que outros aplicativos de transporte público e a API olho vivo da SPTrans implementaram funções que na época considerávamos deficientes.
