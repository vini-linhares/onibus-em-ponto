import UIKit
import WebKit
import AVFoundation
import Speech       //Ouvir

class TermosViewController: UIViewController, WKNavigationDelegate, AVAudioPlayerDelegate {
    
    
    var ler = "Bem-vindo aos Termos de Uso e à Política de Privacidade do Ônibus em Ponto. \n O simples acesso ao nosso aplicativo gera a presunção de que você leu a versão mais recente e atualizada dos Termos de Uso e da nossa Política de Privacidade. Além disso, nós consideraremos que você concordou com todas as condições aqui indicadas. Por essa razão, recomendamos que você leia com atenção este documento ANTES de começar a usar qualquer recurso do nosso aplicativo. \n Se você por acaso não concordar com alguma condição prevista nos Termos de Uso ou na Política de Privacidade, sugerimos que não utilize os nossos serviços. Mas, claro, se simplesmente tiver alguma dúvida sobre as regras de uso, fique à vontade para nos consultar. Teremos prazer em lhe responder! \n Gratuidade \n Nosso aplicativo é inteiramente gratuito, não sendo necessária a realização de qualquer pagamento por parte dos nossos usuários. \n Uso dos nossos aplicativos \n Quando você utiliza este aplicativo, podem ser coletados dados, para fins de veiculação publicitária, sobre o dispositivo, incluindo o Identificador de Publicidade do seu sistema operacional, bem como dados de localização recebidos a partir dos sensores do dispositivo. Além desses dados, também pode haver coleta de dados sobre publicidade no dispositivo, incluindo cliques efetuados a partir dele, impressões e tempo de permanência em publicidade. \n Cumprindo os requisitos de transparência, detalhamos abaixo os dados do seu dispositivo que podem ser coletados se você utiliza nosso aplicativo: \n A. Identificadores anônimos de publicidade do dispositivo, atributos do dispositivo móvel e aplicativos instalados no respectivo aparelho; \n B. Dados dos sensores do aparelho; \n C. Dados anônimos de localização do aparelho por meio de GPS, rede celular e outros sensores. \n Isenção de Responsabilidade \n Não nos responsabilizamos por quaisquer danos resultantes da utilização dos nossos recursos e funcionalidades explícitas no tutorial do aplicativo, ou ainda pela indisponibilidade desses recursos, incluindo, mas não se limitando, a lucros cessantes e danos emergentes, morais e materiais. Não garantimos, tampouco, que as funções incorporadas ou existentes no aplicativo estejam disponíveis sem interrupção ou sem erros. \n Nós empregaremos todos os esforços para assegurar a precisão, correção e confiabilidade dos nossos dados, mas não concedemos quaisquer garantias nesse sentido. \n Nós nos declaramos, ainda, isentos de quaisquer responsabilidades pelos conteúdos ou disponibilidade de informações contidas no índice de pesquisa oferecido no aplicativo, bem como pela precisão de qualquer resultado de pesquisa. \n Qualquer referência a produtos, serviços, processos ou outra informação relativamente a terceiros, mediante indicação de nome comercial, marca, fabricante, fornecedor ou outros, não constitui nem implica em endosso, patrocínio ou recomendação da nossa parte, bem como em existência de qualquer relação entre nossa entidade e terceiros. \n Tolerância\n Nenhuma omissão ou demora da nossa parte em exercer os nossos direitos previstos nestes Termos de Uso e na Política de Privacidade ou previstos em lei implica ou significa renúncia ao seu exercício. \n Alterações dos Termos de Uso e da Política de Privacidade \n Os Termos de Uso e a Política de Privacidade poderão ser modificados e atualizados por nós livremente, a qualquer momento, com o objetivo de adaptá-los às novidades legislativas ou mesmo para inserir novas práticas comerciais, sendo que as alterações passarão a valer imediatamente, se outro prazo não tiver sido indicado na nova versão dos Termos de Uso. É importante, então, que você acesse este documento regularmente ao usar o aplicativo. Ele poderá conter uma alteração com a qual você não esteja de acordo. Afinal, a continuidade do uso do aplicativo por você, depois da publicação das alterações, constituirá na sua plena aceitação tácita de tais alterações. \n Legislação e Foro aplicável \n Apesar de, tecnicamente, o nosso aplicativo poder ser acessado de qualquer parte do planeta, a nossa relação com os usuários do aplicativo estará sempre, em qualquer hipótese e independente do local de onde esteja sendo acessado o serviço, sujeito à Legislação Brasileira, em especial ao Código de Defesa do Consumidor, ao Marco Civil da Internet e a estes Termos de Uso e Política de Privacidade. \n A fim de solucionar eventuais dúvidas ou controvérsias decorrentes da sua utilização ou de seu conteúdo fica desde já eleito o Foro do Estado de São Paulo, Comarca de São Paulo, salvo outro foro privilegiado determinado por lei. \n ÔNIBUS EM PONTO \n contato@onibusemponto.com"
    
    var speechTimer = Timer()
    @objc public func updateCounterSpeech() {
        speechVerificar()
    }
    func speechVerificar(){
        if self.comando == self.comandoAntigo{
            print("Igual - Parou")
            pararOuvir()
            speechTimer.invalidate()
        }else{
            self.comandoAntigo = self.comando
            print("DIFERENTE - CONTINUA")
        }
    }
    
    
    @IBOutlet weak var containerView: UIView!
    @IBOutlet weak var btnDireita: UIButton!
    
    
    //Falar
    let synth = AVSpeechSynthesizer()
    var myUtterance = AVSpeechUtterance(string: "")
    //var falando = false
    var ruaAnterior = ""
    var numAnterior = ""
    var isVoice = false
    var salvaVoice = "salvaVoice"
    var destinoIdentificado = false
    var mensagemInicio = true
    var falarDistancia = false
    var linhaPorFala = -1
    
    //Ouvir
    enum SpeechStatus {
        case ready
        case recognizing
        case unavailable
    }
    var status = SpeechStatus.ready
    let audioEngine = AVAudioEngine()
    let speechRecognizer: SFSpeechRecognizer? = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    let request = SFSpeechAudioBufferRecognitionRequest()
    var recognitionTask: SFSpeechRecognitionTask?
    var comando = ""
    var comandoAntigo = ""
    var player = AVAudioPlayer()
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        //let url = URL(string: "http://virtualartsa.com.br/")!
        //let url = URL(string: "https://www.google.com.br/")!
        //webView.load(URLRequest(url: url))
        //webView.allowsBackForwardNavigationGestures = true
        
        if getVoice() == "s"{
            //Botão
            btnDireita.frame = CGRect(x: view.frame.width * 0.5, y: 0, width: view.frame.width * 0.5, height: view.frame.height)
            btnDireita.imageEdgeInsets = UIEdgeInsetsMake(btnDireita.frame.size.height, btnDireita.frame.size.width, btnDireita.frame.size.height, btnDireita.frame.size.width)
            
            
            //Ouvir
            switch SFSpeechRecognizer.authorizationStatus() {
            case .notDetermined:
                askSpeechPermission()
            case .authorized:
                self.status = .ready
            case .denied, .restricted:
                self.status = .unavailable
            }
            isVoice = true
            falar(texto: ler)
        }else{
            btnDireita.isHidden = true
            isVoice = false
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        synth.stopSpeaking(at: AVSpeechBoundary.immediate)
        super.viewDidDisappear(animated)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let wkWebView = WKWebView(frame: CGRect(x: 0, y: 0, width: view.frame.width, height: view.frame.height))
        view.insertSubview(wkWebView, belowSubview: btnDireita)
        wkWebView.load(NSURLRequest(url: NSURL(string: "http://virtualartsa.com.br/onibusemponto/termosdeusoepoliticadeprivacidade.html")! as URL) as URLRequest)
    }
    
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    //override func loadView() {
    //    webView = WKWebView()
    //    webView.navigationDelegate = self
    //    view = webView
    //}
    
    
    //Falar
    func falar(texto: String){
        myUtterance = AVSpeechUtterance(string: texto)
        myUtterance.rate = 0.45
        synth.speak(myUtterance)
    }
    
    //
    //Ouvir
    //
    func askSpeechPermission() {
        SFSpeechRecognizer.requestAuthorization { status in
            OperationQueue.main.addOperation {
                switch status {
                case .authorized:
                    self.status = .ready
                default:
                    self.status = .unavailable
                }
            }
        }
    }
    func startRecording() {
        self.comando = ""
        self.comandoAntigo = " "
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(TermosViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
        print("1")
        // Setup audio engine and speech recognizer
        let node = audioEngine.inputNode// else { return } //Tirei um guard da frente
        let recordingFormat = node.outputFormat(forBus: 0)
        node.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            self.request.append(buffer)
        }
        print("2")
        // Prepare and start recording
        audioEngine.prepare()
        do {
            try audioEngine.start()
            self.status = .recognizing
        } catch {
            print("Errinho")
            return print(error)
        }
        print("3")
        // Analyze the speech
        recognitionTask = speechRecognizer?.recognitionTask(with: request, resultHandler: { result, error in
            if let result = result {
                print("4")
                print(result.bestTranscription.formattedString)
                self.comando = result.bestTranscription.formattedString
            } else if let error = error {
                print(error)
                print("Errinho 2")
            }
        })
    }
    func cancelRecording() {
        tocaSons(qual: 3)
        audioEngine.stop()
        let node = audioEngine.inputNode //{ //Tirei um if
        node.removeTap(onBus: 0)
        //}
        recognitionTask?.cancel()
        self.reconhecerComando()
    }
    func reconhecerComando(){
        let comand = self.comando.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
        print(comand)
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }
    }
    
    @IBAction func btnOuvir(_ sender: Any) {
        if status != .recognizing{
            print("Ouvindo")
            startRecording()
            status = .recognizing
            synth.stopSpeaking(at: AVSpeechBoundary.immediate) // Parar fala
            tocaSons(qual: 1)
        }
    }
    
    
    func pararOuvir(){
        print("Parando")
        status = .ready
        cancelRecording()
    }
    func tocaSons(qual: Int){
        //Inicio -> 1
        //Erro -> 2
        //Fim -> 3
        var arquivo: String = ""
        switch qual {
        case 1:
            arquivo = "inicio"
        case 2:
            arquivo = "erro"
        case 3:
            arquivo = "fim"
        default:
            arquivo = ""
        }
        if let path = Bundle.main.path(forResource: arquivo, ofType: "mp3"){
            let url = URL(fileURLWithPath: path)
            
            do{
                player = try AVAudioPlayer(contentsOf: url)
                player.delegate = self
                player.prepareToPlay()
                player.play()
            }catch{
                print("Erro ao execultar um som")
            }
        }
    }
    
    
    @IBAction func btnChamar(_ sender: Any) {
        
    }
    
    
}

